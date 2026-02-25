package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.TipoMovimiento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoMovimientoDAO {

    public static TipoMovimiento findById(int id) {
        String sql = "SELECT * FROM tipo_movimientos WHERE id_tipo_movimientos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error TipoMovimientoDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<TipoMovimiento> findAll() {
        List<TipoMovimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipo_movimientos ORDER BY id_tipo_movimientos ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error TipoMovimientoDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static TipoMovimiento create(TipoMovimiento tipoMovimiento) {
        String sql = "INSERT INTO tipo_movimientos (movimiento, naturaleza) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, tipoMovimiento.getMovimiento());
            consulta.setString(2, tipoMovimiento.getNaturaleza());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) tipoMovimiento.setIdTipoMovimientos(clavesGeneradas.getInt(1));
                return tipoMovimiento;
            }
        } catch (Exception excepcion) {
            System.out.println("Error TipoMovimientoDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(TipoMovimiento tipoMovimiento) {
        String sql = "UPDATE tipo_movimientos SET movimiento = ?, naturaleza = ? WHERE id_tipo_movimientos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, tipoMovimiento.getMovimiento());
            consulta.setString(2, tipoMovimiento.getNaturaleza());
            consulta.setInt(3, tipoMovimiento.getIdTipoMovimientos());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error TipoMovimientoDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM tipo_movimientos WHERE id_tipo_movimientos = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error TipoMovimientoDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static TipoMovimiento mapRow(ResultSet resultado) throws SQLException {
        return new TipoMovimiento(
                resultado.getInt("id_tipo_movimientos"),
                resultado.getString("movimiento"),
                resultado.getString("naturaleza"));
    }
}
