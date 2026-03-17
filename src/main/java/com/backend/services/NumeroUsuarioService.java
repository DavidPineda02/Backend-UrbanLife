// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para operaciones CRUD de números de usuario en la base de datos
import com.backend.dao.NumeroUsuarioDAO;
// Entidad que representa un número telefónico asociado a un usuario
import com.backend.models.NumeroUsuario;
// Para acceder a las expresiones regulares de validación centralizadas
import com.backend.helpers.ValidationHelper;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Para manejar colecciones de números
import java.util.List;

/**
 * Servicio con la lógica de negocio para gestionar números telefónicos de usuarios.
 * Permite listar, agregar y eliminar números de teléfono asociados a un usuario.
 * Valida formato de teléfono colombiano y verifica propiedad del recurso antes de eliminar.
 */
public class NumeroUsuarioService {

    /** Gson compartido para serializar objetos NumeroUsuario en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los números telefónicos asociados a un usuario.
     * @param usuarioId ID del usuario cuyos números se consultan
     * @return JsonObject con la lista de números y código 200
     */
    public static JsonObject findByUsuarioId(int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Obtener la lista de números del usuario desde la base de datos
        List<NumeroUsuario> numeros = NumeroUsuarioDAO.findByUsuarioId(usuarioId);
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de números serializada como JSON
        respuesta.add("data", gson.toJsonTree(numeros));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Valida y crea un nuevo número telefónico asociado a un usuario.
     * El número debe tener formato válido según TELEFONO_REGEX (7-10 dígitos colombianos).
     * @param numero Número de teléfono a agregar
     * @param usuarioId ID del usuario al que se asociará el número
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String numero, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validar que el número no sea nulo ni vacío -----

        // Verificar que el número no sea nulo ni esté vacío
        if (numero == null || numero.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el número es obligatorio
            respuesta.addProperty("message", "El número de teléfono es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar formato del número telefónico -----

        // Verificar que el número cumpla con el formato colombiano (7-10 dígitos)
        if (!numero.trim().matches(ValidationHelper.TELEFONO_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando formato inválido
            respuesta.addProperty("message", "El teléfono debe tener entre 7 y 10 dígitos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar que el número no esté duplicado para este usuario -----

        // Verificar si el número ya está registrado para este usuario
        if (NumeroUsuarioDAO.existsByNumeroAndUsuarioId(numero.trim(), usuarioId)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el número ya existe
            respuesta.addProperty("message", "Este número de teléfono ya está registrado");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir el número -----

        // Construir el objeto NumeroUsuario con el número limpio y el ID del usuario
        NumeroUsuario nuevo = new NumeroUsuario(numero.trim(), usuarioId);
        // Persistir el nuevo número en la base de datos
        NumeroUsuario creado = NumeroUsuarioDAO.create(nuevo);

        // Verificar si hubo un error al insertar en la base de datos
        if (creado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al agregar el número");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Número agregado exitosamente");
        // Agregar los datos del número creado (ya con ID asignado) serializado como JSON
        respuesta.add("data", gson.toJsonTree(creado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el número creado
        return respuesta;
    }

    /**
     * Elimina un número telefónico de un usuario verificando propiedad del recurso.
     * Solo permite eliminar si el número pertenece al usuario autenticado.
     * @param idNumero ID del número a eliminar
     * @param usuarioId ID del usuario autenticado (para verificar propiedad)
     * @return JsonObject con el resultado de la eliminación
     */
    public static JsonObject delete(int idNumero, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el número existe -----

        // Buscar el número en la base de datos por su ID
        NumeroUsuario numero = NumeroUsuarioDAO.findById(idNumero);
        // Verificar si el número no existe
        if (numero == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Número no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar propiedad del recurso -----

        // Verificar que el número pertenezca al usuario autenticado
        if (numero.getUsuarioId() != usuarioId) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de acceso denegado
            respuesta.addProperty("message", "No tiene permiso para eliminar este número");
            // Código HTTP 403 Forbidden
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Eliminar el número -----

        // Ejecutar la eliminación en la base de datos
        if (!NumeroUsuarioDAO.delete(idNumero)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al eliminar el número");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la eliminación exitosa
        respuesta.addProperty("message", "Número eliminado exitosamente");
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta de éxito
        return respuesta;
    }
}
