package com.backend.controllers;

import com.backend.server.http.ApiRequest;
import com.backend.server.http.ApiResponse;
import com.backend.services.PasswordResetService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;

public class PasswordResetController {

    public static HttpHandler solicitarRecuperacion() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/forgot-password");

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";

            JsonObject respuesta = PasswordResetService.solicitarRecuperacion(correo);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    public static HttpHandler validarToken() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password/validate");

            String query = exchange.getRequestURI().getQuery();
            String token = "";
            if (query != null && query.contains("token=")) {
                token = query.split("token=")[1];
            }

            JsonObject respuesta = PasswordResetService.validarToken(token);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    public static HttpHandler cambiarContrasena() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password");

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }
            String token          = datosJson.has("token")      ? datosJson.get("token").getAsString()      : "";
            String nuevaContrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";

            JsonObject respuesta = PasswordResetService.cambiarContrasena(token, nuevaContrasena);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
