package com.backend.util; // Paquete de utilidades del sistema

// Para la conversion de objetos a JSON usando JsonHelper
import com.backend.helpers.JsonHelper; // Helper para manejo de JSON
// Clase que representa el intercambio HTTP
import com.sun.net.httpserver.HttpExchange; // Clase para intercambio HTTP

// Para el manejo de excepciones de entrada/salida
import java.io.IOException; // Clase para excepciones IO
// Para escribir el cuerpo de la respuesta HTTP
import java.io.OutputStream; // Clase para flujo de salida
// Para la codificacion UTF-8
import java.nio.charset.StandardCharsets; // Clase para codificación de caracteres
// Para crear mapas literales con Map.of()
import java.util.Map; // Interfaz para mapas

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
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException { // Método para JSON
        // Convertir el objeto a JSON y luego a bytes en UTF-8
        byte[] cuerpoBytes = JsonHelper.toJson(data).getBytes(StandardCharsets.UTF_8); // Serializar y convertir

        // Agregar encabezados CORS antes de enviar la respuesta
        setCorsHeaders(exchange); // Configurar headers CORS
        // Indicar que el contenido es JSON con codificacion UTF-8
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); // Header Content-Type
        // Enviar el codigo de estado y el tamano del cuerpo
        exchange.sendResponseHeaders(statusCode, cuerpoBytes.length); // Enviar headers y status

        // Escribir el cuerpo y cerrar el flujo de salida automaticamente
        try (OutputStream salida = exchange.getResponseBody()) { // Try-with-resources para auto-cierre
            salida.write(cuerpoBytes); // Escribir bytes en el flujo de salida
        } // El OutputStream se cierra automáticamente
    }

    /**
     * Envía una respuesta de error estándar con success=false y el mensaje indicado.
     * Simplifica el envío de respuestas de error con formato estándar.
     * @param exchange Objeto HttpExchange para la respuesta
     * @param statusCode Código de estado HTTP de error
     * @param message Mensaje de error a incluir
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException { // Método para error
        // Crear un mapa con los campos de error y delegarlo a sendJson
        sendJson(exchange, statusCode, Map.of("success", false, "message", message)); // Crear mapa de error y enviar
    }

    /**
     * Responde a peticiones OPTIONS (preflight CORS) con 204 No Content.
     * Maneja las solicitudes preflight del navegador para CORS.
     * @param exchange Objeto HttpExchange para la respuesta
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    public static void handleCors(HttpExchange exchange) throws IOException { // Método para CORS
        // Agregar encabezados CORS necesarios para el preflight
        setCorsHeaders(exchange); // Configurar headers CORS
        // Enviar 204 sin cuerpo (-1 indica cuerpo vacio)
        exchange.sendResponseHeaders(204, -1); // Enviar respuesta 204 sin contenido
    }

    /**
     * Método privado que agrega los tres encabezados CORS estándar a la respuesta.
     * Centraliza la configuración de headers CORS para reutilización.
     * @param exchange Objeto HttpExchange para configurar los encabezados
     */
    private static void setCorsHeaders(HttpExchange exchange) { // Método privado para CORS
        // Permitir solicitudes desde cualquier origen
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Header CORS origin
        // Definir los metodos HTTP permitidos en solicitudes cross-origin
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Header CORS methods
        // Definir los encabezados permitidos en solicitudes cross-origin
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header CORS headers
    }
}
