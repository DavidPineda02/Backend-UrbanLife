package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.GastoAdicional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GastoAdicionalDAO {

    public static GastoAdicional findById(int id) {
        String sql = "SELECT * FROM gastos_adicionales WHERE id_gastos_adic = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error GastoAdicionalDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<GastoAdicional> findAll() {
        List<GastoAdicional> lista = new ArrayList<>();
        String sql = "SELECT * FROM gastos_adicionales ORDER BY id_gastos_adic ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error GastoAdicionalDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<GastoAdicional> findByUsuarioId(int usuarioId) {
        List<GastoAdicional> lista = new ArrayList<>();
        String sql = "SELECT * FROM gastos_adicionales WHERE usuario_id = ? ORDER BY id_gastos_adic DESC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error GastoAdicionalDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static GastoAdicional create(GastoAdicional gastoAdicional) {
        String sql = "INSERT INTO gastos_adicionales (monto, descripcion, fecha_registro, metodo_pago, compra_id, tipo_gasto_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setDouble(1, gastoAdicional.getMonto());
            consulta.setString(2, gastoAdicional.getDescripcion());
            consulta.setDate(3, Date.valueOf(gastoAdicional.getFechaRegistro()));
            consulta.setString(4, gastoAdicional.getMetodoPago());
            if (gastoAdicional.getCompraId() == null) {
                consulta.setNull(5, Types.INTEGER);
            } else {
                consulta.setInt(5, gastoAdicional.getCompraId());
            }
            consulta.setInt(6, gastoAdicional.getTipoGastoId());
            consulta.setInt(7, gastoAdicional.getUsuarioId());

            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) gastoAdicional.setIdGastosAdic(clavesGeneradas.getInt(1));
                return gastoAdicional;
            }
        } catch (Exception excepcion) {
            System.out.println("Error GastoAdicionalDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(GastoAdicional gastoAdicional) {
        String sql = "UPDATE gastos_adicionales SET monto = ?, descripcion = ?, fecha_registro = ?, metodo_pago = ?, compra_id = ?, tipo_gasto_id = ?, usuario_id = ? WHERE id_gastos_adic = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setDouble(1, gastoAdicional.getMonto());
            consulta.setString(2, gastoAdicional.getDescripcion());
            consulta.setDate(3, Date.valueOf(gastoAdicional.getFechaRegistro()));
            consulta.setString(4, gastoAdicional.getMetodoPago());
            if (gastoAdicional.getCompraId() == null) {
                consulta.setNull(5, Types.INTEGER);
            } else {
                consulta.setInt(5, gastoAdicional.getCompraId());
            }
            consulta.setInt(6, gastoAdicional.getTipoGastoId());
            consulta.setInt(7, gastoAdicional.getUsuarioId());
            consulta.setInt(8, gastoAdicional.getIdGastosAdic());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error GastoAdicionalDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM gastos_adicionales WHERE id_gastos_adic = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error GastoAdicionalDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static GastoAdicional mapRow(ResultSet resultado) throws SQLException {
        int compraId = resultado.getInt("compra_id");
        Integer idCompra = resultado.wasNull() ? null : compraId;

        return new GastoAdicional(
                resultado.getInt("id_gastos_adic"),
                resultado.getDouble("monto"),
                resultado.getString("descripcion"),
                resultado.getDate("fecha_registro").toLocalDate(),
                resultado.getString("metodo_pago"),
                idCompra,
                resultado.getInt("tipo_gasto_id"),
                resultado.getInt("usuario_id"));
    }
}
