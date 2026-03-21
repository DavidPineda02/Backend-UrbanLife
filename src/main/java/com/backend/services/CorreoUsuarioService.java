// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para operaciones CRUD de correos de usuario en la base de datos
import com.backend.dao.CorreoUsuarioDAO;
// Entidad que representa un correo electrónico asociado a un usuario
import com.backend.models.CorreoUsuario;
// Para acceder a las expresiones regulares de validación centralizadas
import com.backend.helpers.ValidationHelper;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Para manejar colecciones de correos
import java.util.List;

/**
 * Servicio con la lógica de negocio para gestionar correos adicionales de usuarios.
 * Permite listar, agregar y eliminar correos electrónicos asociados a un usuario.
 * Valida formato de email y verifica propiedad del recurso antes de eliminar.
 */
public class CorreoUsuarioService {

    /** Gson compartido para serializar objetos CorreoUsuario en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los correos electrónicos asociados a un usuario.
     * @param usuarioId ID del usuario cuyos correos se consultan
     * @return JsonObject con la lista de correos y código 200
     */
    public static JsonObject findByUsuarioId(int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Obtener la lista de correos del usuario desde la base de datos
        List<CorreoUsuario> correos = CorreoUsuarioDAO.findByUsuarioId(usuarioId);
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de correos serializada como JSON
        respuesta.add("data", gson.toJsonTree(correos));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Valida y crea un nuevo correo electrónico asociado a un usuario.
     * El correo debe tener formato válido según EMAIL_REGEX.
     * @param correo Dirección de correo electrónico a agregar
     * @param usuarioId ID del usuario al que se asociará el correo
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String correo, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validar que el correo no sea nulo ni vacío -----

        // Verificar que el correo no sea nulo ni esté vacío
        if (correo == null || correo.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el correo es obligatorio
            respuesta.addProperty("message", "El correo es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar formato del correo electrónico -----

        // Verificar que el correo cumpla con el formato definido en ValidationHelper
        if (!correo.trim().matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando formato inválido
            respuesta.addProperty("message", "El formato del correo no es válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar duplicados -----

        // Obtener todos los correos del usuario para verificar si ya existe
        List<CorreoUsuario> correosExistentes = CorreoUsuarioDAO.findByUsuarioId(usuarioId);
        // Recorrer cada correo existente y comparar con el nuevo
        for (CorreoUsuario existente : correosExistentes) {
            // Comparar en minúsculas para evitar duplicados por capitalización
            if (existente.getCorreo().equalsIgnoreCase(correo.trim())) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el correo ya está registrado
                respuesta.addProperty("message", "Este correo ya está registrado");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Crear y persistir el correo -----

        // Construir el objeto CorreoUsuario con el correo limpio, sin principal (NULL) y el ID del usuario
        CorreoUsuario nuevo = new CorreoUsuario(correo.trim(), null, usuarioId);
        // Persistir el nuevo correo en la base de datos
        CorreoUsuario creado = CorreoUsuarioDAO.create(nuevo);

        // Verificar si hubo un error al insertar en la base de datos
        if (creado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al agregar el correo");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Correo agregado exitosamente");
        // Agregar los datos del correo creado (ya con ID asignado) serializado como JSON
        respuesta.add("data", gson.toJsonTree(creado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el correo creado
        return respuesta;
    }

    /**
     * Elimina un correo electrónico de un usuario verificando propiedad del recurso.
     * Solo permite eliminar si el correo pertenece al usuario autenticado.
     * @param idCorreo ID del correo a eliminar
     * @param usuarioId ID del usuario autenticado (para verificar propiedad)
     * @return JsonObject con el resultado de la eliminación
     */
    public static JsonObject delete(int idCorreo, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el correo existe -----

        // Buscar el correo en la base de datos por su ID
        CorreoUsuario correo = CorreoUsuarioDAO.findById(idCorreo);
        // Verificar si el correo no existe
        if (correo == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Correo no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar que no sea el correo principal -----

        // Verificar si el correo es el principal del usuario (no se puede eliminar)
        if (correo.getEsPrincipal() != null && correo.getEsPrincipal()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que no se puede eliminar el correo principal
            respuesta.addProperty("message", "No se puede eliminar el correo principal");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar propiedad del recurso -----

        // Verificar que el correo pertenezca al usuario autenticado
        if (correo.getUsuarioId() != usuarioId) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de acceso denegado
            respuesta.addProperty("message", "No tiene permiso para eliminar este correo");
            // Código HTTP 403 Forbidden
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Eliminar el correo -----

        // Ejecutar la eliminación en la base de datos
        if (!CorreoUsuarioDAO.delete(idCorreo)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al eliminar el correo");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la eliminación exitosa
        respuesta.addProperty("message", "Correo eliminado exitosamente");
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta de éxito
        return respuesta;
    }
}
