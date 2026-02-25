package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.MovimientoFinanciero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoFinancieroDAO {

    public static MovimientoFinanciero findById(int id) {
        String sql = "SELECT * FROM movimientos_financieros WHERE id_movs_financieros = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error MovimientoFinancieroDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<MovimientoFinanciero> findAll() {
        List<MovimientoFinanciero> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_financieros ORDER BY id_movs_financieros ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error MovimientoFinancieroDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<MovimientoFinanciero> findByUsuarioId(int usuarioId) {
        List<MovimientoFinanciero> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_financieros WHERE usuario_id = ? ORDER BY id_movs_financieros DESC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error MovimientoFinancieroDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static MovimientoFinanciero create(MovimientoFinanciero movimientoFinanciero) {
        String sql = "INSERT INTO movimientos_financieros (fecha_movimiento, concepto, monto, metodo_pago, tipo_movimiento_id, usuario_id, venta_id, compra_id, gasto_adicional_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setDate(1, Date.valueOf(movimientoFinanciero.getFechaMovimiento()));
            consulta.setString(2, movimientoFinanciero.getConcepto());
            consulta.setDouble(3, movimientoFinanciero.getMonto());
            consulta.setString(4, movimientoFinanciero.getMetodoPago());
            consulta.setInt(5, movimientoFinanciero.getTipoMovimientoId());
            consulta.setInt(6, movimientoFinanciero.getUsuarioId());
            if (movimientoFinanciero.getVentaId() == null) consulta.setNull(7, Types.INTEGER);
            else consulta.setInt(7, movimientoFinanciero.getVentaId());
            if (movimientoFinanciero.getCompraId() == null) consulta.setNull(8, Types.INTEGER);
            else consulta.setInt(8, movimientoFinanciero.getCompraId());
            if (movimientoFinanciero.getGastoAdicionalId() == null) consulta.setNull(9, Types.INTEGER);
            else consulta.setInt(9, movimientoFinanciero.getGastoAdicionalId());

            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) movimientoFinanciero.setIdMovsFinancieros(clavesGeneradas.getInt(1));
                return movimientoFinanciero;
            }
        } catch (Exception excepcion) {
            System.out.println("Error MovimientoFinancieroDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(MovimientoFinanciero movimientoFinanciero) {
        String sql = "UPDATE movimientos_financieros SET fecha_movimiento = ?, concepto = ?, monto = ?, metodo_pago = ?, tipo_movimiento_id = ?, usuario_id = ?, venta_id = ?, compra_id = ?, gasto_adicional_id = ? WHERE id_movs_financieros = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setDate(1, Date.valueOf(movimientoFinanciero.getFechaMovimiento()));
            consulta.setString(2, movimientoFinanciero.getConcepto());
            consulta.setDouble(3, movimientoFinanciero.getMonto());
            consulta.setString(4, movimientoFinanciero.getMetodoPago());
            consulta.setInt(5, movimientoFinanciero.getTipoMovimientoId());
            consulta.setInt(6, movimientoFinanciero.getUsuarioId());
            if (movimientoFinanciero.getVentaId() == null) consulta.setNull(7, Types.INTEGER);
            else consulta.setInt(7, movimientoFinanciero.getVentaId());
            if (movimientoFinanciero.getCompraId() == null) consulta.setNull(8, Types.INTEGER);
            else consulta.setInt(8, movimientoFinanciero.getCompraId());
            if (movimientoFinanciero.getGastoAdicionalId() == null) consulta.setNull(9, Types.INTEGER);
            else consulta.setInt(9, movimientoFinanciero.getGastoAdicionalId());
            consulta.setInt(10, movimientoFinanciero.getIdMovsFinancieros());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error MovimientoFinancieroDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM movimientos_financieros WHERE id_movs_financieros = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error MovimientoFinancieroDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static MovimientoFinanciero mapRow(ResultSet resultado) throws SQLException {
        int ventaId = resultado.getInt("venta_id");
        Integer idVenta = resultado.wasNull() ? null : ventaId;

        int compraId = resultado.getInt("compra_id");
        Integer idCompra = resultado.wasNull() ? null : compraId;

        int gastoAdicionalId = resultado.getInt("gasto_adicional_id");
        Integer idGastoAdicional = resultado.wasNull() ? null : gastoAdicionalId;

        return new MovimientoFinanciero(
                resultado.getInt("id_movs_financieros"),
                resultado.getDate("fecha_movimiento").toLocalDate(),
                resultado.getString("concepto"),
                resultado.getDouble("monto"),
                resultado.getString("metodo_pago"),
                resultado.getInt("tipo_movimiento_id"),
                resultado.getInt("usuario_id"),
                idVenta,
                idCompra,
                idGastoAdicional);
    }
}
