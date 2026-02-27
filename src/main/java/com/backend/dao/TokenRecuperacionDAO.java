package com.backend.dao;

import com.backend.config.dbConnection;
import com.google.gson.JsonObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class TokenRecuperacionDAO {

    public static String guardarToken(int usuarioId, LocalDateTime fechaExpiracion) {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO token_recuperacion (usuario_id, token, fecha_expiracion) VALUES (?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            consulta.setString(2, token);
            consulta.setObject(3, fechaExpiracion);
            consulta.executeUpdate();
            return token;
        } catch (Exception excepcion) {
            System.out.println("Error TokenRecuperacionDAO.guardarToken: " + excepcion.getMessage());
        }
        return null;
    }

    public static JsonObject validarToken(String token) {
        JsonObject respuesta = new JsonObject();
        String sql = "SELECT id_token, usuario_id, fecha_expiracion FROM token_recuperacion " +
                     "WHERE token = ? AND usado = FALSE";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, token);
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

    public static boolean actualizarContrasena(int usuarioId, String hashContrasena) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, hashContrasena);
            consulta.setInt(2, usuarioId);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error TokenRecuperacionDAO.actualizarContrasena: " + excepcion.getMessage());
        }
        return false;
    }
}
