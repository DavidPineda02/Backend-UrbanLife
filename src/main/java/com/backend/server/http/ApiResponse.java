package com.backend.server.http;

// Libreria Gson para convertir objetos Java a JSON
import com.google.gson.Gson;
// Para construir objetos JSON de forma manual
import com.google.gson.JsonObject;
// Clase que representa el intercambio HTTP
import com.sun.net.httpserver.HttpExchange;

// Para el manejo de excepciones de entrada/salida
import java.io.IOException;
// Para escribir el cuerpo de la respuesta
import java.io.OutputStream;
// Para la codificacion UTF-8 al convertir texto a bytes
import java.nio.charset.StandardCharsets;

// Clase utilitaria centralizada para enviar respuestas HTTP estandarizadas
public class ApiResponse {

    // Instancia compartida de Gson para serializar objetos a JSON
    private static final Gson gson = new Gson();

    // Metodo base que envia cualquier String como respuesta con un codigo HTTP dado
    public static void send(HttpExchange exchange, String body, int statusCode) throws IOException {
        // Convertir el texto de la respuesta a bytes en UTF-8
        byte[] cuerpoBytes = body.getBytes(StandardCharsets.UTF_8);

        // Indicar al cliente que la respuesta es JSON con codificacion UTF-8
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        // Permitir solicitudes desde cualquier origen (CORS)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // Indicar los metodos HTTP permitidos en el CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        // Indicar los encabezados permitidos en el CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Enviar el codigo de estado y el tamano del cuerpo antes de escribirlo
        exchange.sendResponseHeaders(statusCode, cuerpoBytes.length);

        // Escribir el cuerpo de la respuesta y cerrar el flujo automaticamente
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(cuerpoBytes);
        }
    }

    // Convierte cualquier objeto Java a JSON y lo envia como respuesta
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        // Serializar el objeto a JSON y delegar al metodo base send()
        send(exchange, gson.toJson(data), statusCode);
    }

    // Envia una respuesta exitosa estandar con codigo 200 y un mensaje
    public static void success(HttpExchange exchange, String message) throws IOException {
        // Construir el objeto JSON con success=true y el mensaje
        JsonObject respuestaJson = new JsonObject();
        respuestaJson.addProperty("success", true);
        respuestaJson.addProperty("message", message);
        // Enviar con codigo 200 OK
        send(exchange, respuestaJson.toString(), 200);
    }

    // Envia una respuesta de error con el codigo HTTP y mensaje indicados
    public static void error(HttpExchange exchange, int code, String message) throws IOException {
        // Construir el objeto JSON con success=false y el mensaje de error
        JsonObject respuestaJson = new JsonObject();
        respuestaJson.addProperty("success", false);
        respuestaJson.addProperty("message", message);
        // Enviar con el codigo de error proporcionado (400, 401, 403, 404, 500, etc.)
        send(exchange, respuestaJson.toString(), code);
    }

    // Responde a peticiones OPTIONS (preflight CORS) con los encabezados permitidos
    public static void handleCors(HttpExchange exchange) throws IOException {
        // Agregar los encabezados CORS necesarios para el preflight del navegador
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        // Responder con 204 No Content (sin cuerpo) para confirmar que el CORS es valido
        exchange.sendResponseHeaders(204, -1);
    }
}
