package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio que contiene la logica de recuperacion de contrasena
import com.backend.services.PasswordResetService;
// Para parsear el JSON del body de la peticion
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

// Controller que maneja los tres endpoints del flujo de recuperacion de contrasena
public class PasswordResetController {

    // Handler para POST /api/auth/forgot-password: solicita el enlace de recuperacion por correo
    public static HttpHandler solicitarRecuperacion() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/forgot-password");

            // Leer y validar que el cuerpo no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Intentar parsear el body como JSON
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }
            // Extraer el correo del JSON, usar "" si no viene
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";

            // Delegar al servicio: generar token, guardarlo y enviar correo
            JsonObject respuesta = PasswordResetService.solicitarRecuperacion(correo);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    // Handler para GET /api/auth/reset-password/validate?token=X: verifica que el token sea valido y no haya expirado
    public static HttpHandler validarToken() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password/validate");

            // Obtener la query string de la URL (ej: "token=abc123")
            String query = exchange.getRequestURI().getQuery();
            String token = "";
            // Extraer el valor del parametro "token" si existe en la query string
            if (query != null && query.contains("token=")) {
                token = query.split("token=")[1];
            }

            // Delegar al servicio la validacion del token contra la base de datos
            JsonObject respuesta = PasswordResetService.validarToken(token);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    // Handler para POST /api/auth/reset-password: cambia la contrasena usando el token de recuperacion
    public static HttpHandler cambiarContrasena() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password");

            // Leer y validar que el body no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Intentar parsear el body como JSON
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }
            // Extraer token y nueva contrasena del JSON, usar "" si no vienen
            String token           = datosJson.has("token")      ? datosJson.get("token").getAsString()      : "";
            String nuevaContrasena = datosJson.has("contrasena")  ? datosJson.get("contrasena").getAsString() : "";

            // Delegar al servicio: validar token, hashear nueva contrasena y actualizarla en BD
            JsonObject respuesta = PasswordResetService.cambiarContrasena(token, nuevaContrasena);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
