package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio que verifica el token de Google y autentica al usuario
import com.backend.services.GoogleAuthService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja el endpoint POST /api/auth/google para login con Google OAuth.
 * Procesa tokens de Google Sign-In y autentica usuarios en el sistema.
 */
public class GoogleAuthController {

    /**
     * Handler para POST /api/auth/google.
     * Autentica al usuario con el token de Google.
     * @return HttpHandler que procesa la solicitud de login con Google
     */
    public static HttpHandler loginWithGoogle() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/google");

            // Leer y validar que el cuerpo de la peticion no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Parsear el cuerpo como JSON para extraer el token de Google
            Gson gson = new Gson();
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class);

            // Extraer el campo "credential" que contiene el id_token de Google Sign-In
            String idToken = datosJson.has("credential") ? datosJson.get("credential").getAsString() : "";

            // Delegar al servicio la verificacion del token con Google y la autenticacion
            JsonObject respuesta = GoogleAuthService.loginWithGoogle(idToken);
            // Extraer el codigo HTTP que el servicio incluye en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo interno "status" antes de enviar la respuesta al cliente
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
