package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.ImagenProducto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImagenProductoDAO {

    public static ImagenProducto findById(int id) {
        String sql = "SELECT * FROM imagenes_producto WHERE imagen_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error ImagenProductoDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<ImagenProducto> findByProductoId(int productoId) {
        List<ImagenProducto> lista = new ArrayList<>();
        String sql = "SELECT * FROM imagenes_producto WHERE producto_id = ? ORDER BY imagen_producto ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, productoId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error ImagenProductoDAO.findByProductoId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<ImagenProducto> findAll() {
        List<ImagenProducto> lista = new ArrayList<>();
        String sql = "SELECT * FROM imagenes_producto ORDER BY imagen_producto ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error ImagenProductoDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static ImagenProducto create(ImagenProducto imagenProducto) {
        String sql = "INSERT INTO imagenes_producto (url, fecha_registro, producto_id) VALUES (?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, imagenProducto.getUrl());
            consulta.setDate(2, Date.valueOf(imagenProducto.getFechaRegistro()));
            consulta.setInt(3, imagenProducto.getProductoId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) imagenProducto.setImagenProducto(clavesGeneradas.getInt(1));
                return imagenProducto;
            }
        } catch (Exception excepcion) {
            System.out.println("Error ImagenProductoDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(ImagenProducto imagenProducto) {
        String sql = "UPDATE imagenes_producto SET url = ?, fecha_registro = ?, producto_id = ? WHERE imagen_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, imagenProducto.getUrl());
            consulta.setDate(2, Date.valueOf(imagenProducto.getFechaRegistro()));
            consulta.setInt(3, imagenProducto.getProductoId());
            consulta.setInt(4, imagenProducto.getImagenProducto());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ImagenProductoDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM imagenes_producto WHERE imagen_producto = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ImagenProductoDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static ImagenProducto mapRow(ResultSet resultado) throws SQLException {
        return new ImagenProducto(
                resultado.getInt("imagen_producto"),
                resultado.getString("url"),
                resultado.getDate("fecha_registro").toLocalDate(),
                resultado.getInt("producto_id"));
    }
}
