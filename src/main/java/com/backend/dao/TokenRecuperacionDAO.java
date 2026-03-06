package com.backend.dao;

import com.backend.config.dbConnection;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con tokens de recuperación de contraseña en la base de datos.
 */
public class TokenRecuperacionDAO {

    /**
     * Genera y guarda un nuevo token de recuperación para un usuario.
     * El token original se envía al usuario por correo; en la BD solo se guarda su hash SHA-256.
     * @param usuarioId ID del usuario que solicita la recuperación
     * @param fechaExpiracion Fecha y hora de expiración del token
     * @return Token original generado (para enviarlo por correo), o null si falló la inserción
     */
    public static String guardarToken(int usuarioId, LocalDateTime fechaExpiracion) {
        // Generar el UUID que se enviara al usuario por correo
        String token = UUID.randomUUID().toString();
        // Guardar solo el hash SHA-256 en la BD (no el token en texto plano)
        String tokenHash = sha256(token);
        String sql = "INSERT INTO token_recuperacion (usuario_id, token, fecha_expiracion) VALUES (?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            consulta.setString(2, tokenHash);
            consulta.setObject(3, fechaExpiracion);
            consulta.executeUpdate();
            // Retornar el token original (no el hash) para enviarlo por correo
            return token;
        } catch (Exception excepcion) {
            System.out.println("Error TokenRecuperacionDAO.guardarToken: " + excepcion.getMessage());
        }
        return null;
    }

    public static JsonObject validarToken(String token) {
        JsonObject respuesta = new JsonObject();
        // Comparar contra el hash SHA-256 del token recibido
        String sql = "SELECT id_token, usuario_id, fecha_expiracion FROM token_recuperacion " +
                    "WHERE token = ? AND usado = FALSE";
        try (Connection conexion = dbConnection.getConnection();
            PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, sha256(token));
            ResultSet resultado = consulta.executeQuery();

            if (resultado.next()) {
                LocalDateTime fechaExpiracion = resultado.getObject("fecha_expiracion", LocalDateTime.class);

                if (LocalDateTime.now().isAfter(fechaExpiracion)) {
                    respuesta.addProperty("success", false);
                    respuesta.addProperty("message", "El token ha expirado");
                    respuesta.addProperty("status", 400);
                } else {
                    respuesta.addProperty("success", true);
                    respuesta.addProperty("idToken", resultado.getInt("id_token"));
                    respuesta.addProperty("usuarioId", resultado.getInt("usuario_id"));
                    respuesta.addProperty("status", 200);
                }
            } else {
                respuesta.addProperty("success", false);
                respuesta.addProperty("message", "Token invalido o ya utilizado");
                respuesta.addProperty("status", 400);
            }
        } catch (Exception excepcion) {
            System.out.println("Error TokenRecuperacionDAO.validarToken: " + excepcion.getMessage());
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error del servidor");
            respuesta.addProperty("status", 500);
        }
        return respuesta;
    }

    public static boolean marcarTokenUsado(int idToken) {
        String sql = "UPDATE token_recuperacion SET usado = TRUE WHERE id_token = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idToken);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error TokenRecuperacionDAO.marcarTokenUsado: " + excepcion.getMessage());
        }
        return false;
    }

    /**
     * Genera el hash SHA-256 de un String en hexadecimal.
     * Se usa para no almacenar tokens de recuperacion en texto plano en la BD.
     * @param input Texto a hashear
     * @return Hash SHA-256 en hexadecimal
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear el token", e);
        }
    }
}
