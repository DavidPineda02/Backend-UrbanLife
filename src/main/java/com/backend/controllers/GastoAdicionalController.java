// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la petición HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de gastos adicionales
import com.backend.services.GastoAdicionalService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints del módulo de Gastos Adicionales.
 * Proporciona operaciones para listar gastos, ver detalle, crear y listar tipos de gasto.
 * Los gastos no se actualizan ni eliminan (son registros contables inmutables).
 */
public class GastoAdicionalController {

    /**
     * Handler para GET /api/gastos.
     * Retorna todos los gastos adicionales del sistema ordenados por fecha descendente.
     * @return HttpHandler que procesa la solicitud de listar gastos
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/gastos");

            // Delegar al servicio la obtención de todos los gastos
            JsonObject respuesta = GastoAdicionalService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/gastos/id?id=X.
     * Retorna un gasto adicional específico por su ID.
     * @return HttpHandler que procesa la solicitud de buscar gasto por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/gastos/id");

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

            // Delegar al servicio la búsqueda del gasto por ID
            JsonObject respuesta = GastoAdicionalService.findById(id);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/gastos.
     * Registra un nuevo gasto adicional con su movimiento financiero.
     * Requiere autenticación JWT para obtener el ID del usuario que registra.
     * Body esperado: {monto, descripcion, fechaRegistro, metodoPago, compraId (opcional), tipoGastoId}
     * @return HttpHandler que procesa la solicitud de crear gasto
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/gastos");

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

            // Extraer el monto del JSON, usar 0 si no viene en el body
            double monto = datosJson.has("monto") ? datosJson.get("monto").getAsDouble() : 0;
            // Extraer la descripción del JSON, usar "" si no viene en el body
            String descripcion = datosJson.has("descripcion") ? datosJson.get("descripcion").getAsString() : "";
            // Extraer la fecha de registro del JSON, usar "" si no viene en el body
            String fechaRegistro = datosJson.has("fechaRegistro") ? datosJson.get("fechaRegistro").getAsString() : "";
            // Extraer el método de pago del JSON, usar "" si no viene en el body
            String metodoPago = datosJson.has("metodoPago") ? datosJson.get("metodoPago").getAsString() : "";
            // Extraer el ID de la compra asociada del JSON, usar null si no viene en el body
            Integer compraId = datosJson.has("compraId") && !datosJson.get("compraId").isJsonNull() ? datosJson.get("compraId").getAsInt() : null;
            // Extraer el ID del tipo de gasto del JSON, usar 0 si no viene en el body
            int tipoGastoId = datosJson.has("tipoGastoId") ? datosJson.get("tipoGastoId").getAsInt() : 0;

            // Delegar al servicio la validación y creación del gasto
            JsonObject respuesta = GastoAdicionalService.create(monto, descripcion, fechaRegistro,
                    metodoPago, compraId, tipoGastoId, usuarioId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/gastos/tipos.
     * Retorna todos los tipos de gasto disponibles para el dropdown del frontend.
     * @return HttpHandler que procesa la solicitud de listar tipos de gasto
     */
    public static HttpHandler listTipos() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/gastos/tipos");

            // Delegar al servicio la obtención de todos los tipos de gasto
            JsonObject respuesta = GastoAdicionalService.findAllTiposGasto();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
