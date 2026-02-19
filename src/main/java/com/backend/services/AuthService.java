package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.dto.LoginRequest;
import com.backend.dto.LoginResponse;
import com.backend.helpers.JwtHelper;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginResponse login(LoginRequest request) {
        if (!request.isValid())
            return new LoginResponse(false, "Correo y contraseña son requeridos");

        Usuario usuario = usuarioDAO.findByCorreo(request.getCorreo());
        if (usuario == null)
            return new LoginResponse(false, "Credenciales inválidas");

        if (!PasswordHelper.checkPassword(request.getContrasena(), usuario.getContrasena()))
            return new LoginResponse(false, "Credenciales inválidas");

        if (!"Activo".equalsIgnoreCase(usuario.getEstado()))
            return new LoginResponse(false, "Usuario inactivo. Contacte al administrador");

        String rol = usuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        if (rol == null) rol = "Sin rol";

        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        return new LoginResponse(true, "Login exitoso", token, usuario.getNombre(), usuario.getCorreo(), rol);
    }
}
