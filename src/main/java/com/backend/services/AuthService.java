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

/**
 * Servicio que contiene la lógica de validación y autenticación del login.
 * Maneja la validación de credenciales y generación de tokens JWT.
 */
public class AuthService {

    /** Expresión regular para validar el formato del correo electrónico */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    /**
     * Valida las credenciales y retorna un JWT si el login es exitoso.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     * @return JsonObject con el resultado de la autenticación y JWT si es exitoso
     */
    public static JsonObject validateLogin(String correo, String contrasena) {
        JsonObject respuesta = new JsonObject();

        // Verificar que correo y contrasena no sean nulos ni vacios
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Correo y contraseña son requeridos");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar el formato del correo con la expresion regular
        if (!correo.matches(EMAIL_REGEX)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El formato del correo no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Buscar el usuario en la BD por correo
        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        // Respuesta generica para no revelar si el correo existe o no
        if (usuario == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Credenciales inválidas");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Si la contrasena es null, la cuenta fue creada con Google (no tiene contrasena)
        if (usuario.getContrasena() == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesión con Google");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Verificar que la contrasena ingresada coincida con el hash BCrypt almacenado
        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
            // Respuesta generica para no revelar cual de los dos campos es incorrecto
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Credenciales inválidas");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Verificar que el usuario este activo en el sistema
        if (!usuario.isEstado()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            respuesta.addProperty("status", 403);
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
        respuesta.addProperty("message", "Login exitoso");
        respuesta.addProperty("token", token);
        respuesta.addProperty("nombre", usuario.getNombre());
        respuesta.addProperty("correo", usuario.getCorreo());
        respuesta.addProperty("rol", rol);
        respuesta.addProperty("status", 200);

        return respuesta;
    }
}
