package com.backend.server.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ApiResponse {

    private static final Gson gson = new Gson();

    public static void send(HttpExchange exchange, String body, int statusCode) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        exchange.sendResponseHeaders(statusCode, bodyBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bodyBytes);
        }
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        send(exchange, gson.toJson(data), statusCode);
    }

    public static void success(HttpExchange exchange, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("message", message);
        send(exchange, json.toString(), 200);
    }

    public static void error(HttpExchange exchange, int code, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("message", message);
        send(exchange, json.toString(), code);
    }

    public static void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(204, -1);
    }
}
