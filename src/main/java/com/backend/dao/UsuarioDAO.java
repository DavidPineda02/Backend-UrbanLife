package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public static Usuario findByCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, correo);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.findByCorreo: " + excepcion.getMessage());
        }
        return null;
    }

    public static Usuario findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.findById: " + excepcion.getMessage());
        }
        return null;
    }

    public static Usuario findByGoogleId(String googleId) {
        String sql = "SELECT * FROM usuarios WHERE google_id = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, googleId);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.findByGoogleId: " + excepcion.getMessage());
        }
        return null;
    }

    public static List<Usuario> findAll() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.findAll: " + excepcion.getMessage());
        }
        return lista;
    }

    public static Usuario create(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, estado) VALUES (?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, usuario.getNombre());
            consulta.setString(2, usuario.getCorreo());
            consulta.setString(3, usuario.getContrasena());
            consulta.setBoolean(4, usuario.isEstado());
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (clavesGeneradas.next()) usuario.setIdUsuario(clavesGeneradas.getInt(1));
                return usuario;
            }
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.create: " + excepcion.getMessage());
        }
        return null;
    }

    public static Usuario createWithGoogle(String googleId, String nombre, String correo) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, estado, google_id) VALUES (?, ?, NULL, true, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, nombre);
            consulta.setString(2, correo);
            consulta.setString(3, googleId);
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                Usuario nuevo = new Usuario();
                nuevo.setNombre(nombre);
                nuevo.setCorreo(correo);
                nuevo.setGoogleId(googleId);
                nuevo.setEstado(true);
                if (clavesGeneradas.next()) nuevo.setIdUsuario(clavesGeneradas.getInt(1));
                return nuevo;
            }
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.createWithGoogle: " + excepcion.getMessage());
        }
        return null;
    }

    public static boolean linkGoogleId(int usuarioId, String googleId) {
        String sql = "UPDATE usuarios SET google_id = ? WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, googleId);
            consulta.setInt(2, usuarioId);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.linkGoogleId: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, estado = ? WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario.getNombre());
            consulta.setString(2, usuario.getCorreo());
            consulta.setBoolean(3, usuario.isEstado());
            consulta.setInt(4, usuario.getIdUsuario());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updatePassword(int id, String contrasenaEncriptada) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, contrasenaEncriptada);
            consulta.setInt(2, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.updatePassword: " + excepcion.getMessage());
        }
        return false;
    }

    public static boolean updateStatus(int id) {
        String sql = "UPDATE usuarios SET estado = false WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.updateStatus: " + excepcion.getMessage());
        }
        return false;
    }

    // public static boolean delete(int id) {
    //     String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
    //     try (Connection conexion = dbConnection.getConnection();
    //          PreparedStatement consulta = conexion.prepareStatement(sql)) {
    //         consulta.setInt(1, id);
    //         return consulta.executeUpdate() > 0;
    //     } catch (Exception excepcion) {
    //         System.out.println("Error UsuarioDAO.delete: " + excepcion.getMessage());
    //     }
    //     return false;
    // }

    public static String findRolByUsuarioId(int usuarioId) {
        String sql = """
                SELECT r.nombre FROM roles r
                INNER JOIN usuarios_roles ur ON r.id_roles = ur.rol_id
                WHERE ur.usuario_id = ? LIMIT 1
                """;
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return resultado.getString("nombre");
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.findRolByUsuarioId: " + excepcion.getMessage());
        }
        return null;
    }

    private static Usuario mapRow(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario(
                resultado.getInt("id_usuario"),
                resultado.getString("nombre"),
                resultado.getString("correo"),
                resultado.getString("contrasena"),
                resultado.getBoolean("estado"));
        try {
            usuario.setGoogleId(resultado.getString("google_id"));
        } catch (SQLException ignored) {
            // La columna google_id puede no existir en consultas antiguas
        }
        return usuario;
    }
}
