package com.backend.services;

import com.backend.dao.TokenRecuperacionDAO;
import com.backend.dao.UsuarioDAO;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;
import com.google.gson.JsonObject;

import java.time.LocalDateTime;

public class PasswordResetService {

    public static JsonObject solicitarRecuperacion(String correo) {
        JsonObject respuesta = new JsonObject();

        if (correo == null || correo.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo es requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        if (usuario == null) {
            // Respuesta genérica para no revelar si el correo existe
            respuesta.addProperty("success", true);
            respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
            respuesta.addProperty("status", 200);
            return respuesta;
        }

        // Usuarios de Google no tienen contraseña, no pueden recuperar
        if (usuario.getContrasena() == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesion con Google");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        if (!usuario.isEstado()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            respuesta.addProperty("status", 403);
            return respuesta;
        }

        LocalDateTime expiracion = LocalDateTime.now().plusHours(1);
        String token = TokenRecuperacionDAO.guardarToken(usuario.getIdUsuario(), expiracion);

        if (token == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al generar el token");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        boolean correoEnviado = EmailService.enviarCorreoRecuperacion(correo, token);

        if (!correoEnviado) {
            System.out.println("==============================");
            System.out.println("LINK RECUPERACION (fallo email):");
            System.out.println("http://localhost:5500/reset-password.html?token=" + token);
            System.out.println("==============================");
        }

        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
        respuesta.addProperty("status", 200);
        return respuesta;
    }

    public static JsonObject validarToken(String token) {
        JsonObject respuesta = new JsonObject();

        if (token == null || token.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El token es requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        return TokenRecuperacionDAO.validarToken(token);
    }

    public static JsonObject cambiarContrasena(String token, String nuevaContrasena) {
        JsonObject respuesta = new JsonObject();

        if (token == null || token.isBlank() || nuevaContrasena == null || nuevaContrasena.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token y nueva contrasena son requeridos");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número
        if (!nuevaContrasena.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        JsonObject validacion = TokenRecuperacionDAO.validarToken(token);
        if (!validacion.get("success").getAsBoolean()) {
            return validacion;
        }

        int usuarioId = validacion.get("usuarioId").getAsInt();
        int idToken   = validacion.get("idToken").getAsInt();

        String hashNuevaContrasena = PasswordHelper.hashPassword(nuevaContrasena);
        boolean actualizado = TokenRecuperacionDAO.actualizarContrasena(usuarioId, hashNuevaContrasena);

        if (!actualizado) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al actualizar la contrasena");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        TokenRecuperacionDAO.marcarTokenUsado(idToken);

        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Contrasena actualizada correctamente");
        respuesta.addProperty("status", 200);
        return respuesta;
    }
}
