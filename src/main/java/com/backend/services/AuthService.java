 package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.helpers.JwtHelper;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;
import com.google.gson.JsonObject;

public class AuthService {

    public static JsonObject validateLogin(String correo, String contrasena) {
        JsonObject response = new JsonObject();

        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            response.addProperty("success", false);
            response.addProperty("message", "Correo y contraseña son requeridos");
            response.addProperty("status", 400);
            return response;
        }

        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        if (usuario == null) {
            response.addProperty("success", false);
            response.addProperty("message", "Credenciales inválidas");
            response.addProperty("status", 401);
            return response;
        }

        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
            response.addProperty("success", false);
            response.addProperty("message", "Credenciales inválidas");
            response.addProperty("status", 401);
            return response;
        }

        if (!usuario.isEstado()) {
            response.addProperty("success", false);
            response.addProperty("message", "Usuario inactivo. Contacte al administrador");
            response.addProperty("status", 403);
            return response;
        }

        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        if (rol == null) rol = "Sin rol";

        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        response.addProperty("success", true);
        response.addProperty("message", "Login exitoso");
        response.addProperty("token", token);
        response.addProperty("nombre", usuario.getNombre());
        response.addProperty("correo", usuario.getCorreo());
        response.addProperty("rol", rol);
        response.addProperty("status", 200);

        return response;
    }
}
