package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public static Categoria findById(int id) {
        String sql = "SELECT * FROM categoria WHERE id_categoria = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error CategoriaDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Categoria> findAll() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categoria ORDER BY id_categoria ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error CategoriaDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Categoria findByNombre(String nombre) {
        String sql = "SELECT * FROM categoria WHERE nombre = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombre);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error CategoriaDAO.findByNombre: " + excepcion.getMessage());
        }
        return null;
    }

    public static Categoria create(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion, estado) VALUES (?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, categoria.getNombre());
            consulta.setString(2, categoria.getDescripcion());
            consulta.setBoolean(3, categoria.getEstado().equalsIgnoreCase("Activo"));
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) categoria.setIdCategoria(clavesGeneradas.getInt(1));
                return categoria;
            }
        } catch (Exception excepcion) {
            System.out.println("Error CategoriaDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ?, estado = ? WHERE id_categoria = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, categoria.getNombre());
            consulta.setString(2, categoria.getDescripcion());
            consulta.setBoolean(3, categoria.getEstado().equalsIgnoreCase("Activo"));
            consulta.setInt(4, categoria.getIdCategoria());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CategoriaDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updateStatus(int id) {
        String sql = "UPDATE categoria SET estado = false WHERE id_categoria = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error CategoriaDAO.updateStatus: " + excepcion.getMessage());
        }
        return false;
    }

    // public static boolean delete(int id) {
    //     String sql = "DELETE FROM categoria WHERE id_categoria = ?";
    //     try (Connection conexion = dbConnection.getConnection();
    //          PreparedStatement consulta = conexion.prepareStatement(sql)) {
    //         consulta.setInt(1, id);
    //         return consulta.executeUpdate() > 0;
    //     } catch (Exception excepcion) {
    //         System.out.println("Error CategoriaDAO.delete: " + excepcion.getMessage());
    //     }
    //     return false;
    // }

    private static Categoria mapRow(ResultSet resultado) throws SQLException {
        return new Categoria(
                resultado.getInt("id_categoria"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"),
                resultado.getBoolean("estado") ? "Activo" : "Inactivo");
    }
}
