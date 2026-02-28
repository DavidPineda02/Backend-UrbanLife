package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Compra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    public static Compra findById(int id) {
        String sql = "SELECT * FROM compra WHERE id_compra = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error CompraDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Compra> findAll() {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra ORDER BY id_compra ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CompraDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<Compra> findByUsuarioId(int usuarioId) {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra WHERE usuario_id = ? ORDER BY id_compra DESC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CompraDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Compra create(Compra compra) {
        String sql = "INSERT INTO compra (fecha_compra, total_compra, metodo_pago, usuario_id, proveedor_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setDate(1, Date.valueOf(compra.getFechaCompra()));
            consulta.setDouble(2, compra.getTotalCompra());
            consulta.setString(3, compra.getMetodoPago());
            consulta.setInt(4, compra.getUsuarioId());
            consulta.setInt(5, compra.getProveedorId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) compra.setIdCompra(clavesGeneradas.getInt(1));
                return compra;
            }
        } catch (Exception excepcion) {
            System.out.println("Error CompraDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Compra compra) {
        String sql = "UPDATE compra SET fecha_compra = ?, total_compra = ?, metodo_pago = ?, usuario_id = ?, proveedor_id = ? WHERE id_compra = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setDate(1, Date.valueOf(compra.getFechaCompra()));
            consulta.setDouble(2, compra.getTotalCompra());
            consulta.setString(3, compra.getMetodoPago());
            consulta.setInt(4, compra.getUsuarioId());
            consulta.setInt(5, compra.getProveedorId());
            consulta.setInt(6, compra.getIdCompra());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CompraDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM compra WHERE id_compra = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CompraDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static Compra mapRow(ResultSet resultado) throws SQLException {
        return new Compra(
                resultado.getInt("id_compra"),
                resultado.getDate("fecha_compra").toLocalDate(),
                resultado.getDouble("total_compra"),
                resultado.getString("metodo_pago"),
                resultado.getInt("usuario_id"),
                resultado.getInt("proveedor_id"));
    }
}
