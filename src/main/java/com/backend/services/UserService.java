// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para buscar el rol EMPLEADO y asignarlo al nuevo usuario
import com.backend.dao.RolDAO;
// Para operaciones CRUD de usuarios en la base de datos
import com.backend.dao.UsuarioDAO;
// Para crear el registro en la tabla de relacion usuario-rol
import com.backend.dao.UsuarioRolDAO;
// Para generar el JWT al crear un usuario (auto-login)
import com.backend.helpers.JwtHelper;
// Para hashear la contrasena con BCrypt
import com.backend.helpers.PasswordHelper;
// Entidad del rol del sistema
import com.backend.models.Rol;
// Entidad del usuario del sistema
import com.backend.models.Usuario;
// Entidad de la relacion usuario-rol
import com.backend.models.UsuarioRol;
// Para serializar objetos Java a JSON (respuesta con data)
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el CRUD de usuarios.
 * Maneja la creación, actualización y gestión de usuarios con sus roles.
 * Centraliza todas las operaciones de gestión de usuarios.
 */
public class UserService {

    /** Gson compartido para serializar objetos Usuario en la respuesta */
    private static final Gson gson = new Gson();

    /** Expresión regular para validar el formato del correo electrónico */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    /** Política de contraseña: min 8 chars, al menos una mayúscula, una minúscula y un número */
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    /**
     * Valida, crea el usuario, le asigna rol EMPLEADO y retorna JWT (auto-login).
     * Realiza validaciones completas y crea usuario con rol por defecto.
     * @param nombre Nombre del usuario
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     * @return JsonObject con el resultado de la creación y JWT si es exitoso
     */
    public static JsonObject validateAndCreate(String nombre, String correo, String contrasena) {
        // Crear objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Validar que los tres campos obligatorios no sean nulos ni vacios
        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de error
            respuesta.addProperty("message", "Nombre, correo y contraseña son requeridos");
            // Código HTTP 400
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Validar el formato del correo con regex
        if (!correo.matches(EMAIL_REGEX)) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de error
            respuesta.addProperty("message", "El formato del correo no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar que la contrasena cumpla la politica de seguridad
        if (!contrasena.matches(PASSWORD_REGEX)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Verificar unicidad del correo: no puede existir otro usuario con el mismo
        if (UsuarioDAO.findByCorreo(correo) != null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo ya está registrado");
            respuesta.addProperty("status", 409);
            return respuesta;
        }

        // Crear la entidad usuario con la contrasena hasheada y estado activo
        Usuario nuevoUsuario = new Usuario(nombre, correo,
                PasswordHelper.hashPassword(contrasena), true);

        // Persistir el usuario en la base de datos
        Usuario usuarioCreado = UsuarioDAO.create(nuevoUsuario);

        if (usuarioCreado != null) {
            // Buscar el rol EMPLEADO para asignarselo por defecto al nuevo usuario
            Rol rolEmpleado = RolDAO.findByNombre("EMPLEADO");
            String nombreRol = "Sin rol"; // Fallback si por algun motivo no existe el rol
            if (rolEmpleado != null) {
                // Crear el registro de relacion usuario-rol en la tabla de union
                UsuarioRolDAO.create(new UsuarioRol(usuarioCreado.getIdUsuario(), rolEmpleado.getIdRoles()));
                nombreRol = rolEmpleado.getNombre(); // "EMPLEADO"
            } else {
                System.out.println("Advertencia: no se encontro el rol EMPLEADO para asignar al nuevo usuario");
            }

            // Generar JWT para hacer auto-login inmediatamente despues del registro
            String token = JwtHelper.generateToken(usuarioCreado.getIdUsuario(), usuarioCreado.getCorreo(), nombreRol);

            // Construir respuesta exitosa con 201 Created
            respuesta.addProperty("success", true);
            respuesta.addProperty("message", "Usuario registrado exitosamente");
            respuesta.addProperty("token", token);
            respuesta.addProperty("nombre", usuarioCreado.getNombre());
            respuesta.addProperty("correo", usuarioCreado.getCorreo());
            respuesta.addProperty("rol", nombreRol);
            respuesta.addProperty("status", 201);
        } else {
            // Error al insertar en la base de datos
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al crear el usuario");
            respuesta.addProperty("status", 500);
        }

        return respuesta;
    }

    /**
     * Valida y ejecuta la actualización completa de un usuario (PUT).
     * Nombre y correo son obligatorios; contraseña es opcional (se hashea si se proporciona).
     * @param id Identificador del usuario a actualizar
     * @param nombre Nuevo nombre completo del usuario (obligatorio)
     * @param correo Nuevo correo electrónico del usuario (obligatorio, debe ser único)
     * @param contrasena Nueva contraseña en texto plano (opcional, se hashea antes de guardar)
     * @param estado Nuevo estado activo/inactivo del usuario
     * @return JsonObject con success, message y status del resultado de la operación
     */
    public static JsonObject validateAndUpdate(int id, String nombre, String correo, String contrasena, boolean estado) {
        JsonObject respuesta = new JsonObject();

        // Verificar que el usuario existe antes de intentar actualizar
        Usuario usuario = UsuarioDAO.findById(id);
        if (usuario == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario no encontrado");
            respuesta.addProperty("status", 404);
            return respuesta;
        }

        // En PUT, nombre y correo son obligatorios (reemplazo completo)
        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Nombre y correo son obligatorios en PUT");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar el formato del nuevo correo
        if (!correo.matches(EMAIL_REGEX)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El formato del correo no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Solo verificar unicidad del correo si cambio respecto al actual
        if (!correo.equals(usuario.getCorreo())) {
            if (UsuarioDAO.findByCorreo(correo) != null) {
                respuesta.addProperty("success", false);
                respuesta.addProperty("message", "El correo ya está en uso por otro usuario");
                respuesta.addProperty("status", 409);
                return respuesta;
            }
        }

        // Actualizar los campos del objeto usuario en memoria
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setEstado(estado);

        // Persistir los cambios en la base de datos
        if (!UsuarioDAO.update(usuario)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al actualizar el usuario");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // Si se envio una nueva contrasena, hashearla y actualizarla por separado
        if (contrasena != null && !contrasena.isBlank()) {
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        // Obtener el usuario actualizado desde la BD para retornarlo en la respuesta
        Usuario usuarioActualizado = UsuarioDAO.findById(id);
        usuarioActualizado.setContrasena(null); // No exponer el hash al cliente
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Usuario actualizado exitosamente");
        respuesta.add("data", gson.toJsonTree(usuarioActualizado)); // Serializar el usuario actualizado
        respuesta.addProperty("status", 200);

        return respuesta;
    }

    /**
     * Actualización parcial: solo se actualizan los campos que vengan (no nulos).
     * Permite modificar nombre, correo, contraseña y estado de forma independiente.
     * @param id ID del usuario a actualizar
     * @param nombre Nuevo nombre (opcional)
     * @param correo Nuevo correo (opcional)
     * @param contrasena Nueva contraseña (opcional)
     * @param estado Nuevo estado (opcional)
     * @return JsonObject con el resultado de la actualización parcial
     */
    public static JsonObject partialUpdate(int id, String nombre, String correo, String contrasena, Boolean estado) {
        JsonObject respuesta = new JsonObject();

        // Verificar que el usuario existe antes de intentar actualizar
        Usuario usuario = UsuarioDAO.findById(id);
        if (usuario == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario no encontrado");
            respuesta.addProperty("status", 404);
            return respuesta;
        }

        // Si el correo vino en el body, validarlo
        if (correo != null && !correo.isBlank()) {
            if (!correo.matches(EMAIL_REGEX)) {
                respuesta.addProperty("success", false);
                respuesta.addProperty("message", "El formato del correo no es válido");
                respuesta.addProperty("status", 400);
                return respuesta;
            }
            // Verificar unicidad solo si el correo cambio respecto al actual
            if (!correo.equals(usuario.getCorreo()) && UsuarioDAO.findByCorreo(correo) != null) {
                respuesta.addProperty("success", false);
                respuesta.addProperty("message", "El correo ya está en uso por otro usuario");
                respuesta.addProperty("status", 409);
                return respuesta;
            }
        }

        // Actualizar solo los campos que no sean nulos ni vacios
        if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);   // Actualizar nombre si vino
        if (correo != null && !correo.isBlank()) usuario.setCorreo(correo);   // Actualizar correo si vino
        if (estado != null) usuario.setEstado(estado);                         // Actualizar estado si vino

        // Persistir los cambios en la base de datos
        if (!UsuarioDAO.update(usuario)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al actualizar el usuario");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // Si se envio una nueva contrasena, hashearla y actualizarla por separado
        if (contrasena != null && !contrasena.isBlank()) {
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        // Obtener el usuario actualizado desde la BD para retornarlo
        Usuario usuarioActualizado = UsuarioDAO.findById(id);
        usuarioActualizado.setContrasena(null); // No exponer el hash al cliente
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Usuario actualizado parcialmente");
        respuesta.add("data", gson.toJsonTree(usuarioActualizado)); // Serializar el usuario actualizado
        respuesta.addProperty("status", 200);

        return respuesta;
    }

    // ============================================================
    // DELETE (soft delete) — Desactivar usuario (estado = false)
    // Comentado: se maneja el estado por true/false, no se elimina de la BD
    // ============================================================
    // public static JsonObject deleteUser(int id, String idUsuarioToken) {
    //     JsonObject respuesta = new JsonObject();
    //
    //     // Evitar que el admin se desactive a si mismo por accidente
    //     if (idUsuarioToken.equals(String.valueOf(id))) {
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "No puedes desactivar tu propia cuenta");
    //         respuesta.addProperty("status", 403);
    //         return respuesta;
    //     }
    //
    //     // Verificar que el usuario a desactivar existe en la BD
    //     Usuario usuario = UsuarioDAO.findById(id);
    //     if (usuario == null) {
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "Usuario no encontrado");
    //         respuesta.addProperty("status", 404);
    //         return respuesta;
    //     }
    //
    //     // No tiene sentido desactivar un usuario que ya esta inactivo
    //     if (!usuario.isEstado()) {
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "El usuario ya está inactivo");
    //         respuesta.addProperty("status", 409);
    //         return respuesta;
    //     }
    //
    //     // Proteger las cuentas SUPER_ADMIN de ser desactivadas
    //     String rolUsuario = UsuarioDAO.findRolByUsuarioId(id);
    //     if ("SUPER_ADMIN".equalsIgnoreCase(rolUsuario)) {
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "No se puede desactivar a un SUPER_ADMIN");
    //         respuesta.addProperty("status", 403);
    //         return respuesta;
    //     }
    //
    //     // Ejecutar el soft delete: actualizar estado = false en la BD
    //     if (UsuarioDAO.updateStatus(id)) {
    //         respuesta.addProperty("success", true);
    //         respuesta.addProperty("message", "Usuario desactivado exitosamente");
    //         respuesta.addProperty("status", 200);
    //     } else {
    //         // Error al ejecutar el UPDATE en la BD
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "Error al desactivar el usuario");
    //         respuesta.addProperty("status", 500);
    //     }
    //
    //     return respuesta;
    // }
}
