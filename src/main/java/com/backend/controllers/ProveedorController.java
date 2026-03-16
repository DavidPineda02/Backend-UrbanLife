// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de proveedores
import com.backend.services.ProveedorService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints CRUD de proveedores.
 * Proporciona operaciones para listar, crear, actualizar y cambiar estado de proveedores.
 * Centraliza la gestión del directorio de proveedores del sistema.
 */
public class ProveedorController {

    /**
     * Handler para GET /api/proveedores.
     * Retorna todos los proveedores del sistema.
     * @return HttpHandler que procesa la solicitud de listar proveedores
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/proveedores");

            // Delegar al servicio la obtención de todos los proveedores
            JsonObject respuesta = ProveedorService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/proveedores/id?id=X.
     * Retorna un proveedor específico por su ID.
     * @return HttpHandler que procesa la solicitud de buscar proveedor por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/proveedores/id");

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

            // Delegar al servicio la búsqueda del proveedor por ID
            JsonObject respuesta = ProveedorService.findById(id);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/proveedores.
     * Crea un nuevo proveedor en el directorio con sus datos de contacto.
     * @return HttpHandler que procesa la solicitud de crear proveedor
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/proveedores");

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
            String nombre      = datosJson.has("nombre")      && !datosJson.get("nombre").isJsonNull()      ? datosJson.get("nombre").getAsString()      : "";
            // Extraer la razón social del JSON, usar null si no viene en el body
            String razonSocial = datosJson.has("razonSocial") && !datosJson.get("razonSocial").isJsonNull() ? datosJson.get("razonSocial").getAsString() : null;
            // Extraer el NIT del JSON, usar "" si no viene en el body
            String nit         = datosJson.has("nit")         && !datosJson.get("nit").isJsonNull()         ? datosJson.get("nit").getAsString()         : "";
            // Extraer el correo del JSON, usar null si no viene en el body
            String correo      = datosJson.has("correo")      && !datosJson.get("correo").isJsonNull()      ? datosJson.get("correo").getAsString()      : null;
            // Extraer el teléfono del JSON, usar null si no viene en el body
            String telefono    = datosJson.has("telefono")    && !datosJson.get("telefono").isJsonNull()    ? datosJson.get("telefono").getAsString()    : null;
            // Extraer la dirección del JSON, usar null si no viene en el body
            String direccion   = datosJson.has("direccion")   && !datosJson.get("direccion").isJsonNull()   ? datosJson.get("direccion").getAsString()   : null;
            // Extraer la ciudad del JSON, usar null si no viene en el body
            String ciudad      = datosJson.has("ciudad")      && !datosJson.get("ciudad").isJsonNull()      ? datosJson.get("ciudad").getAsString()      : null;

            // Delegar al servicio la validación y creación del proveedor
            JsonObject respuesta = ProveedorService.create(nombre, razonSocial, nit, correo, telefono, direccion, ciudad);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PUT /api/proveedores/id?id=X.
     * Actualiza todos los campos de un proveedor existente.
     * @return HttpHandler que procesa la solicitud de actualizar proveedor
     */
    public static HttpHandler update() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/proveedores/id");

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
            String nombre      = datosJson.has("nombre")      && !datosJson.get("nombre").isJsonNull()      ? datosJson.get("nombre").getAsString()      : "";
            // Extraer la razón social del JSON, usar null si no viene en el body
            String razonSocial = datosJson.has("razonSocial") && !datosJson.get("razonSocial").isJsonNull() ? datosJson.get("razonSocial").getAsString() : null;
            // Extraer el NIT del JSON, usar "" si no viene en el body
            String nit         = datosJson.has("nit")         && !datosJson.get("nit").isJsonNull()         ? datosJson.get("nit").getAsString()         : "";
            // Extraer el correo del JSON, usar null si no viene en el body
            String correo      = datosJson.has("correo")      && !datosJson.get("correo").isJsonNull()      ? datosJson.get("correo").getAsString()      : null;
            // Extraer el teléfono del JSON, usar null si no viene en el body
            String telefono    = datosJson.has("telefono")    && !datosJson.get("telefono").isJsonNull()    ? datosJson.get("telefono").getAsString()    : null;
            // Extraer la dirección del JSON, usar null si no viene en el body
            String direccion   = datosJson.has("direccion")   && !datosJson.get("direccion").isJsonNull()   ? datosJson.get("direccion").getAsString()   : null;
            // Extraer la ciudad del JSON, usar null si no viene en el body
            String ciudad      = datosJson.has("ciudad")      && !datosJson.get("ciudad").isJsonNull()      ? datosJson.get("ciudad").getAsString()      : null;
            // Extraer el estado del JSON, usar true por defecto si no viene en el body
            boolean estado     = datosJson.has("estado")      && !datosJson.get("estado").isJsonNull()      ? datosJson.get("estado").getAsBoolean()     : true;

            // Delegar al servicio la validación y actualización del proveedor
            JsonObject respuesta = ProveedorService.update(id, nombre, razonSocial, nit, correo, telefono, direccion, ciudad, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PATCH /api/proveedores/id?id=X.
     * Cambia únicamente el estado activo/inactivo de un proveedor.
     * @return HttpHandler que procesa la solicitud de cambio de estado
     */
    public static HttpHandler patch() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/proveedores/id");

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

            // Delegar al servicio el cambio de estado del proveedor
            JsonObject respuesta = ProveedorService.updateEstado(id, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
