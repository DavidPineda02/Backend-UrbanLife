// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para consultar usuarios por correo e id
import com.backend.dao.UsuarioDAO;
// Para generar el JWT una vez autenticado el usuario
import com.backend.helpers.JwtHelper;
// Para verificar la contrasena contra el hash BCrypt almacenado
import com.backend.helpers.PasswordHelper;
// Entidad que representa al usuario encontrado en la BD
import com.backend.models.Usuario;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;
// Para las expresiones regulares y políticas de validación compartidas
import com.backend.helpers.ValidationHelper;

/**
 * Servicio que contiene la lógica de validación y autenticación del login.
 * Maneja la validación de credenciales y generación de tokens JWT.
 * Centraliza toda la lógica de autenticación del sistema.
 */
public class AuthService {

    /**
     * Valida las credenciales y retorna un JWT si el login es exitoso.
     * Realiza validaciones completas de seguridad y genera token.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     * @return JsonObject con el resultado de la autenticación y JWT si es exitoso
     */
    public static JsonObject validateLogin(String correo, String contrasena) {
        // Crear objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Validar correo: requerido
        if (correo == null || correo.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo es requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar correo: longitud máxima
        if (correo.length() > 100) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo no puede superar los 100 caracteres");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar correo: formato
        if (!correo.matches(ValidationHelper.EMAIL_REGEX)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El formato del correo no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar contraseña: requerida
        if (contrasena == null || contrasena.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La contraseña es requerida");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar contraseña: longitud máxima (previene ataques de denegación de servicio con BCrypt)
        if (contrasena.length() > 128) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La contraseña no puede superar los 128 caracteres");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Buscar el usuario en la BD por correo
        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        // Respuesta generica para no revelar si el correo existe o no
        if (usuario == null) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje genérico de seguridad
            respuesta.addProperty("message", "Credenciales inválidas");
            // Código HTTP 401
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Si la contrasena es null, la cuenta fue creada con Google (no tiene contrasena)
        if (usuario.getContrasena() == null) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje específico
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesión con Google");
            // Código HTTP 401
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que la contrasena ingresada coincida con el hash BCrypt almacenado
        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
            // Respuesta generica para no revelar cual de los dos campos es incorrecto
            respuesta.addProperty("success", false);
            // Mensaje genérico de seguridad
            respuesta.addProperty("message", "Credenciales inválidas");
            // Código HTTP 401
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que el usuario este activo en el sistema
        if (!usuario.isEstado()) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de inactividad
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            // Código HTTP 403
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // Obtener el rol del usuario desde la tabla de relacion usuarios_roles
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        // Si por alguna razon no tiene rol asignado, usar "Sin rol" como fallback
        if (rol == null) rol = "Sin rol";

        // Generar el JWT con el id, correo y rol del usuario autenticado
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        // Construir la respuesta exitosa con el token y datos del usuario
        respuesta.addProperty("success", true);
        // Mensaje de éxito
        respuesta.addProperty("message", "Login exitoso");
        // Token JWT
        respuesta.addProperty("token", token);
        // Nombre del usuario
        respuesta.addProperty("nombre", usuario.getNombre());
        // Apellido del usuario
        respuesta.addProperty("apellido", usuario.getApellido());
        // Correo del usuario
        respuesta.addProperty("correo", usuario.getCorreo());
        // Rol del usuario
        respuesta.addProperty("rol", rol);
        // Código HTTP 200
        respuesta.addProperty("status", 200);

        // Retornar respuesta exitosa
        return respuesta;
    }
}
