package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.RolPermiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con las relaciones rol-permiso en la base de datos.
 */
public class RolPermisoDAO {

    /**
     * Busca una relación rol-permiso por su ID.
     * @param id ID de la relación a buscar
     * @return Relación RolPermiso encontrada o null si no existe
     */
    public static RolPermiso findById(int id) {
        String sql = "SELECT * FROM rol_permisos WHERE id_rol_permiso = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<RolPermiso> findByRolId(int rolId) {
        List<RolPermiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM rol_permisos WHERE rol_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, rolId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.findByRolId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<RolPermiso> findAll() {
        List<RolPermiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM rol_permisos ORDER BY id_rol_permiso ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static RolPermiso create(RolPermiso rolPermiso) {
        String sql = "INSERT INTO rol_permisos (rol_id, permisos_id) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setInt(1, rolPermiso.getRolId());
            consulta.setInt(2, rolPermiso.getPermisosId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) rolPermiso.setIdRolPermiso(clavesGeneradas.getInt(1));
                return rolPermiso;
            }
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(RolPermiso rolPermiso) {
        String sql = "UPDATE rol_permisos SET rol_id = ?, permisos_id = ? WHERE id_rol_permiso = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, rolPermiso.getRolId());
            consulta.setInt(2, rolPermiso.getPermisosId());
            consulta.setInt(3, rolPermiso.getIdRolPermiso());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM rol_permisos WHERE id_rol_permiso = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean deleteByRolId(int rolId) {
        String sql = "DELETE FROM rol_permisos WHERE rol_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, rolId);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error RolPermisoDAO.deleteByRolId: " + excepcion.getMessage());
        }
        return false;
    }

    private static RolPermiso mapRow(ResultSet resultado) throws SQLException {
        return new RolPermiso(
                resultado.getInt("id_rol_permiso"),
                resultado.getInt("rol_id"),
                resultado.getInt("permisos_id"));
    }
}
