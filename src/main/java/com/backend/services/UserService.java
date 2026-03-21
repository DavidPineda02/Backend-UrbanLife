// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para insertar el correo principal en Correos_Usuario tras crear el usuario
import com.backend.dao.CorreoUsuarioDAO;
// Para buscar el rol EMPLEADO y asignarlo al nuevo usuario
import com.backend.dao.RolDAO;
// Para operaciones CRUD de usuarios en la base de datos
import com.backend.dao.UsuarioDAO;
// Para crear el registro en la tabla de relación usuario-rol
import com.backend.dao.UsuarioRolDAO;
// Para generar el JWT al crear un usuario (auto-login)
import com.backend.helpers.JwtHelper;
// Para hashear la contraseña con BCrypt
import com.backend.helpers.PasswordHelper;
// Para las expresiones regulares y políticas de validación compartidas
import com.backend.helpers.ValidationHelper;
// Modelo que representa un correo electrónico asociado a un usuario
import com.backend.models.CorreoUsuario;
// Entidad del rol del sistema
import com.backend.models.Rol;
// Entidad del usuario del sistema
import com.backend.models.Usuario;
// Entidad de la relación usuario-rol
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

    /**
     * Valida, crea el usuario, le asigna rol EMPLEADO y retorna JWT (auto-login).
     * El usuario queda autenticado inmediatamente después del registro.
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario en texto plano
     * @return JsonObject con el resultado de la creación y JWT si es exitoso
     */
    public static JsonObject validateAndCreate(String nombre, String apellido, String correo, String contrasena) {
        // Crear el objeto de respuesta que se retornará al controller
        JsonObject respuesta = new JsonObject();

        // ----- Validaciones del campo Nombre -----

        // Verificar que el nombre no sea nulo ni esté vacío
        if (nombre == null || nombre.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el nombre es obligatorio
            respuesta.addProperty("message", "El nombre es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre tenga al menos 2 caracteres
        if (nombre.trim().length() < 2) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el mínimo de caracteres requeridos
            respuesta.addProperty("message", "El nombre debe tener al menos 2 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre no supere los 50 caracteres
        if (nombre.trim().length() > 50) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "El nombre no puede superar los 50 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre solo contenga letras (sin números ni caracteres especiales)
        if (!nombre.trim().matches(ValidationHelper.NOMBRE_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que solo se permiten letras
            respuesta.addProperty("message", "El nombre solo puede contener letras");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Apellido -----

        // Verificar que el apellido no sea nulo ni esté vacío
        if (apellido == null || apellido.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el apellido es obligatorio
            respuesta.addProperty("message", "El apellido es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el apellido tenga al menos 2 caracteres
        if (apellido.trim().length() < 2) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el mínimo de caracteres requeridos
            respuesta.addProperty("message", "El apellido debe tener al menos 2 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el apellido no supere los 50 caracteres
        if (apellido.trim().length() > 50) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "El apellido no puede superar los 50 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el apellido solo contenga letras (sin números ni caracteres especiales)
        if (!apellido.trim().matches(ValidationHelper.NOMBRE_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que solo se permiten letras
            respuesta.addProperty("message", "El apellido solo puede contener letras");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Correo -----

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
        // Verificar que el correo no supere la longitud máxima
        if (correo.length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres
            respuesta.addProperty("message", "El correo no puede superar los 100 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el correo tenga un formato válido usando la expresión regular compartida
        if (!correo.matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido del correo
            respuesta.addProperty("message", "El formato del correo no es válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Contraseña -----

        // Verificar que la contraseña no sea nula ni esté vacía
        if (contrasena == null || contrasena.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la contraseña es obligatoria
            respuesta.addProperty("message", "La contraseña es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la contraseña no supere la longitud máxima permitida por la política del sistema
        if (contrasena.length() > 20) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La contraseña no puede superar los 20 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la contraseña cumpla la política: min 8 chars, mayúscula, minúscula y número
        if (!contrasena.matches(ValidationHelper.PASSWORD_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje explicando los requisitos mínimos de la contraseña
            respuesta.addProperty("message", "La contraseña debe tener entre 8 y 20 caracteres, sin espacios, una mayúscula, una minúscula y un número");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del correo -----

        // Verificar que no exista otro usuario con el mismo correo en la BD
        if (UsuarioDAO.findByCorreo(correo) != null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el correo ya está en uso
            respuesta.addProperty("message", "El correo ya está registrado");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir el usuario -----

        // Construir el objeto Usuario con la contraseña hasheada y estado activo por defecto
        Usuario nuevoUsuario = new Usuario(nombre, apellido, correo,
                PasswordHelper.hashPassword(contrasena), true);

        // Persistir el nuevo usuario en la base de datos
        Usuario usuarioCreado = UsuarioDAO.create(nuevoUsuario);

        // Verificar si la creación del usuario fue exitosa
        if (usuarioCreado != null) {
            // Insertar el correo principal en la tabla Correos_Usuario con ES_PRINCIPAL=TRUE
            CorreoUsuarioDAO.create(new CorreoUsuario(correo, true, usuarioCreado.getIdUsuario()));
            // Asignar el correo al objeto usuario en memoria (campo de conveniencia)
            usuarioCreado.setCorreo(correo);

            // Buscar el rol EMPLEADO para asignárselo por defecto al nuevo usuario
            Rol rolEmpleado = RolDAO.findByNombre("EMPLEADO");
            // Valor por defecto si por algún motivo el rol no existe en la BD
            String nombreRol = "Sin rol";
            // Verificar que el rol EMPLEADO existe en la BD
            if (rolEmpleado != null) {
                // Crear el registro de relación usuario-rol en la tabla de unión
                UsuarioRolDAO.create(new UsuarioRol(usuarioCreado.getIdUsuario(), rolEmpleado.getIdRoles()));
                // Asignar el nombre del rol para incluirlo en el JWT y la respuesta
                nombreRol = rolEmpleado.getNombre();
            } else {
                // Si no existe el rol, imprimir advertencia en consola
                System.out.println("Advertencia: no se encontro el rol EMPLEADO para asignar al nuevo usuario");
            }

            // Generar JWT para hacer auto-login inmediatamente después del registro
            String token = JwtHelper.generateToken(usuarioCreado.getIdUsuario(), usuarioCreado.getCorreo(), nombreRol);

            // Indicar que la operación fue exitosa
            respuesta.addProperty("success", true);
            // Mensaje confirmando el registro exitoso
            respuesta.addProperty("message", "Usuario registrado exitosamente");
            // Token JWT para auto-login inmediato tras el registro
            respuesta.addProperty("token", token);
            // Nombre del usuario creado
            respuesta.addProperty("nombre", usuarioCreado.getNombre());
            // Apellido del usuario creado
            respuesta.addProperty("apellido", usuarioCreado.getApellido());
            // Correo del usuario creado
            respuesta.addProperty("correo", usuarioCreado.getCorreo());
            // Rol asignado al usuario creado
            respuesta.addProperty("rol", nombreRol);
            // Código HTTP 201 Created
            respuesta.addProperty("status", 201);
        } else {
            // Error al insertar el usuario en la base de datos
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al crear el usuario");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
        }

        // Retornar la respuesta (exitosa o de error)
        return respuesta;
    }

    /**
     * Valida y ejecuta la actualización completa de un usuario (PUT).
     * Nombre, apellido y correo son obligatorios; contraseña es opcional.
     * @param id Identificador del usuario a actualizar
     * @param nombre Nuevo nombre del usuario (obligatorio)
     * @param apellido Nuevo apellido del usuario (obligatorio)
     * @param correo Nuevo correo electrónico (obligatorio, debe ser único)
     * @param contrasena Nueva contraseña en texto plano (opcional, se hashea antes de guardar)
     * @param estado Nuevo estado activo/inactivo del usuario
     * @return JsonObject con el resultado de la actualización
     */
    public static JsonObject validateAndUpdate(int id, String nombre, String apellido, String correo, String contrasena, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el usuario existe -----

        // Buscar el usuario en la BD por su ID antes de intentar actualizar
        Usuario usuario = UsuarioDAO.findById(id);
        // Si el usuario no existe, retornar error 404
        if (usuario == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el usuario no fue encontrado
            respuesta.addProperty("message", "Usuario no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // En PUT, nombre, apellido y correo son obligatorios (actualización completa)
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank() || correo == null || correo.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando los campos obligatorios en PUT
            respuesta.addProperty("message", "Nombre, apellido y correo son obligatorios en PUT");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que el nuevo correo tenga un formato válido
        if (!correo.matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido del correo
            respuesta.addProperty("message", "El formato del correo no es válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar unicidad del correo solo si cambió respecto al correo actual del usuario
        if (!correo.equals(usuario.getCorreo())) {
            // El correo cambió: verificar que no esté en uso por otro usuario
            if (UsuarioDAO.findByCorreo(correo) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el correo ya está en uso
                respuesta.addProperty("message", "El correo ya está en uso por otro usuario");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // Validar la contraseña ANTES de persistir cualquier cambio (si fue proporcionada)
        if (contrasena != null && !contrasena.isBlank()) {
            // Verificar que la contraseña cumpla la política de seguridad
            if (!contrasena.matches(ValidationHelper.PASSWORD_REGEX)) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje explicando los requisitos mínimos de la contraseña
                respuesta.addProperty("message", "La contraseña debe tener entre 8 y 20 caracteres, sin espacios, una mayúscula, una minúscula y un número");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Aplicar cambios y persistir -----

        // Actualizar el nombre en el objeto usuario en memoria
        usuario.setNombre(nombre);
        // Actualizar el apellido en el objeto usuario en memoria
        usuario.setApellido(apellido);
        // Actualizar el estado en el objeto usuario en memoria
        usuario.setEstado(estado);

        // Persistir los cambios de nombre, apellido y estado en la base de datos
        if (!UsuarioDAO.update(usuario)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno al actualizar el usuario
            respuesta.addProperty("message", "Error al actualizar el usuario");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Actualizar el correo principal en Correos_Usuario si cambió
        if (!correo.equals(usuario.getCorreo())) {
            // Buscar el correo principal actual del usuario en Correos_Usuario
            CorreoUsuario correoPrincipal = CorreoUsuarioDAO.findByCorreo(usuario.getCorreo());
            // Si existe el correo principal, actualizar su dirección
            if (correoPrincipal != null) {
                // Asignar la nueva dirección de correo al objeto
                correoPrincipal.setCorreo(correo);
                // Persistir el cambio en la base de datos
                CorreoUsuarioDAO.update(correoPrincipal);
            }
        }

        // Hashear y guardar la nueva contraseña si fue proporcionada (ya validada arriba)
        if (contrasena != null && !contrasena.isBlank()) {
            // Actualizar la contraseña hasheada en la BD de forma separada
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        // ----- Construir respuesta exitosa -----

        // Obtener el usuario actualizado desde la BD para retornarlo en la respuesta
        Usuario usuarioActualizado = UsuarioDAO.findById(id);
        // Ocultar el hash de la contraseña antes de enviarlo al cliente
        usuarioActualizado.setContrasena(null);
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la actualización exitosa
        respuesta.addProperty("message", "Usuario actualizado exitosamente");
        // Agregar los datos actualizados del usuario serializados como JSON
        respuesta.add("data", gson.toJsonTree(usuarioActualizado));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);

        // Retornar la respuesta exitosa con los datos del usuario actualizado
        return respuesta;
    }

    /**
     * Actualización parcial de un usuario (PATCH): solo se actualizan los campos que vengan.
     * Permite modificar nombre, apellido, correo, contraseña y estado de forma independiente.
     * @param id ID del usuario a actualizar
     * @param nombre Nuevo nombre (opcional, null = no actualizar)
     * @param apellido Nuevo apellido (opcional, null = no actualizar)
     * @param correo Nuevo correo (opcional, null = no actualizar)
     * @param contrasena Nueva contraseña en texto plano (opcional, null = no actualizar)
     * @param estado Nuevo estado (opcional, null = no actualizar)
     * @return JsonObject con el resultado de la actualización parcial
     */
    public static JsonObject partialUpdate(int id, String nombre, String apellido, String correo, String contrasena, Boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el usuario existe -----

        // Buscar el usuario en la BD por su ID antes de intentar actualizar
        Usuario usuario = UsuarioDAO.findById(id);
        // Si el usuario no existe, retornar error 404
        if (usuario == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el usuario no fue encontrado
            respuesta.addProperty("message", "Usuario no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar el correo si fue enviado -----

        // Si el correo vino en el body, validarlo antes de continuar
        if (correo != null && !correo.isBlank()) {
            // Verificar que el nuevo correo tenga un formato válido
            if (!correo.matches(ValidationHelper.EMAIL_REGEX)) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando el formato inválido del correo
                respuesta.addProperty("message", "El formato del correo no es válido");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
            // Verificar unicidad del correo solo si cambió respecto al correo actual del usuario
            if (!correo.equals(usuario.getCorreo()) && UsuarioDAO.findByCorreo(correo) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el correo ya está en uso
                respuesta.addProperty("message", "El correo ya está en uso por otro usuario");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // Validar la contraseña ANTES de persistir cualquier cambio (si fue proporcionada)
        if (contrasena != null && !contrasena.isBlank()) {
            // Verificar que la contraseña cumpla la política de seguridad
            if (!contrasena.matches(ValidationHelper.PASSWORD_REGEX)) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje explicando los requisitos mínimos de la contraseña
                respuesta.addProperty("message", "La contraseña debe tener entre 8 y 20 caracteres, sin espacios, una mayúscula, una minúscula y un número");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Aplicar solo los campos que no sean nulos ni vacíos -----

        // Actualizar el nombre solo si fue enviado en el body
        if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);
        // Actualizar el apellido solo si fue enviado en el body
        if (apellido != null && !apellido.isBlank()) usuario.setApellido(apellido);
        // Actualizar el estado solo si fue enviado en el body (Boolean objeto permite null = no actualizar)
        if (estado != null) usuario.setEstado(estado);

        // Persistir los cambios de nombre, apellido y estado en la base de datos
        if (!UsuarioDAO.update(usuario)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno al actualizar el usuario
            respuesta.addProperty("message", "Error al actualizar el usuario");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Actualizar el correo principal en Correos_Usuario si fue enviado y cambió
        if (correo != null && !correo.isBlank() && !correo.equals(usuario.getCorreo())) {
            // Buscar el correo principal actual del usuario en Correos_Usuario
            CorreoUsuario correoPrincipal = CorreoUsuarioDAO.findByCorreo(usuario.getCorreo());
            // Si existe el correo principal, actualizar su dirección
            if (correoPrincipal != null) {
                // Asignar la nueva dirección de correo al objeto
                correoPrincipal.setCorreo(correo);
                // Persistir el cambio en la base de datos
                CorreoUsuarioDAO.update(correoPrincipal);
            }
        }

        // Hashear y guardar la nueva contraseña si fue proporcionada (ya validada arriba)
        if (contrasena != null && !contrasena.isBlank()) {
            // Actualizar la contraseña hasheada en la BD de forma separada
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        // ----- Construir respuesta exitosa -----

        // Obtener el usuario actualizado desde la BD para retornarlo en la respuesta
        Usuario usuarioActualizado = UsuarioDAO.findById(id);
        // Ocultar el hash de la contraseña antes de enviarlo al cliente
        usuarioActualizado.setContrasena(null);
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la actualización parcial exitosa
        respuesta.addProperty("message", "Usuario actualizado parcialmente");
        // Agregar los datos actualizados del usuario serializados como JSON
        respuesta.add("data", gson.toJsonTree(usuarioActualizado));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);

        // Retornar la respuesta exitosa con los datos del usuario actualizado
        return respuesta;
    }

    // ============================================================
    // DELETE (soft delete) — Desactivar usuario (estado = false)
}
