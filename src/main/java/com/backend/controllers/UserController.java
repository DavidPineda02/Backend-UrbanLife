package com.backend.controllers; // Paquete de controladores HTTP de la aplicación

// Para acceder directamente a la BD y listar/buscar usuarios
import com.backend.dao.UsuarioDAO; // DAO para operaciones de base de datos de usuarios
// DTO que mapea el JSON del body al crear un usuario
import com.backend.dto.CreateUserRequest; // DTO para creación de usuarios
// Entidad que representa un usuario del sistema
import com.backend.models.Usuario; // Modelo de datos de usuario
// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest; // Clase para leer cuerpo de peticiones
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse; // Clase para enviar respuestas HTTP
// Servicio con la logica de negocio de usuarios
import com.backend.services.UserService; // Lógica de negocio de usuarios
// Para parsear el JSON del body
import com.google.gson.Gson; // Biblioteca para manejo de JSON
// Para manipular objetos JSON manualmente
import com.google.gson.JsonObject; // Clase para objetos JSON
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler; // Interfaz para manejar peticiones HTTP

// Para la lista de usuarios retornada por findAll
import java.util.List; // Interfaz para listas genéricas
// Para construir el mapa de respuesta con Map.of()
import java.util.Map; // Interfaz para mapas

/**
 * Controller que maneja todos los endpoints CRUD de usuarios.
 * Proporciona operaciones para crear, leer, actualizar y desactivar usuarios.
 * Centraliza la gestión de usuarios del sistema con validaciones y seguridad.
 */
public class UserController {

    /**
     * Handler para GET /api/users.
     * Retorna todos los usuarios del sistema.
     * @return HttpHandler que procesa la solicitud de listar usuarios
     */
    public static HttpHandler findAll() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users"); // Log de petición

