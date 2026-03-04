package com.backend.controllers; // Paquete de controladores HTTP de la aplicación

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest; // Clase para leer cuerpo de peticiones
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse; // Clase para enviar respuestas HTTP
// Servicio que verifica el token de Google y autentica al usuario
import com.backend.services.GoogleAuthService; // Lógica de negocio de autenticación Google
// Para parsear el JSON del body
import com.google.gson.Gson; // Biblioteca para manejo de JSON
// Para manipular objetos JSON
import com.google.gson.JsonObject; // Clase para objetos JSON
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler; // Interfaz para manejar peticiones HTTP

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
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/google"); // Log de petición

            // Leer y validar que el cuerpo de la peticion no este vacio
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Parsear el cuerpo como JSON para extraer el token de Google
            Gson gson = new Gson(); // Crear instancia de Gson
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class); // Parsear JSON

            // Extraer el campo "credential" que contiene el id_token de Google Sign-In
            String idToken = datosJson.has("credential") ? datosJson.get("credential").getAsString() : ""; // Extraer token

            // Delegar al servicio la verificacion del token con Google y la autenticacion
            JsonObject respuesta = GoogleAuthService.loginWithGoogle(idToken); // Validar token con Google
            // Extraer el codigo HTTP que el servicio incluye en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            // Eliminar el campo interno "status" antes de enviar la respuesta al cliente
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
        };
    }
}
