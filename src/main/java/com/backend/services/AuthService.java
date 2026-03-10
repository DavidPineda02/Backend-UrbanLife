// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para consultar usuarios por correo en la base de datos
import com.backend.dao.UsuarioDAO;
// Para generar el JWT una vez autenticado el usuario
import com.backend.helpers.JwtHelper;
// Para verificar la contraseña contra el hash BCrypt almacenado
import com.backend.helpers.PasswordHelper;
// Para las expresiones regulares y políticas de validación compartidas
import com.backend.helpers.ValidationHelper;
// Entidad que representa al usuario encontrado en la BD
import com.backend.models.Usuario;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio que contiene la lógica de validación y autenticación del login.
 * Maneja la validación de credenciales y generación de tokens JWT.
 * Centraliza toda la lógica de autenticación con correo y contraseña.
 */
public class AuthService {

    /**
     * Valida las credenciales y retorna un JWT si el login es exitoso.
     * Realiza validaciones de formato, compara con la BD y genera el token.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario en texto plano
     * @return JsonObject con el resultado de la autenticación y JWT si es exitoso
     */
    public static JsonObject validateLogin(String correo, String contrasena) {
        // Crear el objeto de respuesta que se retornará al controller
        JsonObject respuesta = new JsonObject();

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

        // Verificar que el correo no supere la longitud máxima permitida
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

        // Verificar longitud máxima (previene ataques de denegación de servicio con BCrypt)
        if (contrasena.length() > 128) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres
            respuesta.addProperty("message", "La contraseña no puede superar los 128 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Autenticación contra la base de datos -----

        // Buscar el usuario en la BD por correo electrónico
        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        // Usar mensaje genérico para no revelar si el correo existe o no (seguridad)
        if (usuario == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje genérico que no revela si el correo existe
            respuesta.addProperty("message", "Credenciales inválidas");
            // Código HTTP 401 Unauthorized
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Si la contraseña es null, la cuenta fue creada con Google y no tiene contraseña
        if (usuario.getContrasena() == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que debe usar Google para iniciar sesión
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesión con Google");
            // Código HTTP 401 Unauthorized
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que la contraseña ingresada coincida con el hash BCrypt almacenado
        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
            // Usar mensaje genérico para no revelar cuál de los dos campos es incorrecto
            respuesta.addProperty("success", false);
            // Mensaje genérico que no revela cuál campo está mal
            respuesta.addProperty("message", "Credenciales inválidas");
            // Código HTTP 401 Unauthorized
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que el usuario esté activo en el sistema (no desactivado por un admin)
        if (!usuario.isEstado()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la cuenta está inactiva
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            // Código HTTP 403 Forbidden
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Generación del JWT -----

        // Obtener el rol del usuario desde la tabla de relación usuario_rol
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        // Si por alguna razón no tiene rol asignado, usar "Sin rol" como valor por defecto
        if (rol == null) rol = "Sin rol";

        // Generar el JWT con el ID, correo y rol del usuario autenticado
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        // ----- Construir respuesta exitosa -----

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje de confirmación del login exitoso
        respuesta.addProperty("message", "Login exitoso");
        // Token JWT para que el frontend lo use en peticiones autenticadas
        respuesta.addProperty("token", token);
        // Nombre del usuario para mostrarlo en la interfaz
        respuesta.addProperty("nombre", usuario.getNombre());
        // Apellido del usuario para mostrarlo en la interfaz
        respuesta.addProperty("apellido", usuario.getApellido());
        // Correo del usuario para mostrarlo en la interfaz
        respuesta.addProperty("correo", usuario.getCorreo());
        // Rol del usuario para controlar permisos en el frontend
        respuesta.addProperty("rol", rol);
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);

        // Retornar la respuesta exitosa con el token y datos del usuario
        return respuesta;
    }
}
