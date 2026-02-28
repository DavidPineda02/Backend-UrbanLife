package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.NumeroPerfil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NumeroPerfilDAO {

    public static NumeroPerfil findById(int id) {
        String sql = "SELECT * FROM numeros_perfil WHERE id_numeros = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error NumeroPerfilDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<NumeroPerfil> findByPerfilId(int perfilId) {
        List<NumeroPerfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM numeros_perfil WHERE perfil_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, perfilId);
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error NumeroPerfilDAO.findByPerfilId: " + excepcion.getMessage());
        }
        return lista;
    }

    public static List<NumeroPerfil> findAll() {
        List<NumeroPerfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM numeros_perfil ORDER BY id_numeros ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error NumeroPerfilDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static NumeroPerfil create(NumeroPerfil numeroPerfil) {
        String sql = "INSERT INTO numeros_perfil (numero, perfil_id) VALUES (?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, numeroPerfil.getNumero());
            consulta.setInt(2, numeroPerfil.getPerfilId());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) numeroPerfil.setIdNumeros(clavesGeneradas.getInt(1));
                return numeroPerfil;
            }
        } catch (Exception excepcion) {
            System.out.println("Error NumeroPerfilDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean update(NumeroPerfil numeroPerfil) {
        String sql = "UPDATE numeros_perfil SET numero = ?, perfil_id = ? WHERE id_numeros = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, numeroPerfil.getNumero());
            consulta.setInt(2, numeroPerfil.getPerfilId());
            consulta.setInt(3, numeroPerfil.getIdNumeros());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error NumeroPerfilDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM numeros_perfil WHERE id_numeros = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error NumeroPerfilDAO.delete: " + excepcion.getMessage());
        }
        return false;
    }

    private static NumeroPerfil mapRow(ResultSet resultado) throws SQLException {
        return new NumeroPerfil(
                resultado.getInt("id_numeros"),
                resultado.getString("numero"),
                resultado.getInt("perfil_id"));
    }
}
