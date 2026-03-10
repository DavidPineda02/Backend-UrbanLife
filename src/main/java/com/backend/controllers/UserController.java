// Paquete de controladores HTTP de la aplicación
package com.backend.controllers;

// Para acceder directamente a la BD y listar/buscar usuarios
import com.backend.dao.UsuarioDAO;
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

/**
 * Controller que maneja todos los endpoints CRUD de usuarios.
 * Proporciona operaciones para crear, leer, actualizar y desactivar usuarios.
 * Centraliza la gestión de usuarios del sistema con validaciones y seguridad.
 */
public class UserController {

    /**
     * Handler para GET /api/users.
     * Retorna todos los usuarios del sistema sin contraseñas.
     * @return HttpHandler que procesa la solicitud de listar usuarios
     */
    public static HttpHandler listAll() {
        return exchange -> {
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users");

            // Obtener la lista completa de usuarios desde la base de datos
            List<Usuario> lista = UsuarioDAO.findAll();
            // Eliminar la contrasena de cada usuario antes de enviarla al cliente
            lista.forEach(usuario -> usuario.setContrasena(null));
            // Enviar la lista de usuarios serializada como JSON con codigo 200
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
        };
    }

    /**
     * Handler para GET /api/users/id?id=X.
     * Retorna un usuario específico por su ID con control de acceso.
     * @return HttpHandler que procesa la solicitud de buscar usuario por ID
     */
    public static HttpHandler getById() {
        return exchange -> {
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            // Leer y validar el parametro "id" de la query string con regex
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar formato id=numérico
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                // Error 400
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                // Salir del handler
                return;
            }
            // Parsear el id a entero (ej: "id=5" -> 5)
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Leer el rol y el id del usuario autenticado desde los atributos del exchange (inyectados por AuthMiddleware)
            String rolUsuario = (String) exchange.getAttribute("rol");
            // ID del usuario autenticado
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Control de recurso propio: EMPLEADO solo puede ver su propio perfil
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
                // Error 403
                ApiResponse.error(exchange, 403, "No tiene permiso para acceder a este recurso");
                // Salir del handler
                return;
            }

            // Buscar el usuario en la base de datos por su ID
            Usuario usuario = UsuarioDAO.findById(id);
            // Validar que exista el usuario
            if (usuario == null) {
                // Error 404
                ApiResponse.error(exchange, 404, "Usuario no encontrado");
                // Salir del handler
                return;
            }
            // Ocultar la contrasena hasheada antes de enviar el usuario al cliente
            usuario.setContrasena(null);
            // Enviar respuesta
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", usuario));
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
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            // Validar el parametro id en la query string
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar formato
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                // Error 400
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                // Salir del handler
                return;
            }
            // Extraer ID numérico
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Control de recurso propio: EMPLEADO solo puede modificar su propio perfil
            String rolUsuario = (String) exchange.getAttribute("rol");
            // ID del usuario autenticado
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Validar permisos
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
                // Error 403
                ApiResponse.error(exchange, 403, "No tiene permiso para modificar este recurso");
                // Salir del handler
                return;
            }

            // Leer el body de la peticion
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

            // Parsear el body como JsonObject para acceder a cada campo individualmente
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

            // Extraer cada campo del JSON, usando "" como valor por defecto si no vienen
            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : "";
            String apellido = datosJson.has("apellido") ? datosJson.get("apellido").getAsString() : "";
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";
            // En PUT el estado tiene valor por defecto true si no se envia
            boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : true;
            // EMPLEADO no puede cambiar su propio estado: conservar el valor actual de la BD
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario)) {
                Usuario usuarioActual = UsuarioDAO.findById(id);
                if (usuarioActual != null) estado = usuarioActual.isEstado();
            }

            // Delegar al servicio la validacion y actualizacion completa del usuario
            JsonObject respuesta = UserService.validateAndUpdate(id, nombre, apellido, correo, contrasena, estado);
            // Obtener código HTTP
            int codigoHttp = respuesta.get("status").getAsInt();
            // Limpiar campo interno
            respuesta.remove("status");

            // Enviar respuesta
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
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
            // Log de petición
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            // Validar el parametro id en la query string
            String parametrosUrl = exchange.getRequestURI().getQuery();
            // Validar formato
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                // Error 400
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                // Salir del handler
                return;
            }
            // Extraer ID numérico
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            // Control de recurso propio: EMPLEADO solo puede modificar su propio perfil
            String rolUsuario = (String) exchange.getAttribute("rol");
            // ID del usuario autenticado
            String idUsuarioToken = (String) exchange.getAttribute("userId");
            // Validar permisos
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
                // Error 403
                ApiResponse.error(exchange, 403, "No tiene permiso para modificar este recurso");
                // Salir del handler
                return;
            }

            // Leer el body de la peticion
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

            // Parsear el body como JsonObject
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

            // Extraer cada campo, usando null como valor por defecto (null = no actualizar ese campo)
            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : null;
            String apellido = datosJson.has("apellido") ? datosJson.get("apellido").getAsString() : null;
            // Correo opcional
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : null;
            // Contraseña opcional
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : null;
            // Boolean (objeto) en lugar de boolean primitivo para poder diferenciar null de false
            Boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : null;
            // EMPLEADO no puede cambiar su propio estado: ignorar el campo si viene en el body
            if ("EMPLEADO".equalsIgnoreCase(rolUsuario)) {
                estado = null;
            }

            // Delegar al servicio la actualizacion parcial (solo se actualizan los campos no nulos)
            JsonObject respuesta = UserService.partialUpdate(id, nombre, apellido, correo, contrasena, estado);
            // Obtener código HTTP
            int codigoHttp = respuesta.get("status").getAsInt();
            // Limpiar campo interno
            respuesta.remove("status");

            // Enviar respuesta
            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

}
