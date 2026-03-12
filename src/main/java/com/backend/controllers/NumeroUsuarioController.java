// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la lógica de negocio de números telefónicos de usuario
import com.backend.services.NumeroUsuarioService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja los endpoints de números telefónicos de usuarios.
 * Proporciona operaciones para listar, agregar y eliminar teléfonos del perfil.
 * Cada usuario solo puede gestionar sus propios números telefónicos.
 */
public class NumeroUsuarioController {

    /**
     * Handler para GET /api/numeros-usuario.
     * Retorna todos los números telefónicos del usuario autenticado.
     * @return HttpHandler que procesa la solicitud de listar números
     */
    public static HttpHandler listByUsuario() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/numeros-usuario");

            // Obtener el ID del usuario autenticado desde los atributos del exchange (inyectado por AuthMiddleware)
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Parsear el ID del usuario de String a int
            int usuarioId = Integer.parseInt(idUsuarioToken);

            // Delegar al servicio la obtención de todos los números del usuario
            JsonObject respuesta = NumeroUsuarioService.findByUsuarioId(usuarioId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/numeros-usuario.
     * Agrega un nuevo número telefónico al perfil del usuario autenticado.
     * @return HttpHandler que procesa la solicitud de agregar número
     */
    public static HttpHandler create() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/numeros-usuario");

            // Obtener el ID del usuario autenticado desde los atributos del exchange
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Parsear el ID del usuario de String a int
            int usuarioId = Integer.parseInt(idUsuarioToken);

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

            // Extraer el número del JSON, usar "" si no viene en el body
            String numero = datosJson.has("numero") ? datosJson.get("numero").getAsString() : "";

            // Delegar al servicio la validación y creación del número
            JsonObject respuesta = NumeroUsuarioService.create(numero, usuarioId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para DELETE /api/numeros-usuario?id=X.
     * Elimina un número telefónico del perfil del usuario autenticado.
     * Verifica que el número pertenezca al usuario antes de eliminar.
     * @return HttpHandler que procesa la solicitud de eliminar número
     */
    public static HttpHandler delete() {
        return exchange -> {
            // Log de la petición recibida en consola
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/numeros-usuario");

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
            int idNumero = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Obtener el ID del usuario autenticado desde los atributos del exchange
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Parsear el ID del usuario de String a int
            int usuarioId = Integer.parseInt(idUsuarioToken);

            // Delegar al servicio la verificación de propiedad y eliminación del número
            JsonObject respuesta = NumeroUsuarioService.delete(idNumero, usuarioId);
            // Extraer el código HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");
            // Enviar la respuesta al cliente con el código HTTP correspondiente
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
