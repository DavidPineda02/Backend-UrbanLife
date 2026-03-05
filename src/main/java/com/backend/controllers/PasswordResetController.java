// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio que contiene la logica de recuperacion de contrasena
import com.backend.services.PasswordResetService;
// Para parsear el JSON del body de la peticion
import com.google.gson.Gson;
// Para manipular objetos JSON
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Controller que maneja los tres endpoints del flujo de recuperación de contraseña.
 * Proporciona endpoints para solicitar, validar y cambiar contraseñas.
 * Implementa el flujo completo de recuperación de forma segura.
 */
public class PasswordResetController {

    /**
     * Handler para POST /api/auth/forgot-password.
     * Solicita el enlace de recuperación por correo.
     * Genera un token único y lo envía por correo electrónico.
     * @return HttpHandler que procesa la solicitud de recuperación
     */
    public static HttpHandler solicitarRecuperacion() {
        return exchange -> {
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/forgot-password");

            // Leer y validar que el cuerpo no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            // Leer cuerpo de la petición
            String cuerpo = peticion.readBody();

            // Validar que el cuerpo no esté vacío
            if (cuerpo.isEmpty()) {
                // Error 400
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                // Salir del handler
                return;
            }

            // Intentar parsear el body como JSON
            JsonObject datosJson;
            try {
                // Parsear JSON
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            // Capturar errores de parseo
            } catch (Exception e) {
                // Error 400
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                // Salir del handler
                return;
            }
            // Extraer el correo del JSON, usar "" si no viene
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";

            // Delegar al servicio: generar token, guardarlo y enviar correo
            JsonObject respuesta = PasswordResetService.solicitarRecuperacion(correo);
            // Obtener código HTTP
            int codigoHttp = respuesta.get("status").getAsInt();
            // Limpiar campo interno
            respuesta.remove("status");

            // Enviar respuesta
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para GET /api/auth/reset-password/validate?token=X.
     * Verifica que el token sea válido y no haya expirado.
     * @return HttpHandler que procesa la validación del token
     */
    public static HttpHandler validarToken() {
        return exchange -> {
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password/validate");

            // Obtener la query string de la URL (ej: "token=abc123")
            String query = exchange.getRequestURI().getQuery();
            // Inicializar token vacío
            String token = "";
            // Extraer el valor del parametro "token" si existe en la query string
            if (query != null && query.contains("token=")) {
                // Extraer valor del token
                token = query.split("token=")[1];
            }

            // Delegar al servicio la validacion del token contra la base de datos
            JsonObject respuesta = PasswordResetService.validarToken(token);
            // Obtener código HTTP
            int codigoHttp = respuesta.get("status").getAsInt();
            // Limpiar campo interno
            respuesta.remove("status");

            // Enviar respuesta
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    /**
     * Handler para POST /api/auth/reset-password.
     * Cambia la contraseña usando el token de recuperación.
     * Valida el token y actualiza la contraseña del usuario.
     * @return HttpHandler que procesa el cambio de contraseña
     */
    public static HttpHandler cambiarContrasena() {
        return exchange -> {
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password");

            // Leer y validar que el body no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            // Leer cuerpo de la petición
            String cuerpo = peticion.readBody();

            // Validar que el cuerpo no esté vacío
            if (cuerpo.isEmpty()) {
                // Error 400
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                // Salir del handler
                return;
            }

            // Intentar parsear el body como JSON
            JsonObject datosJson;
            try {
                // Parsear JSON
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            // Capturar errores de parseo
            } catch (Exception e) {
                // Error 400
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                // Salir del handler
                return;
            }
            // Extraer token y nueva contrasena del JSON, usar "" si no vienen
            String token           = datosJson.has("token")      ? datosJson.get("token").getAsString()      : "";
            // Extraer contraseña
            String nuevaContrasena = datosJson.has("contrasena")  ? datosJson.get("contrasena").getAsString() : "";

            // Delegar al servicio: validar token, hashear nueva contrasena y actualizarla en BD
            JsonObject respuesta = PasswordResetService.cambiarContrasena(token, nuevaContrasena);
            // Obtener código HTTP
            int codigoHttp = respuesta.get("status").getAsInt();
            // Limpiar campo interno
            respuesta.remove("status");

            // Enviar respuesta
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }
}
