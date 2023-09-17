package com.oofem.app;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) {
        int httpPort = 8080;
        int websocketPort = 8081;

        try {
            // App httpServer = new App(httpPort);
            WSServer ws = new WSServer(new InetSocketAddress(websocketPort));
            // httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

            ws.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
