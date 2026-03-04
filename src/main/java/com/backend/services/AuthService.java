package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.helpers.JwtHelper;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;
import com.google.gson.JsonObject;

public class AuthService {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    public static JsonObject validateLogin(String correo, String contrasena) {
        JsonObject respuesta = new JsonObject();

        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Correo y contraseña son requeridos");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        if (!correo.matches(EMAIL_REGEX)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El formato del correo no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        if (usuario == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Credenciales inválidas");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        if (usuario.getContrasena() == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesión con Google");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Credenciales inválidas");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        if (!usuario.isEstado()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            respuesta.addProperty("status", 403);
            return respuesta;
        }

        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        if (rol == null) rol = "Sin rol";

        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

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
