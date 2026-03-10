// Paquete de controladores HTTP de la aplicación
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
 * Integra la autenticación de Google con el sistema de usuarios local.
 */
public class GoogleAuthController {

    /**
     * Handler para POST /api/auth/google.
     * Autentica al usuario con el token de Google.
     * Recibe el token de Google Sign-In y lo valida con los servidores de Google.
     * @return HttpHandler que procesa la solicitud de login con Google
     */
    public static HttpHandler loginWithGoogle() {
        return exchange -> {
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/google");

            // Leer y validar que el cuerpo de la peticion no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            // Leer cuerpo de la petición
            String cuerpo = peticion.readBody();

            // Validar que el cuerpo no esté vacío
            if (cuerpo.isEmpty()) {
                // Error 400
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                // Salir del handler
                return;
            }

            // Parsear el cuerpo como JSON para extraer el token de Google
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }

            // Extraer el campo "credential" que contiene el id_token de Google Sign-In
            String idToken = datosJson.has("credential") ? datosJson.get("credential").getAsString() : "";

            // Delegar al servicio la verificacion del token con Google y la autenticacion
            JsonObject respuesta = GoogleAuthService.loginWithGoogle(idToken);
            // Extraer el codigo HTTP que el servicio incluye en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo interno "status" antes de enviar la respuesta al cliente
            respuesta.remove("status");

            // Enviar respuesta
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
