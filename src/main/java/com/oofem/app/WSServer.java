package com.oofem.app;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fem.Constraint;
import fem.Element;
import fem.Force;
import fem.Node;
import fem.Structure;
import iceb.jnumerics.Vector3D;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class WSServer extends WebSocketServer {
    private Structure structure;
    private JsonNode structureJsonNode = null;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public WSServer(InetSocketAddress address) throws IOException {
        super(address);

    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft,
            ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        builder.put("Access-Control-Allow-Origin", "*");
        builder.put("Access-Control-Allow-Origin", "http://localhost:5173/");

        return builder;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Connected");
        conn.send("Connected successfully to the WebSocket server!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Handle WebSocket connection closing
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String eventName = "";
        ObjectMapper mapper = new ObjectMapper();
        // System.out.println(structureJsonNode.get("eventName"));

        try {
            JsonNode msgJsonNode = mapper.readTree(message);
            eventName = msgJsonNode.get("eventName").textValue().trim();
            this.structureJsonNode = msgJsonNode.get("payload");

            if (eventName.equals("analyze") && structureJsonNode != null) {
                System.out.println("before generateStructure");

                try {
                    generateStructure();
                    structure.solve().toString();

                    updateStructureNodeJson();
                    System.out.println(getCurrentTime() + "Completed solving successfully.");
                    String fileContent = structureJsonNode.toPrettyString();
                    conn.send(fileContent.toString());
                } catch (Exception e) {
                    System.out.println(getCurrentTime() + "Failed to solve the structure.");
                }

            }
        } catch (Exception e) {
            conn.send("Error: invalid JSON file!");

        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Handle WebSocket errors
    }

    public void onStart() {

    }

    private void generateStructure() {
        this.structure = new Structure();

        JsonNode constraintsJsonNode = structureJsonNode.get("constraints");
        JsonNode forcesJsonNode = structureJsonNode.get("forces");
        JsonNode nodesJsonNode = structureJsonNode.get("nodes");
        JsonNode elementsJsonNode = structureJsonNode.get("elements");

        HashMap<String, Constraint> constraintsMap = new HashMap<String, Constraint>();
        HashMap<String, Force> forcesMap = new HashMap<String, Force>();
        HashMap<String, Node> nodesMap = new HashMap<String, Node>();
        HashMap<String, Element> elementsMap = new HashMap<String, Element>();

        constraintsJsonNode.forEach(constraint -> {
            String id = constraint.get("id").textValue();
            Boolean u1 = constraint.get("u1").asBoolean();
            Boolean u2 = constraint.get("u2").asBoolean();
            Boolean u3 = constraint.get("u3").asBoolean();
            constraintsMap.put(id, new Constraint(u1, u2, u3));
        });

        forcesJsonNode.forEach(force -> {
            String id = force.get("id").textValue();
            double r1 = force.get("r1").asDouble();
            double r2 = force.get("r2").asDouble();
            double r3 = force.get("r3").asDouble();
            forcesMap.put(id, new Force(r1, r2, r3));
        });

        nodesJsonNode.forEach(node -> {
            String id = node.get("id").textValue();
            double x1 = node.get("x1").asDouble();
            double x2 = node.get("x2").asDouble();
            double x3 = node.get("x3").asDouble();

            Node n = structure.addNode(x1, x2, x3);
            nodesMap.put(id, n);

            if (node.get("constraint-id") != null) {
                String constraintKey = node.get("constraint-id").textValue();
                Constraint constraint = constraintsMap.get(constraintKey);
                n.setConstraint(constraint);
            }

            if (node.get("force-id") != null) {
                String forceKey = node.get("force-id").textValue();
                Force force = forcesMap.get(forceKey);
                n.setForce(force);
            }

            if (node.get("displacement") != null) {
                JsonNode displacement = node.get("displacement");
                double u1 = displacement.get("u1").asDouble();
                double u2 = displacement.get("u2").asDouble();
                double u3 = displacement.get("u3").asDouble();
                n.setDisplacement(new Vector3D(u1, u2, u3));
            }
        });

        elementsJsonNode.forEach(element -> {
            String id = element.get("id").textValue();
            String firstNodeID = element.get("node1-id").textValue();
            String secondNodeID = element.get("node2-id").textValue();
            double elasticModulus = element.get("elasticModulus").asDouble();
            double area = element.get("area").asDouble();

            Node firstNode = nodesMap.get(firstNodeID);
            Node secondNode = nodesMap.get(secondNodeID);

            Element elem = structure.addElement(elasticModulus, area, firstNode, secondNode);
            elementsMap.put(id, elem);

            if (element.get("axialForce") != null) {
                double axialForce = element.get("axialForce").asDouble();
                elem.setAxialForce(axialForce);
            }
        });
    }

    public void updateStructureNodeJson() {
        JsonNode nodesJsonNode = structureJsonNode.get("nodes");
        JsonNode elementsJsonNode = structureJsonNode.get("elements");

        for (int i = 0; i < nodesJsonNode.size(); i++) {
            Node node = structure.getNode(i);
            JsonNode nodeJson = nodesJsonNode.get(i);
            ObjectNode displacementJson = JsonNodeFactory.instance.objectNode();

            double u1 = node.getDisplacement().c1;
            double u2 = node.getDisplacement().c2;
            double u3 = node.getDisplacement().c3;

            displacementJson.put("u1", u1);
            displacementJson.put("u2", u2);
            displacementJson.put("u3", u3);

            ((ObjectNode) nodeJson).set("displacement", displacementJson);
        }

        for (int i = 0; i < elementsJsonNode.size(); i++) {
            Element element = structure.getElement(i);
            JsonNode elementJson = elementsJsonNode.get(i);

            double axialForce = element.getAxialForce();

            ((ObjectNode) elementJson).put("axialForce", Double.toString(axialForce));
        }
    }

    private String getCurrentTime() {
        String time = dtf.format(LocalDateTime.now());
        return "[" + time + "]: ";
    }

}
