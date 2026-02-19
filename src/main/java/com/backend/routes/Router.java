package com.backend.routes;

import com.backend.controllers.AuthController;
import com.backend.controllers.UserController;
import com.backend.middlewares.AuthMiddleware;
import com.backend.util.HttpResponseUtil;
import com.sun.net.httpserver.HttpServer;

import java.util.Map;

public class Router {

    public static void registerRoutes(HttpServer server) {

        // Rutas públicas
        server.createContext("/api/auth/login", new AuthController());
        server.createContext("/api/users", new UserController());

        // Rutas protegidas (requieren JWT)
        server.createContext("/api/auth/me", new AuthMiddleware(exchange -> {
            try {
                HttpResponseUtil.sendJson(exchange, 200, Map.of(
                        "success", true,
                        "userId", (String) exchange.getAttribute("userId"),
                        "correo", (String) exchange.getAttribute("correo"),
                        "rol", (String) exchange.getAttribute("rol")));
            } catch (Exception e) {
                HttpResponseUtil.sendError(exchange, 500, "Error interno del servidor");
            }
        }));

        System.out.println("Rutas registradas:");
        System.out.println("  POST   /api/auth/login   (público)");
        System.out.println("  GET    /api/auth/me       (protegido)");
        System.out.println("  CRUD   /api/users         (público)");
    }
}
