package com.backend.controllers;

import com.backend.server.http.ApiRequest;
import com.backend.server.http.ApiResponse;
import com.backend.services.GoogleAuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;

public class GoogleAuthController {

    public static HttpHandler loginWithGoogle() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/google");

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class);

            String idToken = datosJson.has("credential") ? datosJson.get("credential").getAsString() : "";

            JsonObject respuesta = GoogleAuthService.loginWithGoogle(idToken);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
