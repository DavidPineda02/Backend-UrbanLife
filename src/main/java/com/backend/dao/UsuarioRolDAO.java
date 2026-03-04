package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.UsuarioRol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con las relaciones usuario-rol en la base de datos.
 */
public class UsuarioRolDAO {

    /**
     * Busca una relación usuario-rol por su ID.
     * @param id ID de la relación a buscar
     * @return Relación UsuarioRol encontrada o null si no existe
     */
    public static UsuarioRol findById(int id) {
        String sql = "SELECT * FROM usuario_rol WHERE id_usuario_rol = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<UsuarioRol> findByUsuarioId(int usuarioId) {
        List<UsuarioRol> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario_rol WHERE usuario_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<UsuarioRol> findAll() {
        List<UsuarioRol> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario_rol ORDER BY id_usuario_rol ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static UsuarioRol create(UsuarioRol usuarioRol) {
        String sql = "INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setInt(1, usuarioRol.getUsuarioId());
            consulta.setInt(2, usuarioRol.getRolId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) usuarioRol.setIdUsuarioRol(clavesGeneradas.getInt(1));
                return usuarioRol;
            }
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(UsuarioRol usuarioRol) {
        String sql = "UPDATE usuario_rol SET usuario_id = ?, rol_id = ? WHERE id_usuario_rol = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioRol.getUsuarioId());
            consulta.setInt(2, usuarioRol.getRolId());
            consulta.setInt(3, usuarioRol.getIdUsuarioRol());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM usuario_rol WHERE id_usuario_rol = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean deleteByUsuarioId(int usuarioId) {
        String sql = "DELETE FROM usuario_rol WHERE usuario_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioRolDAO.deleteByUsuarioId: " + excepcion.getMessage());
        }
        return false;
    }

    private static UsuarioRol mapRow(ResultSet resultado) throws SQLException {
        return new UsuarioRol(
                resultado.getInt("id_usuario_rol"),
                resultado.getInt("usuario_id"),
                resultado.getInt("rol_id"));
    }
}
