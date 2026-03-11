// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la petición HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de imágenes de productos
import com.backend.services.ImagenProductoService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja los endpoints de imágenes de productos.
 * Permite subir, consultar y eliminar imágenes asociadas a productos.
 * Las imágenes se envían en Base64 y se almacenan en disco local.
 */
public class ImagenProductoController {

    /**
     * Handler para POST /api/productos/imagen?id=X.
     * Recibe una imagen en Base64 y la asocia al producto indicado.
     * Body esperado: { "base64": "...", "extension": "jpg" }
     * @return HttpHandler que procesa la subida de imagen
     */
    public static HttpHandler upload() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos/imagen");

            // Leer los parámetros de la URL (query string)
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar que el parámetro id exista y sea un número entero
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                // Error 400 si el parámetro id no viene o tiene formato incorrecto
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            // Extraer el valor numérico del ID del producto
            int productoId = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Crear el lector de petición para obtener el cuerpo
            ApiRequest peticion = new ApiRequest(exchange);
            // Leer el cuerpo de la petición como texto
            String cuerpo = peticion.readBody();

            // Validar que el cuerpo no esté vacío
            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Variable para almacenar el JSON parseado
            JsonObject datosJson;
            try {
                // Intentar parsear el cuerpo como objeto JSON
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }

            // Extraer el base64 de la imagen del JSON
            String base64Data = datosJson.has("base64") ? datosJson.get("base64").getAsString() : "";
            // Extraer la extensión del archivo (jpg, png, webp, etc.)
            String extension  = datosJson.has("extension") ? datosJson.get("extension").getAsString() : "jpg";

            // Delegar al servicio la subida y almacenamiento de la imagen
            JsonObject respuesta = ImagenProductoService.upload(productoId, base64Data, extension);
            // Extraer el código HTTP interno del servicio
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/productos/imagen?id=X.
     * Retorna todas las imágenes asociadas a un producto.
     * @return HttpHandler que procesa la consulta de imágenes
     */
    public static HttpHandler getByProductoId() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos/imagen");

            // Leer los parámetros de la URL (query string)
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar que el parámetro id exista y sea un número entero
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            // Extraer el valor numérico del ID del producto
            int productoId = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Delegar al servicio la consulta de imágenes del producto
            JsonObject respuesta = ImagenProductoService.getByProductoId(productoId);
            // Extraer el código HTTP interno del servicio
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para DELETE /api/productos/imagen?id=X.
     * Elimina una imagen por su ID de la BD y del disco.
     * @return HttpHandler que procesa la eliminación de imagen
     */
    public static HttpHandler delete() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/productos/imagen");

            // Leer los parámetros de la URL (query string)
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar que el parámetro id exista y sea un número entero
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            // Extraer el valor numérico del ID de la imagen
            int imagenId = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Delegar al servicio la eliminación de la imagen
            JsonObject respuesta = ImagenProductoService.delete(imagenId);
            // Extraer el código HTTP interno del servicio
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
