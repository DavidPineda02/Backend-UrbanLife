// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de clientes
import com.backend.services.ClienteService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints CRUD de clientes.
 * Proporciona operaciones para listar, crear, actualizar y cambiar estado de clientes.
 * Centraliza la gestión del directorio de clientes del sistema.
 */
public class ClienteController {

    /**
     * Handler para GET /api/clientes.
     * Retorna todos los clientes del sistema.
     * @return HttpHandler que procesa la solicitud de listar clientes
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/clientes");

            // Delegar al servicio la obtención de todos los clientes
            JsonObject respuesta = ClienteService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/clientes/id?id=X.
     * Retorna un cliente específico por su ID.
     * @return HttpHandler que procesa la solicitud de buscar cliente por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/clientes/id");

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

            // Delegar al servicio la búsqueda del cliente por ID
            JsonObject respuesta = ClienteService.findById(id);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/clientes.
     * Crea un nuevo cliente en el directorio con sus datos de contacto.
     * @return HttpHandler que procesa la solicitud de crear cliente
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/clientes");

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

            // Extraer el nombre del JSON, usar "" si no viene en el body
            String nombre    = datosJson.has("nombre")    && !datosJson.get("nombre").isJsonNull()    ? datosJson.get("nombre").getAsString()    : "";
            // Extraer el documento numérico del JSON, usar null si no viene en el body
            Long documento   = datosJson.has("documento") && !datosJson.get("documento").isJsonNull() ? datosJson.get("documento").getAsLong()   : null;
            // Extraer el correo del JSON, usar null si no viene en el body
            String correo    = datosJson.has("correo")    && !datosJson.get("correo").isJsonNull()    ? datosJson.get("correo").getAsString()    : null;
            // Extraer el teléfono del JSON, usar null si no viene en el body
            String telefono  = datosJson.has("telefono")  && !datosJson.get("telefono").isJsonNull()  ? datosJson.get("telefono").getAsString()  : null;
            // Extraer la dirección del JSON, usar null si no viene en el body
            String direccion = datosJson.has("direccion") && !datosJson.get("direccion").isJsonNull() ? datosJson.get("direccion").getAsString() : null;
            // Extraer la ciudad del JSON, usar null si no viene en el body
            String ciudad    = datosJson.has("ciudad")    && !datosJson.get("ciudad").isJsonNull()    ? datosJson.get("ciudad").getAsString()    : null;

            // Delegar al servicio la validación y creación del cliente
            JsonObject respuesta = ClienteService.create(nombre, documento, correo, telefono, direccion, ciudad);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PUT /api/clientes/id?id=X.
     * Actualiza todos los campos de un cliente existente.
     * @return HttpHandler que procesa la solicitud de actualizar cliente
     */
    public static HttpHandler update() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/clientes/id");

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

            // Extraer el nombre del JSON, usar "" si no viene en el body
            String nombre    = datosJson.has("nombre")    && !datosJson.get("nombre").isJsonNull()    ? datosJson.get("nombre").getAsString()    : "";
            // Extraer el documento numérico del JSON, usar null si no viene en el body
            Long documento   = datosJson.has("documento") && !datosJson.get("documento").isJsonNull() ? datosJson.get("documento").getAsLong()   : null;
            // Extraer el correo del JSON, usar null si no viene en el body
            String correo    = datosJson.has("correo")    && !datosJson.get("correo").isJsonNull()    ? datosJson.get("correo").getAsString()    : null;
            // Extraer el teléfono del JSON, usar null si no viene en el body
            String telefono  = datosJson.has("telefono")  && !datosJson.get("telefono").isJsonNull()  ? datosJson.get("telefono").getAsString()  : null;
            // Extraer la dirección del JSON, usar null si no viene en el body
            String direccion = datosJson.has("direccion") && !datosJson.get("direccion").isJsonNull() ? datosJson.get("direccion").getAsString() : null;
            // Extraer la ciudad del JSON, usar null si no viene en el body
            String ciudad    = datosJson.has("ciudad")    && !datosJson.get("ciudad").isJsonNull()    ? datosJson.get("ciudad").getAsString()    : null;
            // Extraer el estado del JSON, usar true por defecto si no viene en el body
            boolean estado   = datosJson.has("estado")    && !datosJson.get("estado").isJsonNull()    ? datosJson.get("estado").getAsBoolean()   : true;

            // Delegar al servicio la validación y actualización del cliente
            JsonObject respuesta = ClienteService.update(id, nombre, documento, correo, telefono, direccion, ciudad, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PATCH /api/clientes/id?id=X.
     * Cambia únicamente el estado activo/inactivo de un cliente.
     * @return HttpHandler que procesa la solicitud de cambio de estado
     */
    public static HttpHandler patch() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/clientes/id");

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

            // PATCH solo permite cambiar el estado, verificar que venga en el body
            if (!datosJson.has("estado") || datosJson.get("estado").isJsonNull()) {
                // Error 400 si no se envió el campo estado
                ApiResponse.error(exchange, 400, "El campo 'estado' es requerido");
                // Salir del handler sin continuar
                return;
            }
            // Extraer el valor booleano del estado del JSON
            boolean estado = datosJson.get("estado").getAsBoolean();

            // Delegar al servicio el cambio de estado del cliente
            JsonObject respuesta = ClienteService.updateEstado(id, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
