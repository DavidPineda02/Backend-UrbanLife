package com.backend.server;

import com.backend.routes.Routes;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class serverConnection {

    private static HttpServer server;

    public static void startServer(int port) {
        try {
            System.out.println("\nIniciando servidor...");

            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Configurar rutas
            Routes routes = new Routes();
            HttpHandler router = routes.configureRoutes();

            // El router maneja TODAS las peticiones
            server.createContext("/", router);

            server.setExecutor(null);
            server.start();

            System.out.println("UrbanLife Backend corriendo en: http://localhost:" + port + "\n");
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Servidor detenido.");
        }
    }
}
