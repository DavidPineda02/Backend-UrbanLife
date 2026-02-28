package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public static Cliente findById(int id) {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error ClienteDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Cliente> findAll() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY id_cliente ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error ClienteDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Cliente findByDocumento(String documento) {
        String sql = "SELECT * FROM clientes WHERE documento = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, documento);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error ClienteDAO.findByDocumento: " + excepcion.getMessage());
        }
        return null;
    }

    public static Cliente create(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, documento, correo, telefono, direccion, ciudad, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getDocumento());
            consulta.setString(3, cliente.getCorreo());
            consulta.setString(4, cliente.getTelefono());
            consulta.setString(5, cliente.getDireccion());
            consulta.setString(6, cliente.getCiudad());
            consulta.setBoolean(7, cliente.isEstado());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) cliente.setIdCliente(clavesGeneradas.getInt(1));
                return cliente;
            }
        } catch (Exception excepcion) {
            System.out.println("Error ClienteDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, documento = ?, correo = ?, telefono = ?, direccion = ?, ciudad = ?, estado = ? WHERE id_cliente = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getDocumento());
            consulta.setString(3, cliente.getCorreo());
            consulta.setString(4, cliente.getTelefono());
            consulta.setString(5, cliente.getDireccion());
            consulta.setString(6, cliente.getCiudad());
            consulta.setBoolean(7, cliente.isEstado());
            consulta.setInt(8, cliente.getIdCliente());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ClienteDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updateStatus(int id) {
        String sql = "UPDATE clientes SET estado = false WHERE id_cliente = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error ClienteDAO.updateStatus: " + excepcion.getMessage());
        }
        return false;
    }

    // public static boolean delete(int id) {
    //     String sql = "DELETE FROM clientes WHERE id_cliente = ?";
    //     try (Connection conexion = dbConnection.getConnection();
    //          PreparedStatement consulta = conexion.prepareStatement(sql)) {
    //         consulta.setInt(1, id);
    //         return consulta.executeUpdate() > 0;
    //     } catch (Exception excepcion) {
    //         System.out.println("Error ClienteDAO.delete: " + excepcion.getMessage());
    //     }
    //     return false;
    // }

    private static Cliente mapRow(ResultSet resultado) throws SQLException {
        return new Cliente(
                resultado.getInt("id_cliente"),
                resultado.getString("nombre"),
                resultado.getString("documento"),
                resultado.getString("correo"),
                resultado.getString("telefono"),
                resultado.getString("direccion"),
                resultado.getString("ciudad"),
                resultado.getBoolean("estado"));
    }
}
