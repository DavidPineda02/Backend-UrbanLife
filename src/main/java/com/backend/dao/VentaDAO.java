package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Venta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public static Venta findById(int id) {
        String sql = "SELECT * FROM venta WHERE id_venta = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error VentaDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Venta> findAll() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM venta ORDER BY id_venta ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error VentaDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<Venta> findByUsuarioId(int usuarioId) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM venta WHERE usuario_id = ? ORDER BY id_venta DESC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error VentaDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Venta create(Venta venta) {
        String sql = "INSERT INTO venta (fecha_venta, total_venta, metodo_pago, usuario_id, cliente_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setDate(1, Date.valueOf(venta.getFechaVenta()));
            consulta.setDouble(2, venta.getTotalVenta());
            consulta.setString(3, venta.getMetodoPago());
            consulta.setInt(4, venta.getUsuarioId());
            consulta.setInt(5, venta.getClienteId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) venta.setIdVenta(clavesGeneradas.getInt(1));
                return venta;
            }
        } catch (Exception excepcion) {
            System.out.println("Error VentaDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Venta venta) {
        String sql = "UPDATE venta SET fecha_venta = ?, total_venta = ?, metodo_pago = ?, usuario_id = ?, cliente_id = ? WHERE id_venta = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setDate(1, Date.valueOf(venta.getFechaVenta()));
            consulta.setDouble(2, venta.getTotalVenta());
            consulta.setString(3, venta.getMetodoPago());
            consulta.setInt(4, venta.getUsuarioId());
            consulta.setInt(5, venta.getClienteId());
            consulta.setInt(6, venta.getIdVenta());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error VentaDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM venta WHERE id_venta = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error VentaDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static Venta mapRow(ResultSet resultado) throws SQLException {
        return new Venta(
                resultado.getInt("id_venta"),
                resultado.getDate("fecha_venta").toLocalDate(),
                resultado.getDouble("total_venta"),
                resultado.getString("metodo_pago"),
                resultado.getInt("usuario_id"),
                resultado.getInt("cliente_id"));
    }
}
