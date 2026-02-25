package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.CorreoPerfil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorreoPerfilDAO {

    public static CorreoPerfil findById(int id) {
        String sql = "SELECT * FROM correos_perfil WHERE id_correos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error CorreoPerfilDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<CorreoPerfil> findByPerfilId(int perfilId) {
        List<CorreoPerfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM correos_perfil WHERE perfil_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, perfilId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CorreoPerfilDAO.findByPerfilId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<CorreoPerfil> findAll() {
        List<CorreoPerfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM correos_perfil ORDER BY id_correos ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CorreoPerfilDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static CorreoPerfil create(CorreoPerfil correoPerfil) {
        String sql = "INSERT INTO correos_perfil (correo, perfil_id) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, correoPerfil.getCorreo());
            consulta.setInt(2, correoPerfil.getPerfilId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) correoPerfil.setIdCorreos(clavesGeneradas.getInt(1));
                return correoPerfil;
            }
        } catch (Exception excepcion) {
            System.out.println("Error CorreoPerfilDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(CorreoPerfil correoPerfil) {
        String sql = "UPDATE correos_perfil SET correo = ?, perfil_id = ? WHERE id_correos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, correoPerfil.getCorreo());
            consulta.setInt(2, correoPerfil.getPerfilId());
            consulta.setInt(3, correoPerfil.getIdCorreos());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CorreoPerfilDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM correos_perfil WHERE id_correos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CorreoPerfilDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static CorreoPerfil mapRow(ResultSet resultado) throws SQLException {
        return new CorreoPerfil(
                resultado.getInt("id_correos"),
                resultado.getString("correo"),
                resultado.getInt("perfil_id"));
    }
}
