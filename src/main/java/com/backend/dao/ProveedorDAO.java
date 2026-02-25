package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public static Proveedor findById(int id) {
        String sql = "SELECT * FROM proveedores WHERE id_proveedor = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error ProveedorDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Proveedor> findAll() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY id_proveedor ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error ProveedorDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Proveedor findByNit(String nit) {
        String sql = "SELECT * FROM proveedores WHERE nit = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nit);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error ProveedorDAO.findByNit: " + excepcion.getMessage());
        }
        return null;
    }

    public static Proveedor create(Proveedor proveedor) {
        String sql = "INSERT INTO proveedores (nombre, razon_social, nit, correo, telefono, direccion, ciudad, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, proveedor.getNombre());
            consulta.setString(2, proveedor.getRazonSocial());
            consulta.setString(3, proveedor.getNit());
            consulta.setString(4, proveedor.getCorreo());
            consulta.setString(5, proveedor.getTelefono());
            consulta.setString(6, proveedor.getDireccion());
            consulta.setString(7, proveedor.getCiudad());
            consulta.setBoolean(8, proveedor.isEstado());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) proveedor.setIdProveedor(clavesGeneradas.getInt(1));
                return proveedor;
            }
        } catch (Exception excepcion) {
            System.out.println("Error ProveedorDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Proveedor proveedor) {
        String sql = "UPDATE proveedores SET nombre = ?, razon_social = ?, nit = ?, correo = ?, telefono = ?, direccion = ?, ciudad = ?, estado = ? WHERE id_proveedor = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, proveedor.getNombre());
            consulta.setString(2, proveedor.getRazonSocial());
            consulta.setString(3, proveedor.getNit());
            consulta.setString(4, proveedor.getCorreo());
            consulta.setString(5, proveedor.getTelefono());
            consulta.setString(6, proveedor.getDireccion());
            consulta.setString(7, proveedor.getCiudad());
            consulta.setBoolean(8, proveedor.isEstado());
            consulta.setInt(9, proveedor.getIdProveedor());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ProveedorDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updateStatus(int id) {
        String sql = "UPDATE proveedores SET estado = false WHERE id_proveedor = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ProveedorDAO.updateStatus: " + excepcion.getMessage());
        }
        return false;
    }

    // public static boolean delete(int id) {
    //     String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";
    //     try (Connection conexion = dbConnection.getConnection();
    //          PreparedStatement consulta = conexion.prepareStatement(sql)) {
    //         consulta.setInt(1, id);
    //         return consulta.executeUpdate() > 0;
    //     } catch (Exception excepcion) {
    //         System.out.println("Error ProveedorDAO.delete: " + excepcion.getMessage());
    //     }
    //     return false;
    // }

    private static Proveedor mapRow(ResultSet resultado) throws SQLException {
        return new Proveedor(
                resultado.getInt("id_proveedor"),
                resultado.getString("nombre"),
                resultado.getString("razon_social"),
                resultado.getString("nit"),
                resultado.getString("correo"),
                resultado.getString("telefono"),
                resultado.getString("direccion"),
                resultado.getString("ciudad"),
                resultado.getBoolean("estado"));
    }
}
