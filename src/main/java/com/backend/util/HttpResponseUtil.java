// Paquete de utilidades del sistema
package com.backend.util;

// Para la conversion de objetos a JSON usando JsonHelper
import com.backend.helpers.JsonHelper;
// Clase que representa el intercambio HTTP
import com.sun.net.httpserver.HttpExchange;

// Para el manejo de excepciones de entrada/salida
import java.io.IOException;
// Para escribir el cuerpo de la respuesta HTTP
import java.io.OutputStream;
// Para la codificacion UTF-8
import java.nio.charset.StandardCharsets;
// Para crear mapas literales con Map.of()
import java.util.Map;

/**
 * Clase utilitaria alternativa para enviar respuestas HTTP.
 * Usa JsonHelper internamente y proporciona métodos para manejo de CORS.
 * Ofrece una alternativa ApiResponse con funcionalidades similares.
 */
public class HttpResponseUtil {

    /**
     * Envía un objeto Java serializado como JSON con el código HTTP indicado.
     * Serializa objetos automáticamente y configura headers CORS.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param statusCode Código de estado HTTP
     * @param data Objeto Java a serializar como JSON
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        // Convertir el objeto a JSON y luego a bytes en UTF-8
        byte[] cuerpoBytes = JsonHelper.toJson(data).getBytes(StandardCharsets.UTF_8);

        // Agregar encabezados CORS antes de enviar la respuesta
        setCorsHeaders(exchange);
        // Indicar que el contenido es JSON con codificacion UTF-8
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        // Enviar el codigo de estado y el tamano del cuerpo
        exchange.sendResponseHeaders(statusCode, cuerpoBytes.length);

        // Escribir el cuerpo y cerrar el flujo de salida automaticamente
        try (OutputStream salida = exchange.getResponseBody()) {
            // Escribir bytes en el flujo de salida
            salida.write(cuerpoBytes);
        // El OutputStream se cierra automáticamente
        }
    }

    /**
     * Envía una respuesta de error estándar con success=false y el mensaje indicado.
     * Simplifica el envío de respuestas de error con formato estándar.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param statusCode Código de estado HTTP de error
     * @param message Mensaje de error a incluir
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        // Crear un mapa con los campos de error y delegarlo a sendJson
        sendJson(exchange, statusCode, Map.of("success", false, "message", message));
    }

    /**
     * Responde a peticiones OPTIONS (preflight CORS) con 204 No Content.
     * Maneja las solicitudes preflight del navegador para CORS.
     * @param exchange Objeto HttpExchange para la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void handleCors(HttpExchange exchange) throws IOException {
        // Agregar encabezados CORS necesarios para el preflight
        setCorsHeaders(exchange);
        // Enviar 204 sin cuerpo (-1 indica cuerpo vacio)
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Método privado que agrega los tres encabezados CORS estándar a la respuesta.
     * Centraliza la configuración de headers CORS para reutilización.
     * @param exchange Objeto HttpExchange para configurar los encabezados
     */
    private static void setCorsHeaders(HttpExchange exchange) {
        // Permitir solicitudes desde cualquier origen
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        // Definir los metodos HTTP permitidos en solicitudes cross-origin
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        // Definir los encabezados permitidos en solicitudes cross-origin
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
