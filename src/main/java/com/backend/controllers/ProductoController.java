// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de productos
import com.backend.services.ProductoService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja todos los endpoints CRUD de productos.
 * Proporciona operaciones para listar, crear, actualizar y cambiar estado de productos.
 * Centraliza la gestión de productos del inventario del sistema.
 */
public class ProductoController {

    /**
     * Handler para GET /api/productos.
     * Retorna todos los productos del sistema.
     * @return HttpHandler que procesa la solicitud de listar productos
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos");

            // Delegar al servicio la obtención de todos los productos
            JsonObject respuesta = ProductoService.findAll();
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/productos/id?id=X.
     * Retorna un producto específico por su ID.
     * @return HttpHandler que procesa la solicitud de buscar producto por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos/id");

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

            // Delegar al servicio la búsqueda del producto por ID
            JsonObject respuesta = ProductoService.findById(id);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/productos.
     * Crea un nuevo producto con nombre, descripción, precio, costo, stock y categoría.
     * @return HttpHandler que procesa la solicitud de crear producto
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos");

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
            String nombre        = datosJson.has("nombre")        ? datosJson.get("nombre").getAsString()        : "";
            // Extraer la descripción del JSON, usar null si no viene en el body
            String descripcion   = datosJson.has("descripcion")   ? datosJson.get("descripcion").getAsString()   : null;
            // Extraer el precio de venta del JSON, usar 0 si no viene en el body
            double precioVenta   = datosJson.has("precioVenta")   ? datosJson.get("precioVenta").getAsDouble()   : 0;
            // Extraer el costo promedio del JSON, usar 0 si no viene en el body
            double costoPromedio = datosJson.has("costoPromedio") ? datosJson.get("costoPromedio").getAsDouble() : 0;
            // Extraer el stock del JSON, usar 0 si no viene en el body
            int stock            = datosJson.has("stock")         ? datosJson.get("stock").getAsInt()            : 0;
            // Extraer el ID de categoría del JSON, usar 0 si no viene en el body
            int categoriaId      = datosJson.has("categoriaId")   ? datosJson.get("categoriaId").getAsInt()      : 0;

            // Delegar al servicio la validación y creación del producto
            JsonObject respuesta = ProductoService.create(nombre, descripcion, precioVenta, costoPromedio, stock, categoriaId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PUT /api/productos/id?id=X.
     * Actualiza todos los campos de un producto existente.
     * @return HttpHandler que procesa la solicitud de actualizar producto
     */
    public static HttpHandler update() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos/id");

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
            String nombre        = datosJson.has("nombre")        ? datosJson.get("nombre").getAsString()        : "";
            // Extraer la descripción del JSON, usar null si no viene en el body
            String descripcion   = datosJson.has("descripcion")   ? datosJson.get("descripcion").getAsString()   : null;
            // Extraer el precio de venta del JSON, usar 0 si no viene en el body
            double precioVenta   = datosJson.has("precioVenta")   ? datosJson.get("precioVenta").getAsDouble()   : 0;
            // Extraer el costo promedio del JSON, usar 0 si no viene en el body
            double costoPromedio = datosJson.has("costoPromedio") ? datosJson.get("costoPromedio").getAsDouble() : 0;
            // Extraer el stock del JSON, usar 0 si no viene en el body
            int stock            = datosJson.has("stock")         ? datosJson.get("stock").getAsInt()            : 0;
            // Extraer el estado del JSON, usar true por defecto si no viene en el body
            boolean estado       = datosJson.has("estado")        ? datosJson.get("estado").getAsBoolean()       : true;
            // Extraer el ID de categoría del JSON, usar 0 si no viene en el body
            int categoriaId      = datosJson.has("categoriaId")   ? datosJson.get("categoriaId").getAsInt()      : 0;

            // Delegar al servicio la validación y actualización del producto
            JsonObject respuesta = ProductoService.update(id, nombre, descripcion, precioVenta, costoPromedio, stock, estado, categoriaId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para PATCH /api/productos/id?id=X.
     * Cambia únicamente el estado activo/inactivo de un producto.
     * @return HttpHandler que procesa la solicitud de cambio de estado
     */
    public static HttpHandler patch() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos/id");

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
            if (!datosJson.has("estado")) {
                // Error 400 si no se envió el campo estado
                ApiResponse.error(exchange, 400, "El campo 'estado' es requerido");
                // Salir del handler sin continuar
                return;
            }
            // Extraer el valor booleano del estado del JSON
            boolean estado = datosJson.get("estado").getAsBoolean();

            // Delegar al servicio el cambio de estado del producto
            JsonObject respuesta = ProductoService.updateEstado(id, estado);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
