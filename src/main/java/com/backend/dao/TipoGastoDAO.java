package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.TipoGasto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoGastoDAO {

    public static TipoGasto findById(int id) {
        String sql = "SELECT * FROM tipo_gasto WHERE id_tipo_gasto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error TipoGastoDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<TipoGasto> findAll() {
        List<TipoGasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipo_gasto ORDER BY id_tipo_gasto ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error TipoGastoDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static TipoGasto create(TipoGasto tipoGasto) {
        String sql = "INSERT INTO tipo_gasto (nombre, descripcion) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, tipoGasto.getNombre());
            consulta.setString(2, tipoGasto.getDescripcion());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) tipoGasto.setIdTipoGasto(clavesGeneradas.getInt(1));
                return tipoGasto;
            }
        } catch (Exception excepcion) {
            System.out.println("Error TipoGastoDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(TipoGasto tipoGasto) {
        String sql = "UPDATE tipo_gasto SET nombre = ?, descripcion = ? WHERE id_tipo_gasto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, tipoGasto.getNombre());
            consulta.setString(2, tipoGasto.getDescripcion());
            consulta.setInt(3, tipoGasto.getIdTipoGasto());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error TipoGastoDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM tipo_gasto WHERE id_tipo_gasto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error TipoGastoDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static TipoGasto mapRow(ResultSet resultado) throws SQLException {
        return new TipoGasto(
                resultado.getInt("id_tipo_gasto"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"));
    }
}
