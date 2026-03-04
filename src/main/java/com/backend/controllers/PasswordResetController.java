package com.backend.controllers; // Paquete de controladores HTTP de la aplicación

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest; // Clase para leer cuerpo de peticiones
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse; // Clase para enviar respuestas HTTP
// Servicio que contiene la logica de recuperacion de contrasena
import com.backend.services.PasswordResetService; // Lógica de negocio de recuperación
// Para parsear el JSON del body de la peticion
import com.google.gson.Gson; // Biblioteca para manejo de JSON
// Para manipular objetos JSON
import com.google.gson.JsonObject; // Clase para objetos JSON
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler; // Interfaz para manejar peticiones HTTP

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
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/forgot-password"); // Log de petición

            // Leer y validar que el cuerpo no este vacio
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Intentar parsear el body como JSON
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class); // Parsear JSON
            } catch (Exception e) { // Capturar errores de parseo
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido"); // Error 400
                return; // Salir del handler
            }
            // Extraer el correo del JSON, usar "" si no viene
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : ""; // Extraer correo

            // Delegar al servicio: generar token, guardarlo y enviar correo
            JsonObject respuesta = PasswordResetService.solicitarRecuperacion(correo); // Procesar solicitud
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
        };
    }

    /**
     * Handler para GET /api/auth/reset-password/validate?token=X.
     * Verifica que el token sea válido y no haya expirado.
     * @return HttpHandler que procesa la validación del token
     */
    public static HttpHandler validarToken() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password/validate"); // Log de petición

            // Obtener la query string de la URL (ej: "token=abc123")
            String query = exchange.getRequestURI().getQuery(); // Obtener parámetros URL
            String token = ""; // Inicializar token vacío
            // Extraer el valor del parametro "token" si existe en la query string
            if (query != null && query.contains("token=")) { // Validar que exista el parámetro
                token = query.split("token=")[1]; // Extraer valor del token
            }

            // Delegar al servicio la validacion del token contra la base de datos
            JsonObject respuesta = PasswordResetService.validarToken(token); // Validar token
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
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
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/reset-password"); // Log de petición

            // Leer y validar que el body no este vacio
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Intentar parsear el body como JSON
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class); // Parsear JSON
            } catch (Exception e) { // Capturar errores de parseo
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido"); // Error 400
                return; // Salir del handler
            }
            // Extraer token y nueva contrasena del JSON, usar "" si no vienen
            String token           = datosJson.has("token")      ? datosJson.get("token").getAsString()      : ""; // Extraer token
            String nuevaContrasena = datosJson.has("contrasena")  ? datosJson.get("contrasena").getAsString() : ""; // Extraer contraseña

            // Delegar al servicio: validar token, hashear nueva contrasena y actualizarla en BD
            JsonObject respuesta = PasswordResetService.cambiarContrasena(token, nuevaContrasena); // Cambiar contraseña
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
        };
    }
}
