// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa la relación entre usuario y rol
import com.backend.models.UsuarioRol;

// Clases JDBC para conexión, consultas preparadas, resultados y sentencias
import java.sql.*;
// Lista dinámica para retornar múltiples relaciones
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con las relaciones usuario-rol en la base de datos.
 * Centraliza todo el acceso a la tabla Usuario_Rol evitando SQL disperso en capas superiores.
 */
public class UsuarioRolDAO {

    /**
     * Busca una relación usuario-rol por su ID.
     * @param id ID de la relación a buscar
     * @return Relación UsuarioRol encontrada o null si no existe
     */
    public static UsuarioRol findById(int id) {
        // SQL para seleccionar una relación usuario-rol por su clave primaria
        String sql = "SELECT * FROM usuario_rol WHERE id_usuario_rol = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID como parámetro de búsqueda
            consulta.setInt(1, id);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró la relación
        return null;
    }

    /**
     * Obtiene todas las relaciones rol asignadas a un usuario específico.
     * @param usuarioId ID del usuario cuyos roles se desean obtener
     * @return Lista de relaciones UsuarioRol (vacía si el usuario no tiene roles)
     */
    public static List<UsuarioRol> findByUsuarioId(int usuarioId) {
        // Lista donde se acumularán las relaciones encontradas
        List<UsuarioRol> lista = new ArrayList<>();
        // SQL para seleccionar todas las relaciones de un usuario específico
        String sql = "SELECT * FROM usuario_rol WHERE usuario_id = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del usuario como parámetro de búsqueda
            consulta.setInt(1, usuarioId);
            // Ejecutar consulta y obtener resultados
            ResultSet resultado = consulta.executeQuery();
            // Recorrer todos los registros y agregar cada relación a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        // Retornar la lista con todas las relaciones encontradas
        return lista;
    }

    /**
     * Obtiene todas las relaciones usuario-rol de la base de datos ordenadas por ID ascendente.
     * @return Lista de relaciones UsuarioRol (vacía si no hay ninguna)
     */
    public static List<UsuarioRol> findAll() {
        // Lista donde se acumularán todas las relaciones encontradas
        List<UsuarioRol> lista = new ArrayList<>();
        // SQL para seleccionar todas las relaciones ordenadas por ID
        String sql = "SELECT * FROM usuario_rol ORDER BY id_usuario_rol ASC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada relación a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todas las relaciones encontradas
        return lista;
    }

    /**
     * Inserta una nueva relación usuario-rol en la base de datos y asigna el ID generado.
     * @param usuarioRol Objeto UsuarioRol con usuarioId y rolId a insertar
     * @return El mismo UsuarioRol con su ID asignado, o null si falló la inserción
     */
    public static UsuarioRol create(UsuarioRol usuarioRol) {
        // SQL para insertar una nueva relación usuario-rol
        String sql = "INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)";
        // Abrir conexión y preparar consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el ID del usuario en la relación
            consulta.setInt(1, usuarioRol.getUsuarioId());
            // Asignar el ID del rol en la relación
            consulta.setInt(2, usuarioRol.getRolId());
            // Ejecutar INSERT y verificar que se insertó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por la BD (ID auto-incrementado)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si se obtuvo la clave generada, asignarla al objeto
                if (clavesGeneradas.next()) usuarioRol.setIdUsuarioRol(clavesGeneradas.getInt(1));
                // Retornar la relación con su ID asignado
                return usuarioRol;
            }
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la inserción falló
        return null;
    }

    /**
     * Actualiza los IDs de usuario y rol de una relación existente.
     * @param usuarioRol Objeto UsuarioRol con los nuevos datos y el ID a actualizar
     * @return true si se actualizó al menos una fila, false en caso contrario
     */
    public static boolean update(UsuarioRol usuarioRol) {
        // SQL para actualizar usuario_id y rol_id de una relación por su ID
        String sql = "UPDATE usuario_rol SET usuario_id = ?, rol_id = ? WHERE id_usuario_rol = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo ID de usuario
            consulta.setInt(1, usuarioRol.getUsuarioId());
            // Asignar el nuevo ID de rol
            consulta.setInt(2, usuarioRol.getRolId());
            // Asignar el ID de la relación a actualizar como condición WHERE
            consulta.setInt(3, usuarioRol.getIdUsuarioRol());
            // Ejecutar UPDATE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina una relación usuario-rol de la base de datos por su ID.
     * @param id ID de la relación a eliminar
     * @return true si se eliminó al menos una fila, false en caso contrario
     */
    public static boolean delete(int id) {
        // SQL para eliminar una relación por su clave primaria
        String sql = "DELETE FROM usuario_rol WHERE id_usuario_rol = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID de la relación a eliminar
            consulta.setInt(1, id);
            // Ejecutar DELETE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Elimina todas las relaciones de rol asignadas a un usuario específico.
     * Útil al eliminar un usuario o reasignar todos sus roles.
     * @param usuarioId ID del usuario cuyas relaciones de rol se desean eliminar
     * @return true si se eliminó al menos una fila, false en caso contrario
     */
    public static boolean deleteByUsuarioId(int usuarioId) {
        // SQL para eliminar todas las relaciones de un usuario específico
        String sql = "DELETE FROM usuario_rol WHERE usuario_id = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del usuario cuyas relaciones se eliminarán
            consulta.setInt(1, usuarioId);
            // Ejecutar DELETE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error UsuarioRolDAO.deleteByUsuarioId: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Convierte una fila del ResultSet en un objeto UsuarioRol.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto UsuarioRol con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static UsuarioRol mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un UsuarioRol con los datos del registro actual
        return new UsuarioRol(
                // Leer el ID de la relación desde la columna id_usuario_rol
                resultado.getInt("id_usuario_rol"),
                // Leer el ID del usuario desde la columna usuario_id
                resultado.getInt("usuario_id"),
                // Leer el ID del rol desde la columna rol_id
                resultado.getInt("rol_id"));
    }
}
