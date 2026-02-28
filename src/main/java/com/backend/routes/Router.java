package com.backend.routes;

import com.backend.server.http.ApiResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router implements HttpHandler {

    Map<String, Map<String, HttpHandler>> routes = new HashMap<>();

    public void addRoute(String metodo, String ruta, HttpHandler handler) {
        routes.computeIfAbsent(metodo, clave -> new HashMap<>()).put(ruta, handler);
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

    public void patch(String path, HttpHandler handler) {
        addRoute("PATCH", path, handler);
    }

    public void delete(String path, HttpHandler handler) {
        addRoute("DELETE", path, handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String metodo = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            ApiResponse.handleCors(exchange);
            return;
        }

        String ruta = exchange.getRequestURI().getPath();
        Map<String, HttpHandler> rutasDelMetodo = routes.get(metodo);

        if (rutasDelMetodo != null) {
            HttpHandler handler = rutasDelMetodo.get(ruta);
            if (handler != null) {
                handler.handle(exchange);
                return;
            }
        }

        ApiResponse.error(exchange, 404, "Ruta no encontrada");
    }
}
