// Paquete de utilidades HTTP del servidor
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

/**
 * Clase utilitaria centralizada para enviar respuestas HTTP estandarizadas.
 * Proporciona métodos para enviar respuestas JSON con encabezados CORS.
 * Simplifica y estandariza todas las respuestas del servidor.
 */
public class ApiResponse {

    /** Instancia compartida de Gson para serializar objetos a JSON */
    private static final Gson gson = new Gson();

    /**
     * Método base que envía cualquier String como respuesta con un código HTTP dado.
     * Configura automáticamente los encabezados CORS y Content-Type.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param body Cuerpo de la respuesta como String
     * @param statusCode Código de estado HTTP
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void send(HttpExchange exchange, String body, int statusCode) throws IOException {
        // Convertir el texto de la respuesta a bytes en UTF-8
        byte[] cuerpoBytes = body.getBytes(StandardCharsets.UTF_8);

        // Indicar al cliente que la respuesta es JSON con codificacion UTF-8
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        // Permitir solicitudes desde cualquier origen (CORS)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // Indicar los metodos HTTP permitidos en el CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        // Indicar los encabezados permitidos en el CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Enviar el codigo de estado y el tamano del cuerpo antes de escribirlo
        exchange.sendResponseHeaders(statusCode, cuerpoBytes.length);

        // Escribir el cuerpo de la respuesta y cerrar el flujo automaticamente
        try (OutputStream salida = exchange.getResponseBody()) {
            // Escribir bytes en el flujo de salida
            salida.write(cuerpoBytes);
        // El OutputStream se cierra automáticamente
        }
    }

    /**
     * Convierte cualquier objeto Java a JSON y lo envía como respuesta.
     * Serializa objetos automáticamente a formato JSON.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param statusCode Código de estado HTTP
     * @param data Objeto Java a serializar como JSON
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        // Serializar el objeto a JSON y delegar al metodo base send()
        send(exchange, gson.toJson(data), statusCode);
    }

    /**
     * Envía una respuesta exitosa estándar con código 200 y un mensaje.
     * Simplifica el envío de respuestas de éxito comunes.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param message Mensaje de éxito a incluir en la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void success(HttpExchange exchange, String message) throws IOException {
        // Construir el objeto JSON con success=true y el mensaje
        JsonObject respuestaJson = new JsonObject();
        // Agregar campo success
        respuestaJson.addProperty("success", true);
        // Agregar campo message
        respuestaJson.addProperty("message", message);
        // Enviar con codigo 200 OK
        send(exchange, respuestaJson.toString(), 200);
    }

    /**
     * Envía una respuesta de error con el código HTTP y mensaje indicados.
     * Estandariza el formato de respuestas de error del sistema.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param code Código de estado HTTP de error
     * @param message Mensaje de error a incluir en la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void error(HttpExchange exchange, int code, String message) throws IOException {
        // Construir el objeto JSON con success=false y el mensaje de error
        JsonObject respuestaJson = new JsonObject();
        // Agregar campo success
        respuestaJson.addProperty("success", false);
        // Agregar campo message
        respuestaJson.addProperty("message", message);
        // Enviar con el codigo de error proporcionado (400, 401, 403, 404, 500, etc.)
        send(exchange, respuestaJson.toString(), code);
    }

    /**
     * Responde a peticiones OPTIONS (preflight CORS) con los encabezados permitidos.
     * Maneja las solicitudes preflight del navegador para CORS.
     * @param exchange Objeto HttpExchange para la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void handleCors(HttpExchange exchange) throws IOException {
        // Agregar los encabezados CORS necesarios para el preflight del navegador
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // Header CORS methods
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        // Header CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        // Responder con 204 No Content (sin cuerpo) para confirmar que el CORS es valido
        exchange.sendResponseHeaders(204, -1);
    }
}
