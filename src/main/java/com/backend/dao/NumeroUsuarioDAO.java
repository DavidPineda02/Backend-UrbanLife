package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.NumeroUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NumeroUsuarioDAO {

    public static NumeroUsuario findById(int id) {
        String sql = "SELECT * FROM Numeros_Usuario WHERE ID_NUMERO = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error NumeroUsuarioDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<NumeroUsuario> findByUsuarioId(int usuarioId) {
        List<NumeroUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Numeros_Usuario WHERE USUARIO_ID = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error NumeroUsuarioDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<NumeroUsuario> findAll() {
        List<NumeroUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Numeros_Usuario ORDER BY ID_NUMERO ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error NumeroUsuarioDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static NumeroUsuario create(NumeroUsuario numeroUsuario) {
        String sql = "INSERT INTO Numeros_Usuario (NUMERO, USUARIO_ID) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, numeroUsuario.getNumero());
            consulta.setInt(2, numeroUsuario.getUsuarioId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) numeroUsuario.setIdNumero(clavesGeneradas.getInt(1));
                return numeroUsuario;
            }
        } catch (Exception excepcion) {
            System.out.println("Error NumeroUsuarioDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(NumeroUsuario numeroUsuario) {
        String sql = "UPDATE Numeros_Usuario SET NUMERO = ? WHERE ID_NUMERO = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, numeroUsuario.getNumero());
            consulta.setInt(2, numeroUsuario.getIdNumero());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error NumeroUsuarioDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM Numeros_Usuario WHERE ID_NUMERO = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error NumeroUsuarioDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static NumeroUsuario mapRow(ResultSet resultado) throws SQLException {
        return new NumeroUsuario(
                resultado.getInt("ID_NUMERO"),
                resultado.getString("NUMERO"),
                resultado.getInt("USUARIO_ID"));
    }
}
