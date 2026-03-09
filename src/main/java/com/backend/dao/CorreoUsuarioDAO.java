package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.CorreoUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorreoUsuarioDAO {

    public static CorreoUsuario findById(int id) {
        String sql = "SELECT * FROM Correos_Usuario WHERE ID_CORREO = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error CorreoUsuarioDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<CorreoUsuario> findByUsuarioId(int usuarioId) {
        List<CorreoUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Correos_Usuario WHERE USUARIO_ID = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CorreoUsuarioDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<CorreoUsuario> findAll() {
        List<CorreoUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Correos_Usuario ORDER BY ID_CORREO ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CorreoUsuarioDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static CorreoUsuario create(CorreoUsuario correoUsuario) {
        String sql = "INSERT INTO Correos_Usuario (CORREO, USUARIO_ID) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, correoUsuario.getCorreo());
            consulta.setInt(2, correoUsuario.getUsuarioId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) correoUsuario.setIdCorreo(clavesGeneradas.getInt(1));
                return correoUsuario;
            }
        } catch (Exception excepcion) {
            System.out.println("Error CorreoUsuarioDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(CorreoUsuario correoUsuario) {
        String sql = "UPDATE Correos_Usuario SET CORREO = ? WHERE ID_CORREO = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, correoUsuario.getCorreo());
            consulta.setInt(2, correoUsuario.getIdCorreo());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CorreoUsuarioDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM Correos_Usuario WHERE ID_CORREO = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CorreoUsuarioDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static CorreoUsuario mapRow(ResultSet resultado) throws SQLException {
        return new CorreoUsuario(
                resultado.getInt("ID_CORREO"),
                resultado.getString("CORREO"),
                resultado.getInt("USUARIO_ID"));
    }
}
