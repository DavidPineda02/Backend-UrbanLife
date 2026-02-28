package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.DetalleVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAO {

    public static DetalleVenta findById(int id) {
        String sql = "SELECT * FROM detalle_venta WHERE id_det_venta = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error DetalleVentaDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<DetalleVenta> findByVentaId(int ventaId) {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_venta WHERE venta_id = ? ORDER BY id_det_venta ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, ventaId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error DetalleVentaDAO.findByVentaId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<DetalleVenta> findAll() {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_venta ORDER BY id_det_venta ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error DetalleVentaDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static DetalleVenta create(DetalleVenta detalleVenta) {
        String sql = "INSERT INTO detalle_venta (cantidad, precio_unitario, subtotal, venta_id, producto_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setInt(1, detalleVenta.getCantidad());
            consulta.setDouble(2, detalleVenta.getPrecioUnitario());
            consulta.setDouble(3, detalleVenta.getSubtotal());
            consulta.setInt(4, detalleVenta.getVentaId());
            consulta.setInt(5, detalleVenta.getProductoId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) detalleVenta.setIdDetVenta(clavesGeneradas.getInt(1));
                return detalleVenta;
            }
        } catch (Exception excepcion) {
            System.out.println("Error DetalleVentaDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(DetalleVenta detalleVenta) {
        String sql = "UPDATE detalle_venta SET cantidad = ?, precio_unitario = ?, subtotal = ?, venta_id = ?, producto_id = ? WHERE id_det_venta = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, detalleVenta.getCantidad());
            consulta.setDouble(2, detalleVenta.getPrecioUnitario());
            consulta.setDouble(3, detalleVenta.getSubtotal());
            consulta.setInt(4, detalleVenta.getVentaId());
            consulta.setInt(5, detalleVenta.getProductoId());
            consulta.setInt(6, detalleVenta.getIdDetVenta());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error DetalleVentaDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM detalle_venta WHERE id_det_venta = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error DetalleVentaDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static DetalleVenta mapRow(ResultSet resultado) throws SQLException {
        return new DetalleVenta(
                resultado.getInt("id_det_venta"),
                resultado.getInt("cantidad"),
                resultado.getDouble("precio_unitario"),
                resultado.getDouble("subtotal"),
                resultado.getInt("venta_id"),
                resultado.getInt("producto_id"));
    }
}
