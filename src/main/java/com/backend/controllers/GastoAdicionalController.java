// Paquete de controllers de la API REST
package com.backend.controllers;

// Para manejar peticiones y respuestas HTTP
import com.sun.net.httpserver.HttpHandler;
// Para parsear JSON del cuerpo de la petición
import com.google.gson.JsonObject;
import com.google.gson.Gson;
// Para leer el cuerpo de la petición HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicios de lógica de negocio para gastos adicionales
import com.backend.services.GastoAdicionalService;

/**
 * Controller con los endpoints de la API REST para el módulo de Gastos Adicionales.
 * Expone endpoints para listar, obtener por ID, crear y actualizar gastos adicionales.
 * Cada método retorna un HttpHandler que procesa las peticiones HTTP correspondientes.
 */
public class GastoAdicionalController {

    /**
     * Handler para GET /api/gastos.
     * Retorna todos los gastos adicionales del sistema.
     * @return HttpHandler que procesa la solicitud de listar todos los gastos
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
     * @return HttpHandler que procesa la solicitud de obtener gasto por ID
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
     * Crea un nuevo gasto adicional con validación y transacción atómica.
     * @return HttpHandler que procesa la solicitud de crear un gasto
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

            // Delegar al servicio la validación y creación del gasto
            JsonObject respuesta = GastoAdicionalService.create(monto, descripcion, fechaRegistro,
                    metodoPago, usuarioId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
