package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Permiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con permisos en la base de datos.
 */
public class PermisoDAO {

    /**
     * Busca un permiso por su ID.
     * @param id ID del permiso a buscar
     * @return Permiso encontrado o null si no existe
     */
    public static Permiso findById(int id) {
        String sql = "SELECT * FROM permisos WHERE id_permisos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error PermisoDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Permiso> findAll() {
        List<Permiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM permisos ORDER BY id_permisos ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error PermisoDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Permiso create(Permiso permiso) {
        String sql = "INSERT INTO permisos (nombre, descripcion) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, permiso.getNombre());
            consulta.setString(2, permiso.getDescripcion());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) permiso.setIdPermisos(clavesGeneradas.getInt(1));
                return permiso;
            }
        } catch (Exception excepcion) {
            System.out.println("Error PermisoDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Permiso permiso) {
        String sql = "UPDATE permisos SET nombre = ?, descripcion = ? WHERE id_permisos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, permiso.getNombre());
            consulta.setString(2, permiso.getDescripcion());
            consulta.setInt(3, permiso.getIdPermisos());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error PermisoDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM permisos WHERE id_permisos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error PermisoDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static Permiso mapRow(ResultSet resultado) throws SQLException {
        return new Permiso(
                resultado.getInt("id_permisos"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"));
    }
}
