// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para operaciones CRUD de teléfonos de usuario en la base de datos
import com.backend.dao.TelefonoUsuarioDAO;
// Entidad que representa un número telefónico asociado a un usuario
import com.backend.models.TelefonoUsuario;
// Para acceder a las expresiones regulares de validación centralizadas
import com.backend.helpers.ValidationHelper;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Para manejar colecciones de teléfonos
import java.util.List;

/**
 * Servicio con la lógica de negocio para gestionar teléfonos de usuarios.
 * Permite listar, agregar y eliminar números de teléfono asociados a un usuario.
 * Valida formato de teléfono colombiano y verifica propiedad del recurso antes de eliminar.
 */
public class TelefonoUsuarioService {

    /** Gson compartido para serializar objetos TelefonoUsuario en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los teléfonos asociados a un usuario.
     * @param usuarioId ID del usuario cuyos teléfonos se consultan
     * @return JsonObject con la lista de teléfonos y código 200
     */
    public static JsonObject findByUsuarioId(int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Obtener la lista de teléfonos del usuario desde la base de datos
        List<TelefonoUsuario> telefonos = TelefonoUsuarioDAO.findByUsuarioId(usuarioId);
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de teléfonos serializada como JSON
        respuesta.add("data", gson.toJsonTree(telefonos));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Valida y crea un nuevo teléfono asociado a un usuario.
     * El teléfono debe tener formato válido según TELEFONO_REGEX (7-10 dígitos colombianos).
     * @param telefono Número de teléfono a agregar
     * @param usuarioId ID del usuario al que se asociará el teléfono
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String telefono, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validar que el teléfono no sea nulo ni vacío -----

        // Verificar que el teléfono no sea nulo ni esté vacío
        if (telefono == null || telefono.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el teléfono es obligatorio
            respuesta.addProperty("message", "El número de teléfono es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar formato del número telefónico -----

        // Verificar que el teléfono cumpla con el formato colombiano (7-10 dígitos)
        if (!telefono.trim().matches(ValidationHelper.TELEFONO_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando formato inválido
            respuesta.addProperty("message", "El teléfono debe tener entre 7 y 10 dígitos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar que el teléfono no esté duplicado para este usuario -----

        // Verificar si el teléfono ya está registrado para este usuario
        if (TelefonoUsuarioDAO.existsByTelefonoAndUsuarioId(telefono.trim(), usuarioId)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el teléfono ya existe
            respuesta.addProperty("message", "Este número de teléfono ya está registrado");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir el teléfono -----

        // Construir el objeto TelefonoUsuario con el teléfono limpio, sin principal (NULL) y el ID del usuario
        TelefonoUsuario nuevo = new TelefonoUsuario(telefono.trim(), null, usuarioId);
        // Persistir el nuevo teléfono en la base de datos
        TelefonoUsuario creado = TelefonoUsuarioDAO.create(nuevo);

        // Verificar si hubo un error al insertar en la base de datos
        if (creado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al agregar el teléfono");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Teléfono agregado exitosamente");
        // Agregar los datos del teléfono creado (ya con ID asignado) serializado como JSON
        respuesta.add("data", gson.toJsonTree(creado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el teléfono creado
        return respuesta;
    }

    /**
     * Elimina un teléfono de un usuario verificando propiedad del recurso.
     * Solo permite eliminar si el teléfono pertenece al usuario autenticado.
     * @param idTelefono ID del teléfono a eliminar
     * @param usuarioId ID del usuario autenticado (para verificar propiedad)
     * @return JsonObject con el resultado de la eliminación
     */
    public static JsonObject delete(int idTelefono, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el teléfono existe -----

        // Buscar el teléfono en la base de datos por su ID
        TelefonoUsuario telefono = TelefonoUsuarioDAO.findById(idTelefono);
        // Verificar si el teléfono no existe
        if (telefono == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Teléfono no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar propiedad del recurso -----

        // Verificar que el teléfono pertenezca al usuario autenticado
        if (telefono.getUsuarioId() != usuarioId) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de acceso denegado
            respuesta.addProperty("message", "No tiene permiso para eliminar este teléfono");
            // Código HTTP 403 Forbidden
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Eliminar el teléfono -----

        // Ejecutar la eliminación en la base de datos
        if (!TelefonoUsuarioDAO.delete(idTelefono)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al eliminar el teléfono");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la eliminación exitosa
        respuesta.addProperty("message", "Teléfono eliminado exitosamente");
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta de éxito
        return respuesta;
    }
}
