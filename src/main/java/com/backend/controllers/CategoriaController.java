// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de categorías
import com.backend.services.CategoriaService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints CRUD de categorías.
 * Proporciona operaciones para listar, crear, actualizar y cambiar estado de categorías.
 * Centraliza la gestión de categorías del sistema.
 */
public class CategoriaController {

    /**
     * Handler para GET /api/categorias.
     * Retorna todas las categorías del sistema.
     * @return HttpHandler que procesa la solicitud de listar categorías
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/categorias");

            // Delegar al servicio la obtención de todas las categorías
            JsonObject respuesta = CategoriaService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/categorias/id?id=X.
     * Retorna una categoría específica por su ID.
     * @return HttpHandler que procesa la solicitud de buscar categoría por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/categorias/id");

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

            // Delegar al servicio la búsqueda de la categoría por ID
            JsonObject respuesta = CategoriaService.findById(id);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/categorias.
     * Crea una nueva categoría con nombre y descripción opcional.
     * @return HttpHandler que procesa la solicitud de crear categoría
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/categorias");

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

            // Extraer el nombre del JSON, usar "" si no viene en el body (verificar que no sea JsonNull)
            String nombre      = datosJson.has("nombre") && !datosJson.get("nombre").isJsonNull()           ? datosJson.get("nombre").getAsString()      : "";
            // Extraer la descripción del JSON, usar null si no viene en el body (verificar que no sea JsonNull)
            String descripcion = datosJson.has("descripcion") && !datosJson.get("descripcion").isJsonNull() ? datosJson.get("descripcion").getAsString() : null;

            // Delegar al servicio la validación y creación de la categoría
            JsonObject respuesta = CategoriaService.create(nombre, descripcion);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PUT /api/categorias/id?id=X.
     * Actualiza todos los campos de una categoría existente.
     * @return HttpHandler que procesa la solicitud de actualizar categoría
     */
    public static HttpHandler update() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/categorias/id");

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

            // Extraer el nombre del JSON, usar "" si no viene en el body (verificar que no sea JsonNull)
            String nombre      = datosJson.has("nombre") && !datosJson.get("nombre").isJsonNull()           ? datosJson.get("nombre").getAsString()      : "";
            // Extraer la descripción del JSON, usar null si no viene en el body (verificar que no sea JsonNull)
            String descripcion = datosJson.has("descripcion") && !datosJson.get("descripcion").isJsonNull() ? datosJson.get("descripcion").getAsString() : null;
            // Extraer el estado del JSON, usar true por defecto si no viene en el body (verificar que no sea JsonNull)
            boolean estado     = datosJson.has("estado") && !datosJson.get("estado").isJsonNull() ? datosJson.get("estado").getAsBoolean() : true;

            // Delegar al servicio la validación y actualización de la categoría
            JsonObject respuesta = CategoriaService.update(id, nombre, descripcion, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PATCH /api/categorias/id?id=X.
     * Cambia únicamente el estado activo/inactivo de una categoría.
     * @return HttpHandler que procesa la solicitud de cambio de estado
     */
    public static HttpHandler patch() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/categorias/id");

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

            // PATCH solo permite cambiar el estado, verificar que venga en el body y no sea null
            if (!datosJson.has("estado") || datosJson.get("estado").isJsonNull()) {
                // Error 400 si no se envió el campo estado o es null
                ApiResponse.error(exchange, 400, "El campo 'estado' es requerido");
                // Salir del handler sin continuar
                return;
            }
            // Extraer el valor booleano del estado del JSON
            boolean estado = datosJson.get("estado").getAsBoolean();

            // Delegar al servicio el cambio de estado de la categoría
            JsonObject respuesta = CategoriaService.updateEstado(id, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
