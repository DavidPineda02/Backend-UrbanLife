package com.backend.routes;

import com.backend.server.http.ApiResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router implements HttpHandler {

    Map<String, Map<String, HttpHandler>> routes = new HashMap<>();

    public void addRoute(String method, String path, HttpHandler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public void get(String path, HttpHandler handler) {
        addRoute("GET", path, handler);
    }

    public void post(String path, HttpHandler handler) {
        addRoute("POST", path, handler);
    }

    public void put(String path, HttpHandler handler) {
        addRoute("PUT", path, handler);
    }

    public void delete(String path, HttpHandler handler) {
        addRoute("DELETE", path, handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            ApiResponse.handleCors(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        Map<String, HttpHandler> methodRoutes = routes.get(method);

        if (methodRoutes != null) {
            HttpHandler handler = methodRoutes.get(path);
            if (handler != null) {
                handler.handle(exchange);
                return;
            }
        }

        ApiResponse.error(exchange, 404, "Ruta no encontrada");
    }
}
