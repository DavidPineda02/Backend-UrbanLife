package com.backend.controllers;

import com.backend.server.http.ApiRequest;
import com.backend.server.http.ApiResponse;
import com.backend.services.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;

public class AuthController {

    public static HttpHandler login() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/login");

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            String correo = json.has("correo") ? json.get("correo").getAsString() : "";
            String contrasena = json.has("contrasena") ? json.get("contrasena").getAsString() : "";

            JsonObject response = AuthService.validateLogin(correo, contrasena);
            int code = response.get("status").getAsInt();
            response.remove("status");

            ApiResponse.send(exchange, response.toString(), code);
        };
    }

    public static HttpHandler me() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/me");

            String userId = (String) exchange.getAttribute("userId");
            String correo = (String) exchange.getAttribute("correo");
            String rol = (String) exchange.getAttribute("rol");

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("userId", userId);
            response.addProperty("correo", correo);
            response.addProperty("rol", rol);

            ApiResponse.send(exchange, response.toString(), 200);
        };
    }
}
