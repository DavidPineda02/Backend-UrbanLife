package com.backend.server.http;

// Clase que representa el intercambio HTTP (peticion + respuesta)
import com.sun.net.httpserver.HttpExchange;
// Para el manejo de excepciones de entrada/salida
import java.io.IOException;
// Para leer el cuerpo de la peticion como flujo de bytes
import java.io.InputStream;
// Para convertir los bytes a texto con codificacion UTF-8
import java.nio.charset.StandardCharsets;

/**
 * Clase auxiliar que encapsula la lectura del cuerpo de la petición HTTP entrante.
 * Proporciona métodos para leer y procesar datos de solicitudes HTTP.
 */
public class ApiRequest {

    /** Referencia al exchange HTTP para acceder a los datos de la petición */
    public HttpExchange exchange;

    /**
     * Constructor que recibe el exchange de la petición actual.
     * @param exchange Objeto HttpExchange con la información de la solicitud
     */
    public ApiRequest(HttpExchange exchange) {
        this.exchange = exchange;
    }

    /**
     * Lee y retorna el cuerpo de la petición como texto plano en UTF-8.
     * @return String con el contenido del cuerpo de la petición
     * @throws IOException Si ocurre un error al leer el flujo de entrada
     */
    public String readBody() throws IOException {
        // Obtener el flujo de entrada del cuerpo de la peticion
        InputStream flujoEntrada = exchange.getRequestBody();
        // Leer todos los bytes del cuerpo y convertirlos a String con UTF-8
        return new String(flujoEntrada.readAllBytes(), StandardCharsets.UTF_8);
    }
}
