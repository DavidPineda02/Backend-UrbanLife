package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario findByCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findByCorreo: " + e.getMessage());
        }
        return null;
    }

    public Usuario findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findById: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> findAll() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findAll: " + e.getMessage());
        }
        return lista;
    }

    public Usuario create(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getCorreo());
            stmt.setString(3, u.getContrasena());
            stmt.setString(4, u.getEstado());
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) u.setIdUsuario(keys.getInt(1));
                return u;
            }
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.create: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Usuario u) {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, estado = ? WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getCorreo());
            stmt.setString(3, u.getEstado());
            stmt.setInt(4, u.getIdUsuario());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.update: " + e.getMessage());
        }
        return false;
    }

    public boolean updatePassword(int id, String hashedPassword) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.updatePassword: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.delete: " + e.getMessage());
        }
        return false;
    }

    public String findRolByUsuarioId(int usuarioId) {
        String sql = """
                SELECT r.nombre FROM roles r
                INNER JOIN usuarios_roles ur ON r.id_roles = ur.rol_id
                WHERE ur.usuario_id = ? LIMIT 1
                """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("nombre");
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findRolByUsuarioId: " + e.getMessage());
        }
        return null;
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("contrasena"),
                rs.getString("estado"));
    }
}
