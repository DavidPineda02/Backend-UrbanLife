// Paquete de controllers de la API REST
package com.backend.controllers;

// Para manejar peticiones y respuestas HTTP
import com.sun.net.httpserver.HttpHandler;
// Para parsear JSON del cuerpo de la petición
import com.google.gson.JsonObject;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio de lógica de negocio para movimientos financieros
import com.backend.services.MovimientoFinancieroService;

/**
 * Controller con los endpoints de la API REST para el módulo de Movimientos Financieros.
 * Los movimientos son de SOLO LECTURA — se crean automáticamente al registrar ventas, compras y gastos.
 * Expone un único endpoint para listar todos los movimientos financieros enriquecidos con tipo y naturaleza.
 */
public class MovimientoFinancieroController {

    /**
     * Handler para GET /api/movimientos-financieros.
     * Retorna todos los movimientos financieros del sistema enriquecidos con tipo y naturaleza.
     * @return HttpHandler que procesa la solicitud de listar todos los movimientos
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/movimientos-financieros");

            // Delegar al servicio la obtención de todos los movimientos enriquecidos
            JsonObject respuesta = MovimientoFinancieroService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
