package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con usuarios en la base de datos.
 */
public class UsuarioDAO {

    /**
     * Busca un usuario por su correo electrónico.
     * @param correo Correo electrónico del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
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

    /**
     * Busca un usuario por su ID.
     * @param id ID del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
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

    /**
     * Busca un usuario por su ID de Google (autenticación OAuth2).
     * @param googleId ID de Google del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
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

    /**
     * Obtiene todos los usuarios de la base de datos ordenados por ID.
     * @return Lista de todos los usuarios
     */
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

    /**
     * Crea un nuevo usuario en la base de datos.
     * @param usuario Objeto Usuario con los datos a insertar
     * @return Usuario creado con su ID asignado, o null si falló
     */
    public static Usuario create(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, apellido, correo, contrasena, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, usuario.getNombre());
            consulta.setString(2, usuario.getApellido());
            consulta.setString(3, usuario.getCorreo());
            consulta.setString(4, usuario.getContrasena());
            consulta.setBoolean(5, usuario.isEstado());
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

    /**
     * Crea un nuevo usuario usando autenticación de Google.
     * @param googleId ID único de Google del usuario
     * @param nombre Nombre del usuario
     * @param correo Correo electrónico del usuario
     * @return Usuario creado con su ID asignado, o null si falló
     */
    public static Usuario createWithGoogle(String googleId, String nombre, String apellido, String correo) {
        String sql = "INSERT INTO usuarios (nombre, apellido, correo, contrasena, estado, google_id) VALUES (?, ?, ?, NULL, true, ?)";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, nombre);
            consulta.setString(2, apellido);
            consulta.setString(3, correo);
            consulta.setString(4, googleId);
            if (consulta.executeUpdate() > 0) {
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                Usuario nuevo = new Usuario();
                nuevo.setNombre(nombre);
                nuevo.setApellido(apellido);
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

    /**
     * Vincula un ID de Google a un usuario existente.
     * @param usuarioId ID del usuario existente
     * @param googleId ID de Google a vincular
     * @return true si la vinculación fue exitosa, false si falló
     */
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

    /**
     * Actualiza los datos de un usuario existente.
     * @param usuario Objeto Usuario con los datos actualizados
     * @return true si la actualización fue exitosa, false si falló
     */
    public static boolean update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, correo = ?, estado = ? WHERE id_usuario = ?";
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario.getNombre());
            consulta.setString(2, usuario.getApellido());
            consulta.setString(3, usuario.getCorreo());
            consulta.setBoolean(4, usuario.isEstado());
            consulta.setInt(5, usuario.getIdUsuario());
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            System.out.println("Error UsuarioDAO.update: " + excepcion.getMessage());
        }
        return false;
    }

    /**
     * Actualiza la contraseña de un usuario.
     * @param id ID del usuario
     * @param contrasenaEncriptada Nueva contraseña ya encriptada
     * @return true si la actualización fue exitosa, false si falló
     */
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

    /**
     * Desactiva un usuario (cambia su estado a false).
     * @param id ID del usuario a desactivar
     * @return true si la desactivación fue exitosa, false si falló
     */
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

    /**
     * Busca el nombre del rol principal de un usuario.
     * @param usuarioId ID del usuario
     * @return Nombre del rol encontrado o null si no existe
     */
    public static String findRolByUsuarioId(int usuarioId) {
        String sql = """
                SELECT r.nombre 
                FROM roles r 
                JOIN usuario_rol ur ON r.id_roles = ur.rol_id 
                WHERE ur.usuario_id = ?
                LIMIT 1
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

    /**
     * Mapea un ResultSet a un objeto Usuario.
     * @param resultado ResultSet con los datos del usuario
     * @return Objeto Usuario con los datos mapeados
     * @throws SQLException Si hay error al acceder a los datos
     */
    private static Usuario mapRow(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario(
                resultado.getInt("id_usuario"),
                resultado.getString("nombre"),
                resultado.getString("apellido"),
                resultado.getString("correo"),
                resultado.getString("contrasena"),
                resultado.getBoolean("estado"));
        try {
            usuario.setGoogleId(resultado.getString("google_id"));
        } catch (SQLException ignored) {}
        return usuario;
    }
}