            try {
                List<Usuario> usuarios = UsuarioDAO.findAll(); // Obtener todos los usuarios
                ApiResponse.send(exchange, new Gson().toJson(usuarios), 200); // Enviar respuesta
            } catch (Exception excepcion) { // Capturar errores de base de datos
                ApiResponse.error(exchange, 500, "Error al obtener usuarios: " + excepcion.getMessage()); // Error 500
            }
        };
    }
    
    /**
     * Handler para GET /api/users (versión mejorada).
     * Retorna todos los usuarios del sistema sin contraseñas.
     * @return HttpHandler que procesa la solicitud de listar usuarios
     */
    public static HttpHandler listAll() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users"); // Log de petición

            // Obtener la lista completa de usuarios desde la base de datos
            List<Usuario> lista = UsuarioDAO.findAll(); // Obtener todos los usuarios
            // Eliminar la contrasena de cada usuario antes de enviarla al cliente
            lista.forEach(usuario -> usuario.setContrasena(null)); // Limpiar contraseñas por seguridad
            // Enviar la lista de usuarios serializada como JSON con codigo 200
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista)); // Enviar respuesta segura
        };
    }

    /**
     * Handler para GET /api/users/id?id=X.
     * Retorna un usuario específico por su ID con control de acceso.
     * @return HttpHandler que procesa la solicitud de buscar usuario por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id"); // Log de petición

            // Leer y validar el parametro "id" de la query string con regex
            String parametrosUrl = exchange.getRequestURI().getQuery(); // Obtener parámetros URL
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) { // Validar formato id=numérico
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)"); // Error 400
                return; // Salir del handler
            }
            // Parsear el id a entero (ej: "id=5" -> 5)
            int id = Integer.parseInt(parametrosUrl.split("=")[1]); // Extraer ID numérico

            // Leer el rol y el id del usuario autenticado desde los atributos del exchange (inyectados por AuthMiddleware)
            String rolUsuario = (String) exchange.getAttribute("rol"); // Rol del usuario autenticado
            String idUsuarioToken = (String) exchange.getAttribute("userId"); // ID del usuario autenticado
            // Control de recurso propio: EMPLEADO solo puede ver su propio perfil
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) { // Validar permisos
                ApiResponse.error(exchange, 403, "No tiene permiso para acceder a este recurso"); // Error 403
                return; // Salir del handler
            }

            // Buscar el usuario en la base de datos por su ID
            Usuario usuario = UsuarioDAO.findById(id); // Buscar usuario por ID
            if (usuario == null) { // Validar que exista el usuario
                ApiResponse.error(exchange, 404, "Usuario no encontrado"); // Error 404
                return; // Salir del handler
            }
            // Ocultar la contrasena hasheada antes de enviar el usuario al cliente
            usuario.setContrasena(null); // Limpiar contraseña por seguridad
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", usuario)); // Enviar respuesta
        };
    }

    /**
     * Handler para POST /api/users.
     * Crea un nuevo usuario con rol EMPLEADO por defecto.
     * Valida datos y crea usuario con contraseña hasheada.
     * @return HttpHandler que procesa la solicitud de crear usuario
     */
    public static HttpHandler create() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users"); // Log de petición

            // Leer y validar que el body no este vacio
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Deserializar el JSON al DTO CreateUserRequest (nombre, correo, contrasena)
            CreateUserRequest request;
            try {
                request = new Gson().fromJson(cuerpo, CreateUserRequest.class); // Parsear a DTO
            } catch (Exception e) { // Capturar errores de parseo
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido"); // Error 400
                return; // Salir del handler
            }

            // Delegar la validacion, creacion y generacion de JWT al servicio
            JsonObject respuesta = UserService.validateAndCreate( // Delegar lógica de negocio
                    request.getNombre(), request.getCorreo(), request.getContrasena()); // Pasar datos
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
        };
    }

    /**
     * Handler para PUT /api/users/id?id=X.
     * Actualiza todos los campos del usuario (nombre, correo, estado obligatorios).
     * Requiere que todos los campos vengan en la petición.
     * @return HttpHandler que procesa la solicitud de actualizar usuario completo
     */
    public static HttpHandler update() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id"); // Log de petición

            // Validar el parametro id en la query string
            String parametrosUrl = exchange.getRequestURI().getQuery(); // Obtener parámetros URL
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) { // Validar formato
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)"); // Error 400
                return; // Salir del handler
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]); // Extraer ID numérico

            // Control de recurso propio: EMPLEADO solo puede modificar su propio perfil
            String rolUsuario = (String) exchange.getAttribute("rol"); // Rol del usuario autenticado
            String idUsuarioToken = (String) exchange.getAttribute("userId"); // ID del usuario autenticado
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) { // Validar permisos
                ApiResponse.error(exchange, 403, "No tiene permiso para modificar este recurso"); // Error 403
                return; // Salir del handler
            }

            // Leer el body de la peticion
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Parsear el body como JsonObject para acceder a cada campo individualmente
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class); // Parsear JSON
            } catch (Exception e) { // Capturar errores de parseo
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido"); // Error 400
                return; // Salir del handler
            }

            // Extraer cada campo del JSON, usando "" como valor por defecto si no vienen
            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : "";
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";
            // En PUT el estado tiene valor por defecto true si no se envia
            boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : true; // Estado por defecto

            // Delegar al servicio la validacion y actualizacion completa del usuario
            JsonObject respuesta = UserService.validateAndUpdate(id, nombre, correo, contrasena, estado); // Actualizar completo
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
        };
    }

    /**
     * Handler para PATCH /api/users/id?id=X.
     * Actualiza solo los campos enviados (actualización parcial).
     * Permite modificar campos individuales sin afectar los demás.
     * @return HttpHandler que procesa la solicitud de actualizar usuario parcial
     */
    public static HttpHandler patch() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id"); // Log de petición

            // Validar el parametro id en la query string
            String parametrosUrl = exchange.getRequestURI().getQuery(); // Obtener parámetros URL
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) { // Validar formato
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)"); // Error 400
                return; // Salir del handler
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]); // Extraer ID numérico

            // Control de recurso propio: EMPLEADO solo puede modificar su propio perfil
            String rolUsuario = (String) exchange.getAttribute("rol"); // Rol del usuario autenticado
            String idUsuarioToken = (String) exchange.getAttribute("userId"); // ID del usuario autenticado
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) { // Validar permisos
                ApiResponse.error(exchange, 403, "No tiene permiso para modificar este recurso"); // Error 403
                return; // Salir del handler
            }

            // Leer el body de la peticion
            ApiRequest peticion = new ApiRequest(exchange); // Crear objeto para leer cuerpo
            String cuerpo = peticion.readBody(); // Leer cuerpo de la petición

            if (cuerpo.isEmpty()) { // Validar que el cuerpo no esté vacío
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio"); // Error 400
                return; // Salir del handler
            }

            // Parsear el body como JsonObject
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class); // Parsear JSON
            } catch (Exception e) { // Capturar errores de parseo
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido"); // Error 400
                return; // Salir del handler
            }

            // Extraer cada campo, usando null como valor por defecto (null = no actualizar ese campo)
            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : null; // Nombre opcional
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : null; // Correo opcional
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : null; // Contraseña opcional
            // Boolean (objeto) en lugar de boolean primitivo para poder diferenciar null de false
            Boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : null; // Estado opcional

            // Delegar al servicio la actualizacion parcial (solo se actualizan los campos no nulos)
            JsonObject respuesta = UserService.partialUpdate(id, nombre, correo, contrasena, estado); // Actualización parcial
            int codigoHttp = respuesta.get("status").getAsInt(); // Obtener código HTTP
            respuesta.remove("status"); // Limpiar campo interno

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp); // Enviar respuesta
        };
    }

    // ============================================================
    // DELETE /api/users/id?id=X — Desactivar usuario (soft delete)
    // Comentado: se maneja el estado por true/false, no se elimina de la BD
    // Roles permitidos: SUPER_ADMIN, ADMIN
    // ============================================================
    // public static HttpHandler delete() {
    //     return exchange -> {
    //         System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id"); // Log de petición
    //
    //         // Validar que el parametro id este presente y sea un numero entero
    //         String parametrosUrl = exchange.getRequestURI().getQuery(); // Obtener parámetros URL
    //         if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) { // Validar formato
    //             ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)"); // Error 400
    //             return; // Salir del handler
    //         }
    //         int id = Integer.parseInt(parametrosUrl.split("=")[1]); // Extraer ID numérico
    //
    //         // Leer el id del usuario autenticado para validar que no se desactive a si mismo
    //         String idUsuarioToken = (String) exchange.getAttribute("userId");
    //
    //         // Delegar al servicio las validaciones y la desactivacion (estado = false)
    //         JsonObject respuesta = UserService.deleteUser(id, idUsuarioToken);
    //         int codigoHttp = respuesta.get("status").getAsInt();
    //         respuesta.remove("status");
    //
    //         ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
    //     };
    // }
}
