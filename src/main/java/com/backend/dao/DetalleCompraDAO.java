package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.DetalleCompra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleCompraDAO {

    public static DetalleCompra findById(int id) {
        String sql = "SELECT * FROM detalle_compra WHERE id_det_compra = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error DetalleCompraDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<DetalleCompra> findByCompraId(int compraId) {
        List<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_compra WHERE compra_id = ? ORDER BY id_det_compra ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, compraId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error DetalleCompraDAO.findByCompraId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<DetalleCompra> findAll() {
        List<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_compra ORDER BY id_det_compra ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error DetalleCompraDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static DetalleCompra create(DetalleCompra detalleCompra) {
        String sql = "INSERT INTO detalle_compra (cantidad, costo_unitario, subtotal, compra_id, producto_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setInt(1, detalleCompra.getCantidad());
            consulta.setDouble(2, detalleCompra.getCostoUnitario());
            consulta.setDouble(3, detalleCompra.getSubtotal());
            consulta.setInt(4, detalleCompra.getCompraId());
            consulta.setInt(5, detalleCompra.getProductoId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) detalleCompra.setIdDetCompra(clavesGeneradas.getInt(1));
                return detalleCompra;
            }
        } catch (Exception excepcion) {
            System.out.println("Error DetalleCompraDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(DetalleCompra detalleCompra) {
        String sql = "UPDATE detalle_compra SET cantidad = ?, costo_unitario = ?, subtotal = ?, compra_id = ?, producto_id = ? WHERE id_det_compra = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, detalleCompra.getCantidad());
            consulta.setDouble(2, detalleCompra.getCostoUnitario());
            consulta.setDouble(3, detalleCompra.getSubtotal());
            consulta.setInt(4, detalleCompra.getCompraId());
            consulta.setInt(5, detalleCompra.getProductoId());
            consulta.setInt(6, detalleCompra.getIdDetCompra());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error DetalleCompraDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM detalle_compra WHERE id_det_compra = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error DetalleCompraDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static DetalleCompra mapRow(ResultSet resultado) throws SQLException {
        return new DetalleCompra(
                resultado.getInt("id_det_compra"),
                resultado.getInt("cantidad"),
                resultado.getDouble("costo_unitario"),
                resultado.getDouble("subtotal"),
                resultado.getInt("compra_id"),
                resultado.getInt("producto_id"));
    }
}
