package com.oofem.app;

import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WSServer extends WebSocketServer {

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
        // Handle WebSocket connection opening
        System.out.println("Connected");
        // You can also send a welcome message or perform other actions upon connection
        conn.send("Welcome to the WebSocket server!");

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Handle WebSocket connection closing
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Handle WebSocket messages
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Handle WebSocket errors
    }

    public void onStart() {

    }
}
