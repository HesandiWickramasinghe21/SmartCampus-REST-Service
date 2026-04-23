package com.smartcampus.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int DEFAULT_PORT = 8080;

    private static int findAvailablePort(int preferred) {
        try (ServerSocket ss = new ServerSocket(preferred)) {
            ss.setReuseAddress(true);
            return preferred;
        } catch (IOException e) {
            try (ServerSocket ss = new ServerSocket(0)) {
                ss.setReuseAddress(true);
                int port = ss.getLocalPort();
                LOGGER.log(Level.WARNING,
                    "Port " + preferred + " is already in use. Using port " + port + " instead.");
                return port;
            } catch (IOException ex) {
                throw new RuntimeException("No available port found", ex);
            }
        }
    }

    public static HttpServer startServer() {
        DataStore.initializeWithSampleData();

        int port = findAvailablePort(DEFAULT_PORT);

        String baseUri = "http://0.0.0.0:" + port + "/api/v1/";
        
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus.resource",
                           "com.smartcampus.exception",
                           "com.smartcampus.filter");

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(baseUri), rc, false);

        try {
            server.start();
            String url = "http://localhost:" + port;
            LOGGER.log(Level.INFO, "========================================");
            LOGGER.log(Level.INFO, "  Smart Campus API Server Started");
            LOGGER.log(Level.INFO, "========================================");
            LOGGER.log(Level.INFO, "  Discovery : " + url + "/api/v1/");
            LOGGER.log(Level.INFO, "  Rooms     : " + url + "/api/v1/rooms");
            LOGGER.log(Level.INFO, "  Sensors   : " + url + "/api/v1/sensors");
            LOGGER.log(Level.INFO, "  Press Ctrl+C to stop.");
            LOGGER.log(Level.INFO, "========================================");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to start server", ex);
            throw new RuntimeException("Server failed to start", ex);
        }

        return server;
    }

    public static void main(String[] args) {
        final HttpServer server = startServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.log(Level.INFO, "Shutting down Smart Campus API server...");
            server.shutdownNow();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, "Server interrupted");
            server.shutdownNow();
        }
    }
}
