package com.backend.controllers; // Paquete de controladores HTTP de la aplicación

// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest; // Clase para leer cuerpo de peticiones
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse; // Clase para enviar respuestas HTTP
// Servicio que contiene la logica de validacion del login
import com.backend.services.AuthService; // Lógica de negocio de autenticación
// Para parsear el JSON del body de la peticion
import com.google.gson.Gson; // Biblioteca para manejo de JSON
// Para construir y manipular objetos JSON
import com.google.gson.JsonObject; // Clase para objetos JSON
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler; // Interfaz para manejar peticiones HTTP

/**
 * Controller que maneja los endpoints de autenticación básica.
 * Proporciona endpoints para login y verificación de usuario autenticado.
 * Centraliza la lógica de autenticación con correo y contraseña.
 */
public class AuthController {

    /**
     * Handler para POST /api/auth/login.
     * Autentica al usuario con correo y contraseña.
     * Procesa credenciales y genera token JWT si son válidas.
     * @return HttpHandler que procesa la solicitud de login
     */
    public static HttpHandler login() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/login"); // Log de petición

            // Leer y validar que el cuerpo de la peticion no este vacio
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Intentar parsear el cuerpo como JSON (si falla, el body no es JSON valido)
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class); // Parsear JSON
            } catch (Exception e) { // Capturar errores de parseo
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido"); // Error 400
                return; // Salir del handler
            }

            // Extraer correo y contrasena del JSON, usar "" si no vienen en el body
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : ""; // Extraer correo
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : ""; // Extraer contraseña

            // Delegar la validacion y autenticacion al servicio
            JsonObject respuesta = AuthService.validateLogin(correo, contrasena); // Validar credenciales
            // Extraer el codigo HTTP interno del servicio para usarlo en la respuesta
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            // Eliminar el campo "status" interno antes de enviar al cliente
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
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
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/auth/me"); // Log de petición

            // Leer los datos del usuario que el AuthMiddleware extrajo del JWT y guardo como atributos
            String idUsuario = (String) exchange.getAttribute("userId"); // ID del usuario desde JWT
            String correo = (String) exchange.getAttribute("correo");    // Correo del usuario desde JWT
            String rol = (String) exchange.getAttribute("rol");          // Rol del usuario desde JWT

            // Construir la respuesta con los datos de identidad del usuario autenticado
            JsonObject respuesta = new JsonObject(); // Crear objeto JSON de respuesta
            respuesta.addProperty("success", true); // Indicar éxito
            respuesta.addProperty("userId", idUsuario); // Agregar ID del usuario
            respuesta.addProperty("correo", correo); // Agregar correo del usuario
            respuesta.addProperty("rol", rol); // Agregar rol del usuario

            ApiResponse.send(exchange, respuesta.toString(), 200); // Enviar respuesta 200
        };
    }
}
