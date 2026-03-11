// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la petición HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de compras
import com.backend.services.CompraService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para recibir el array de ítems de la compra
import com.google.gson.JsonArray;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints del módulo de Compras.
 * Proporciona operaciones para listar compras, ver detalle y registrar nuevas compras.
 * Las compras no se actualizan ni eliminan (son registros contables inmutables).
 */
public class CompraController {

    /**
     * Handler para GET /api/compras.
     * Retorna todas las compras del sistema ordenadas por fecha descendente.
     * @return HttpHandler que procesa la solicitud de listar compras
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/compras");

            // Delegar al servicio la obtención de todas las compras
            JsonObject respuesta = CompraService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/compras/id?id=X.
     * Retorna una compra específica con sus ítems (detalles) incluidos.
     * @return HttpHandler que procesa la solicitud de buscar compra por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/compras/id");

            // Leer los parámetros de la URL (query string)
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar que el parámetro id exista y sea un número entero
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                // Error 400 si el parámetro id no viene o tiene formato incorrecto
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                // Salir del handler sin continuar
                return;
            }
            // Extraer el valor numérico del ID del parámetro (ej: "id=5" → 5)
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Delegar al servicio la búsqueda de la compra por ID (incluye detalles)
            JsonObject respuesta = CompraService.findById(id);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/compras.
     * Registra una nueva compra con sus ítems. El costoUnitario viene del frontend.
     * Requiere autenticación JWT para obtener el ID del usuario que registra.
     * Body esperado: {fechaCompra, metodoPago, proveedorId, items: [{productoId, cantidad, costoUnitario}]}
     * @return HttpHandler que procesa la solicitud de crear compra
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/compras");

            // Obtener el ID del usuario autenticado desde el token JWT (String del subject)
            String idUsuarioStr = (String) exchange.getAttribute("userId");
            // Convertir el ID del usuario a entero
            int usuarioId = Integer.parseInt(idUsuarioStr);

            // Crear el lector de petición para obtener el cuerpo
            ApiRequest peticion = new ApiRequest(exchange);
            // Leer el cuerpo de la petición como texto
            String cuerpo = peticion.readBody();

            // Validar que el cuerpo no esté vacío
            if (cuerpo.isEmpty()) {
                // Error 400 si no se envió ningún cuerpo
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                // Salir del handler sin continuar
                return;
            }

            // Variable para almacenar el JSON parseado
            JsonObject datosJson;
            try {
                // Intentar parsear el cuerpo como objeto JSON
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                // Error 400 si el cuerpo no es un JSON válido
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                // Salir del handler sin continuar
                return;
            }

            // Extraer la fecha de compra del JSON, usar "" si no viene en el body
            String fechaCompra = datosJson.has("fechaCompra") ? datosJson.get("fechaCompra").getAsString() : "";
            // Extraer el método de pago del JSON, usar "" si no viene en el body
            String metodoPago = datosJson.has("metodoPago") ? datosJson.get("metodoPago").getAsString() : "";
            // Extraer el ID del proveedor del JSON, usar 0 si no viene en el body
            int proveedorId = datosJson.has("proveedorId") ? datosJson.get("proveedorId").getAsInt() : 0;
            // Extraer el array de ítems del JSON, usar null si no viene en el body
            JsonArray items = datosJson.has("items") ? datosJson.getAsJsonArray("items") : null;

            // Delegar al servicio la validación y creación de la compra
            JsonObject respuesta = CompraService.create(fechaCompra, metodoPago, proveedorId, usuarioId, items);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
