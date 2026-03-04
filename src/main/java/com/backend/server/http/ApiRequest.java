package com.backend.server.http;

// Clase que representa el intercambio HTTP (peticion + respuesta)
import com.sun.net.httpserver.HttpExchange;
// Para el manejo de excepciones de entrada/salida
import java.io.IOException;
// Para leer el cuerpo de la peticion como flujo de bytes
import java.io.InputStream;
// Para convertir los bytes a texto con codificacion UTF-8
import java.nio.charset.StandardCharsets;

// Clase auxiliar que encapsula la lectura del cuerpo de la peticion HTTP entrante
public class ApiRequest {

    // Referencia al exchange HTTP para acceder a los datos de la peticion
    public HttpExchange exchange;

    // Constructor: recibe el exchange de la peticion actual
    public ApiRequest(HttpExchange exchange) {
        this.exchange = exchange;
    }

    // Lee y retorna el cuerpo de la peticion como texto plano en UTF-8
    public String readBody() throws IOException {
        // Obtener el flujo de entrada del cuerpo de la peticion
        InputStream flujoEntrada = exchange.getRequestBody();
        // Leer todos los bytes del cuerpo y convertirlos a String con UTF-8
        return new String(flujoEntrada.readAllBytes(), StandardCharsets.UTF_8);
    }
}
