package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio que contiene la logica de validacion del login
import com.backend.services.AuthService;
// Para parsear el JSON del body de la peticion
import com.google.gson.Gson;
// Para construir y manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja los endpoints de autenticación básica.
 * Proporciona endpoints para login y verificación de usuario autenticado.
 */
public class AuthController {

    /**
     * Handler para POST /api/auth/login.
     * Autentica al usuario con correo y contraseña.
     * @return HttpHandler que procesa la solicitud de login
     */
    public static HttpHandler login() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/login");

            // Leer y validar que el cuerpo de la peticion no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Intentar parsear el cuerpo como JSON (si falla, el body no es JSON valido)
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }

            // Extraer correo y contrasena del JSON, usar "" si no vienen en el body
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";

            // Delegar la validacion y autenticacion al servicio
            JsonObject respuesta = AuthService.validateLogin(correo, contrasena);
            // Extraer el codigo HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt();
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/auth/me.
     * Retorna los datos del usuario autenticado via JWT.
     * Este endpoint es protegido por AuthMiddleware que inyecta los atributos userId, correo y rol.
     * @return HttpHandler que procesa la solicitud de verificación de usuario
     */
    public static HttpHandler me() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/me");

            // Leer los datos del usuario que el AuthMiddleware extrajo del JWT y guardo como atributos
            String idUsuario = (String) exchange.getAttribute("userId"); // ID del usuario
            String correo = (String) exchange.getAttribute("correo");    // Correo del usuario
            String rol = (String) exchange.getAttribute("rol");          // Rol del usuario

            // Construir la respuesta con los datos de identidad del usuario autenticado
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("success", true);
            respuesta.addProperty("userId", idUsuario);
            respuesta.addProperty("correo", correo);
            respuesta.addProperty("rol", rol);

            ApiResponse.send(exchange, respuesta.toString(), 200);
        };
    }
}
