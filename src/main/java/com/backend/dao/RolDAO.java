package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con roles en la base de datos.
 */
public class RolDAO {

    /**
     * Busca un rol por su ID.
     * @param id ID del rol a buscar
     * @return Rol encontrado o null si no existe
     */
    public static Rol findById(int id) {
        String sql = "SELECT * FROM roles WHERE id_roles = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error RolDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static Rol findByNombre(String nombre) {
        String sql = "SELECT * FROM roles WHERE nombre = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombre);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error RolDAO.findByNombre: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Rol> findAll() {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY id_roles ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error RolDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Rol create(Rol rol) {
        String sql = "INSERT INTO roles (nombre, descripcion) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, rol.getNombre());
            consulta.setString(2, rol.getDescripcion());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) rol.setIdRoles(clavesGeneradas.getInt(1));
                return rol;
            }
        } catch (Exception excepcion) {
            System.out.println("Error RolDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Rol rol) {
        String sql = "UPDATE roles SET nombre = ?, descripcion = ? WHERE id_roles = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, rol.getNombre());
            consulta.setString(2, rol.getDescripcion());
            consulta.setInt(3, rol.getIdRoles());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error RolDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM roles WHERE id_roles = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error RolDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static Rol mapRow(ResultSet resultado) throws SQLException {
        return new Rol(
                resultado.getInt("id_roles"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"));
    }
}
