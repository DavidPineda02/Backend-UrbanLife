package com.backend.server.http; // Paquete de utilidades HTTP del servidor

// Libreria Gson para convertir objetos Java a JSON
import com.google.gson.Gson; // Biblioteca para manejo de JSON
// Para construir objetos JSON de forma manual
import com.google.gson.JsonObject; // Clase para objetos JSON
// Clase que representa el intercambio HTTP
import com.sun.net.httpserver.HttpExchange; // Clase para intercambio HTTP

// Para el manejo de excepciones de entrada/salida
import java.io.IOException; // Clase para excepciones IO
// Para escribir el cuerpo de la respuesta
import java.io.OutputStream; // Clase para flujo de salida
// Para la codificacion UTF-8 al convertir texto a bytes
import java.nio.charset.StandardCharsets; // Clase para codificación de caracteres

/**
 * Clase utilitaria centralizada para enviar respuestas HTTP estandarizadas.
 * Proporciona métodos para enviar respuestas JSON con encabezados CORS.
 * Simplifica y estandariza todas las respuestas del servidor.
 */
public class ApiResponse {

    /** Instancia compartida de Gson para serializar objetos a JSON */
    private static final Gson gson = new Gson(); // Instancia para serialización JSON

    /**
     * Método base que envía cualquier String como respuesta con un código HTTP dado.
     * Configura automáticamente los encabezados CORS y Content-Type.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param body Cuerpo de la respuesta como String
     * @param statusCode Código de estado HTTP
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void send(HttpExchange exchange, String body, int statusCode) throws IOException { // Método base
        // Convertir el texto de la respuesta a bytes en UTF-8
        byte[] cuerpoBytes = body.getBytes(StandardCharsets.UTF_8); // Convertir string a bytes UTF-8

        // Indicar al cliente que la respuesta es JSON con codificacion UTF-8
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8"); // Header Content-Type
        // Permitir solicitudes desde cualquier origen (CORS)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // Header CORS origin
        // Indicar los metodos HTTP permitidos en el CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Header CORS methods
        // Indicar los encabezados permitidos en el CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header CORS headers

        // Enviar el codigo de estado y el tamano del cuerpo antes de escribirlo
        exchange.sendResponseHeaders(statusCode, cuerpoBytes.length); // Enviar headers y status

        // Escribir el cuerpo de la respuesta y cerrar el flujo automaticamente
        try (OutputStream salida = exchange.getResponseBody()) { // Try-with-resources para auto-cierre
            salida.write(cuerpoBytes); // Escribir bytes en el flujo de salida
        } // El OutputStream se cierra automáticamente
    }

    /**
     * Convierte cualquier objeto Java a JSON y lo envía como respuesta.
     * Serializa objetos automáticamente a formato JSON.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param statusCode Código de estado HTTP
     * @param data Objeto Java a serializar como JSON
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException { // Método para JSON
        // Serializar el objeto a JSON y delegar al metodo base send()
        send(exchange, gson.toJson(data), statusCode); // Convertir objeto a JSON y enviar
    }

    /**
     * Envía una respuesta exitosa estándar con código 200 y un mensaje.
     * Simplifica el envío de respuestas de éxito comunes.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param message Mensaje de éxito a incluir en la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void success(HttpExchange exchange, String message) throws IOException { // Método para éxito
        // Construir el objeto JSON con success=true y el mensaje
        JsonObject respuestaJson = new JsonObject(); // Crear objeto JSON
        respuestaJson.addProperty("success", true); // Agregar campo success
        respuestaJson.addProperty("message", message); // Agregar campo message
        // Enviar con codigo 200 OK
        send(exchange, respuestaJson.toString(), 200); // Enviar respuesta exitosa
    }

    /**
     * Envía una respuesta de error con el código HTTP y mensaje indicados.
     * Estandariza el formato de respuestas de error del sistema.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param code Código de estado HTTP de error
     * @param message Mensaje de error a incluir en la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void error(HttpExchange exchange, int code, String message) throws IOException { // Método para error
        // Construir el objeto JSON con success=false y el mensaje de error
        JsonObject respuestaJson = new JsonObject(); // Crear objeto JSON
        respuestaJson.addProperty("success", false); // Agregar campo success
        respuestaJson.addProperty("message", message); // Agregar campo message
        // Enviar con el codigo de error proporcionado (400, 401, 403, 404, 500, etc.)
        send(exchange, respuestaJson.toString(), code); // Enviar respuesta de error
    }

    /**
     * Responde a peticiones OPTIONS (preflight CORS) con los encabezados permitidos.
     * Maneja las solicitudes preflight del navegador para CORS.
     * @param exchange Objeto HttpExchange para la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void handleCors(HttpExchange exchange) throws IOException { // Método para CORS
        // Agregar los encabezados CORS necesarios para el preflight del navegador
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // Header CORS origin
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Header CORS methods
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header CORS headers
        // Responder con 204 No Content (sin cuerpo) para confirmar que el CORS es valido
        exchange.sendResponseHeaders(204, -1); // Enviar respuesta 204 sin contenido
    }
}
