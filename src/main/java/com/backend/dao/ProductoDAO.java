package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public static Producto findById(int id) {
        String sql = "SELECT * FROM producto WHERE id_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Producto> findAll() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY id_producto ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<Producto> findByCategoriaId(int categoriaId) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE categoria_id = ? ORDER BY id_producto ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, categoriaId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.findByCategoriaId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Producto create(Producto producto) {
        String sql = "INSERT INTO producto (nombre, descripcion, precio_venta, costo_promedio, stock, estado, categoria_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, producto.getNombre());
            consulta.setString(2, producto.getDescripcion());
            consulta.setDouble(3, producto.getPrecioVenta());
            consulta.setDouble(4, producto.getCostoPromedio());
            consulta.setInt(5, producto.getStock());
            consulta.setBoolean(6, producto.isEstado());
            consulta.setInt(7, producto.getCategoriaId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) producto.setIdProducto(clavesGeneradas.getInt(1));
                return producto;
            }
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, descripcion = ?, precio_venta = ?, costo_promedio = ?, stock = ?, estado = ?, categoria_id = ? WHERE id_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, producto.getNombre());
            consulta.setString(2, producto.getDescripcion());
            consulta.setDouble(3, producto.getPrecioVenta());
            consulta.setDouble(4, producto.getCostoPromedio());
            consulta.setInt(5, producto.getStock());
            consulta.setBoolean(6, producto.isEstado());
            consulta.setInt(7, producto.getCategoriaId());
            consulta.setInt(8, producto.getIdProducto());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updateStock(int id, int nuevoStock) {
        String sql = "UPDATE producto SET stock = ? WHERE id_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, nuevoStock);
            consulta.setInt(2, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.updateStock: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updateStatus(int id) {
        String sql = "UPDATE producto SET estado = false WHERE id_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ProductoDAO.updateStatus: " + excepcion.getMessage());
        }
        return false;
    }

    // public static boolean delete(int id) {
    //     String sql = "DELETE FROM producto WHERE id_producto = ?";
    //     try (Connection conexion = dbConnection.getConnection();
    //          PreparedStatement consulta = conexion.prepareStatement(sql)) {
    //         consulta.setInt(1, id);
    //         return consulta.executeUpdate() > 0;
    //     } catch (Exception excepcion) {
    //         System.out.println("Error ProductoDAO.delete: " + excepcion.getMessage());
    //     }
    //     return false;
    // }

    private static Producto mapRow(ResultSet resultado) throws SQLException {
        return new Producto(
                resultado.getInt("id_producto"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"),
                resultado.getDouble("precio_venta"),
                resultado.getDouble("costo_promedio"),
                resultado.getInt("stock"),
                resultado.getBoolean("estado"),
                resultado.getInt("categoria_id"));
    }
}
