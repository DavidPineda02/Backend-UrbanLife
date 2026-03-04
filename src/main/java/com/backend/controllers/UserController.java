package com.backend.controllers;

// Para acceder directamente a la BD y listar/buscar usuarios
import com.backend.dao.UsuarioDAO;
// DTO que mapea el JSON del body al crear un usuario
import com.backend.dto.CreateUserRequest;
// Entidad que representa un usuario del sistema
import com.backend.models.Usuario;
// Para leer el cuerpo de la peticion HTTP
import com.backend.server.http.ApiRequest;
// Para enviar respuestas HTTP estandarizadas
import com.backend.server.http.ApiResponse;
// Servicio con la logica de negocio de usuarios
import com.backend.services.UserService;
// Para parsear el JSON del body
import com.google.gson.Gson;
// Para manipular objetos JSON manualmente
import com.google.gson.JsonObject;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

// Para la lista de usuarios retornada por findAll
import java.util.List;
// Para construir el mapa de respuesta con Map.of()
import java.util.Map;

// Controller que maneja todos los endpoints CRUD de usuarios (/api/users)
public class UserController {

    // Handler para GET /api/users: retorna todos los usuarios del sistema
    public static HttpHandler listAll() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users");

            // Obtener la lista completa de usuarios desde la base de datos
            List<Usuario> lista = UsuarioDAO.findAll();
            // Eliminar la contrasena de cada usuario antes de enviarla al cliente
            lista.forEach(usuario -> usuario.setContrasena(null));
            // Enviar la lista de usuarios serializada como JSON con codigo 200
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
        };
    }

    // Handler para GET /api/users/id?id=X: retorna un usuario especifico por su ID
    public static HttpHandler getById() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            // Leer y validar el parametro "id" de la query string con regex
            String parametrosUrl = exchange.getRequestURI().getQuery();
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            // Parsear el id a entero (ej: "id=5" -> 5)
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Leer el rol y el id del usuario autenticado desde los atributos del exchange (inyectados por AuthMiddleware)
            String rolUsuario = (String) exchange.getAttribute("rol");
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Control de recurso propio: EMPLEADO solo puede ver su propio perfil
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
                ApiResponse.error(exchange, 403, "No tiene permiso para acceder a este recurso");
                return;
            }

            // Buscar el usuario en la base de datos por su ID
            Usuario usuario = UsuarioDAO.findById(id);
            if (usuario == null) {
                ApiResponse.error(exchange, 404, "Usuario no encontrado");
                return;
            }
            // Ocultar la contrasena hasheada antes de enviar el usuario al cliente
            usuario.setContrasena(null);
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", usuario));
        };
    }

    // Handler para POST /api/users: crea un nuevo usuario con rol EMPLEADO por defecto
    public static HttpHandler create() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users");

            // Leer y validar que el body no este vacio
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Deserializar el JSON al DTO CreateUserRequest (nombre, correo, contrasena)
            CreateUserRequest request;
            try {
                request = new Gson().fromJson(cuerpo, CreateUserRequest.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }

            // Delegar la validacion, creacion y generacion de JWT al servicio
            JsonObject respuesta = UserService.validateAndCreate(
                    request.getNombre(), request.getCorreo(), request.getContrasena());
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    // Handler para PUT /api/users/id?id=X: actualiza todos los campos del usuario (nombre, correo, estado obligatorios)
    public static HttpHandler update() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            // Validar el parametro id en la query string
            String parametrosUrl = exchange.getRequestURI().getQuery();
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Control de recurso propio: EMPLEADO solo puede modificar su propio perfil
            String rolUsuario = (String) exchange.getAttribute("rol");
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
                ApiResponse.error(exchange, 403, "No tiene permiso para modificar este recurso");
                return;
            }

            // Leer el body de la peticion
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Parsear el body como JsonObject para acceder a cada campo individualmente
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }

            // Extraer cada campo del JSON, usando "" como valor por defecto si no vienen
            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : "";
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";
            // En PUT el estado tiene valor por defecto true si no se envia
            boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : true;

            // Delegar al servicio la validacion y actualizacion completa del usuario
            JsonObject respuesta = UserService.validateAndUpdate(id, nombre, correo, contrasena, estado);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    // Handler para PATCH /api/users/id?id=X: actualiza solo los campos enviados (actualizacion parcial)
    public static HttpHandler patch() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            // Validar el parametro id en la query string
            String parametrosUrl = exchange.getRequestURI().getQuery();
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Control de recurso propio: EMPLEADO solo puede modificar su propio perfil
            String rolUsuario = (String) exchange.getAttribute("rol");
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
                ApiResponse.error(exchange, 403, "No tiene permiso para modificar este recurso");
                return;
            }

            // Leer el body de la peticion
            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            // Parsear el body como JsonObject
            JsonObject datosJson;
            try {
                datosJson = new Gson().fromJson(cuerpo, JsonObject.class);
            } catch (Exception e) {
                ApiResponse.error(exchange, 400, "El cuerpo debe ser JSON valido");
                return;
            }

            // Extraer cada campo, usando null como valor por defecto (null = no actualizar ese campo)
            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : null;
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : null;
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : null;
            // Boolean (objeto) en lugar de boolean primitivo para poder diferenciar null de false
            Boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : null;

            // Delegar al servicio la actualizacion parcial (solo se actualizan los campos no nulos)
            JsonObject respuesta = UserService.partialUpdate(id, nombre, correo, contrasena, estado);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    // ============================================================
    // DELETE /api/users/id?id=X — Desactivar usuario (soft delete)
    // Comentado: se maneja el estado por true/false, no se elimina de la BD
    // Roles permitidos: SUPER_ADMIN, ADMIN
    // ============================================================
    // public static HttpHandler delete() {
    //     return exchange -> {
    //         System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");
    //
    //         // Validar que el parametro id este presente y sea un numero entero
    //         String parametrosUrl = exchange.getRequestURI().getQuery();
    //         if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
    //             ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
    //             return;
    //         }
    //         int id = Integer.parseInt(parametrosUrl.split("=")[1]);
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
