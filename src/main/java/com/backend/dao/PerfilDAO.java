package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Perfil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerfilDAO {

    public static Perfil findById(int id) {
        String sql = "SELECT * FROM perfil WHERE id_perfil = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error PerfilDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static Perfil findByUsuarioId(int usuarioId) {
        String sql = "SELECT * FROM perfil WHERE usuario_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error PerfilDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Perfil> findAll() {
        List<Perfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM perfil ORDER BY id_perfil ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error PerfilDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Perfil create(Perfil perfil) {
        String sql = "INSERT INTO perfil (nombre_empresa, direccion, ciudad, usuario_id) VALUES (?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, perfil.getNombreEmpresa());
            consulta.setString(2, perfil.getDireccion());
            consulta.setString(3, perfil.getCiudad());
            consulta.setInt(4, perfil.getUsuarioId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) perfil.setIdPerfil(clavesGeneradas.getInt(1));
                return perfil;
            }
        } catch (Exception excepcion) {
            System.out.println("Error PerfilDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Perfil perfil) {
        String sql = "UPDATE perfil SET nombre_empresa = ?, direccion = ?, ciudad = ?, usuario_id = ? WHERE id_perfil = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, perfil.getNombreEmpresa());
            consulta.setString(2, perfil.getDireccion());
            consulta.setString(3, perfil.getCiudad());
            consulta.setInt(4, perfil.getUsuarioId());
            consulta.setInt(5, perfil.getIdPerfil());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error PerfilDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM perfil WHERE id_perfil = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error PerfilDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static Perfil mapRow(ResultSet resultado) throws SQLException {
        return new Perfil(
                resultado.getInt("id_perfil"),
                resultado.getString("nombre_empresa"),
                resultado.getString("direccion"),
                resultado.getString("ciudad"),
                resultado.getInt("usuario_id"));
    }
}
