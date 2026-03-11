// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para enviar respuestas HTTP estandarizadas al cliente
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio del módulo Dashboard
import com.backend.services.DashboardService;
// Para manipular objetos JSON de respuesta antes de enviarlos
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java para registrar los handlers
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints del módulo Dashboard.
 * Proporciona datos de resumen contable, gráficas y métricas del negocio
 * para alimentar el home del sistema de gestión.
 * Todos los endpoints son de solo lectura (GET) y requieren autenticación JWT.
 */
public class DashboardController {

    /**
     * Handler para GET /api/dashboard/resumen.
     * Retorna las tarjetas del dashboard: ingresos/egresos del día,
     * ingresos/egresos/ganancia del mes y contadores de productos y clientes activos.
     *
     * @return HttpHandler que procesa la solicitud de resumen general
     */
    public static HttpHandler getResumen() {
        // Retornar el handler como expresión lambda que recibe el exchange HTTP
        return exchange -> {
            // Registrar en consola la petición recibida con método y ruta
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/dashboard/resumen");

            // Delegar al servicio la obtención del resumen del dashboard
            JsonObject respuesta = DashboardService.getResumen();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar la respuesta al cliente
            respuesta.remove("status");
            // Enviar la respuesta JSON al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/dashboard/ventas-semanales.
     * Retorna el total de ventas agrupado por día para los últimos 7 días.
     * Alimenta el gráfico de barras "Ventas por Semana" del home.
     *
     * @return HttpHandler que procesa la solicitud de ventas de los últimos 7 días
     */
    public static HttpHandler getVentasSemanales() {
        // Retornar el handler como expresión lambda que recibe el exchange HTTP
        return exchange -> {
            // Registrar en consola la petición recibida con método y ruta
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/dashboard/ventas-semanales");

            // Delegar al servicio la obtención de las ventas de los últimos 7 días
            JsonObject respuesta = DashboardService.getVentasSemanales();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar la respuesta al cliente
            respuesta.remove("status");
            // Enviar la respuesta JSON al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/dashboard/resumen-semanal.
     * Retorna ingresos, egresos y ganancia neta por día para los últimos 7 días.
     * Alimenta el gráfico de barras agrupadas "Resumen Semanal" del home.
     *
     * @return HttpHandler que procesa la solicitud de resumen de los últimos 7 días
     */
    public static HttpHandler getResumenSemanal() {
        // Retornar el handler como expresión lambda que recibe el exchange HTTP
        return exchange -> {
            // Registrar en consola la petición recibida con método y ruta
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/dashboard/resumen-semanal");

            // Delegar al servicio la obtención del resumen semanal por día
            JsonObject respuesta = DashboardService.getResumenSemanal();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar la respuesta al cliente
            respuesta.remove("status");
            // Enviar la respuesta JSON al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/dashboard/stock-categorias.
     * Retorna el stock total agrupado por categoría para los productos activos.
     * Alimenta el gráfico de dona "Stock por Categoría" del home.
     *
     * @return HttpHandler que procesa la solicitud de stock agrupado por categoría
     */
    public static HttpHandler getStockPorCategoria() {
        // Retornar el handler como expresión lambda que recibe el exchange HTTP
        return exchange -> {
            // Registrar en consola la petición recibida con método y ruta
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/dashboard/stock-categorias");

            // Delegar al servicio la obtención del stock agrupado por categoría
            JsonObject respuesta = DashboardService.getStockPorCategoria();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar la respuesta al cliente
            respuesta.remove("status");
            // Enviar la respuesta JSON al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/dashboard/productos-rentables.
     * Retorna los 10 productos con mayor margen absoluto (precio_venta - costo_promedio).
     * Incluye el margen porcentual calculado en la BD para mayor eficiencia.
     * Alimenta la lista "Productos Más Rentables" del home.
     *
     * @return HttpHandler que procesa la solicitud del top 10 de productos más rentables
     */
    public static HttpHandler getProductosRentables() {
        // Retornar el handler como expresión lambda que recibe el exchange HTTP
        return exchange -> {
            // Registrar en consola la petición recibida con método y ruta
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/dashboard/productos-rentables");

            // Delegar al servicio la obtención de los productos más rentables
            JsonObject respuesta = DashboardService.getProductosRentables();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar la respuesta al cliente
            respuesta.remove("status");
            // Enviar la respuesta JSON al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
