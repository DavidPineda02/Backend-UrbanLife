package com.backend.server.http; // Paquete de utilidades HTTP del servidor

// Clase que representa el intercambio HTTP (peticion + respuesta)
import com.sun.net.httpserver.HttpExchange; // Clase para intercambio HTTP
// Para el manejo de excepciones de entrada/salida
import java.io.IOException; // Clase para excepciones IO
// Para leer el cuerpo de la peticion como flujo de bytes
import java.io.InputStream; // Clase para flujo de entrada
// Para convertir los bytes a texto con codificacion UTF-8
import java.nio.charset.StandardCharsets; // Clase para codificación de caracteres

/**
 * Clase auxiliar que encapsula la lectura del cuerpo de la petición HTTP entrante.
 * Proporciona métodos para leer y procesar datos de solicitudes HTTP.
 * Simplifica el manejo de cuerpos de peticiones en los controllers.
 */
public class ApiRequest {

    /** Referencia al exchange HTTP para acceder a los datos de la petición */
    public HttpExchange exchange; // Campo para el intercambio HTTP

    /**
     * Constructor que recibe el exchange de la petición actual.
     * Inicializa el objeto con el contexto de la solicitud HTTP.
     * @param exchange Objeto HttpExchange con la información de la solicitud
     */
    public ApiRequest(HttpExchange exchange) { // Constructor con exchange
        this.exchange = exchange; // Asignar exchange
    }

    /**
     * Lee y retorna el cuerpo de la petición como texto plano en UTF-8.
     * Procesa el flujo de entrada y lo convierte a cadena de texto.
     * @return String con el contenido del cuerpo de la petición
     * @throws IOException Si ocurre un error al leer el flujo de entrada
     */
    public String readBody() throws IOException { // Método para leer cuerpo
        // Obtener el flujo de entrada del cuerpo de la peticion
        InputStream flujoEntrada = exchange.getRequestBody(); // Obtener input stream
        // Leer todos los bytes del cuerpo y convertirlos a String con UTF-8
        return new String(flujoEntrada.readAllBytes(), StandardCharsets.UTF_8); // Convertir a string UTF-8
    }
}
