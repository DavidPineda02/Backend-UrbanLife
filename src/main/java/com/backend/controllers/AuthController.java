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

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class);

            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";

            JsonObject respuesta = AuthService.validateLogin(correo, contrasena);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    public static HttpHandler me() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/me");

            String idUsuario = (String) exchange.getAttribute("userId");
            String correo = (String) exchange.getAttribute("correo");
            String rol = (String) exchange.getAttribute("rol");

            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("success", true);
            respuesta.addProperty("userId", idUsuario);
            respuesta.addProperty("correo", correo);
            respuesta.addProperty("rol", rol);

            ApiResponse.send(exchange, respuesta.toString(), 200);
        };
    }
}
