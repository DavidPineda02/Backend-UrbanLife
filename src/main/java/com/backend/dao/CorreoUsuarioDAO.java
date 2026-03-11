// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa un correo electrónico asociado a un usuario
import com.backend.models.CorreoUsuario;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de correos
import java.util.ArrayList;
// Para manejar colecciones de correos
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con los correos electrónicos de usuarios en la base de datos.
 * Un usuario puede tener múltiples correos asociados.
 */
public class CorreoUsuarioDAO {

    /**
     * Busca un correo de usuario por su ID en la base de datos.
     * @param id ID del correo a buscar
     * @return CorreoUsuario encontrado o null si no existe
     */
    public static CorreoUsuario findById(int id) {
        // Consulta SQL para buscar un correo por su ID
        String sql = "SELECT * FROM Correos_Usuario WHERE ID_CORREO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del correo como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto CorreoUsuario y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error CorreoUsuarioDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el correo
        return null;
    }

    /**
     * Busca todos los correos asociados a un usuario específico.
     * @param usuarioId ID del usuario a consultar
     * @return Lista de correos del usuario especificado
     */
    public static List<CorreoUsuario> findByUsuarioId(int usuarioId) {
        // Crear lista vacía para almacenar los correos encontrados
        List<CorreoUsuario> lista = new ArrayList<>();
        // Consulta SQL para buscar correos por ID del usuario
        String sql = "SELECT * FROM Correos_Usuario WHERE USUARIO_ID = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del usuario como parámetro de la consulta
            consulta.setInt(1, usuarioId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Recorrer cada registro y agregarlo a la lista como objeto CorreoUsuario
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error CorreoUsuarioDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        // Retornar la lista de correos (vacía si hubo error o no hay datos)
        return lista;
    }

    /**
     * Obtiene todos los correos de usuarios registrados en la base de datos.
     * @return Lista de correos ordenados por ID ascendente
     */
    public static List<CorreoUsuario> findAll() {
        // Crear lista vacía para almacenar los correos encontrados
        List<CorreoUsuario> lista = new ArrayList<>();
        // Consulta SQL para obtener todos los correos ordenados por ID
        String sql = "SELECT * FROM Correos_Usuario ORDER BY ID_CORREO ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto CorreoUsuario
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error CorreoUsuarioDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de correos (vacía si hubo error)
        return lista;
    }

    /**
     * Crea un nuevo correo de usuario en la base de datos.
     * @param correoUsuario Objeto CorreoUsuario con los datos a insertar
     * @return CorreoUsuario creado con su ID generado o null si falló
     */
    public static CorreoUsuario create(CorreoUsuario correoUsuario) {
        // Consulta SQL para insertar un nuevo correo de usuario
        String sql = "INSERT INTO Correos_Usuario (CORREO, USUARIO_ID) VALUES (?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer la dirección de correo como primer parámetro
            consulta.setString(1, correoUsuario.getCorreo());
            // Establecer el ID del usuario asociado como segundo parámetro
            consulta.setInt(2, correoUsuario.getUsuarioId());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto correoUsuario
                if (clavesGeneradas.next()) correoUsuario.setIdCorreo(clavesGeneradas.getInt(1));
                // Retornar el correo creado con su ID asignado
                return correoUsuario;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error CorreoUsuarioDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza un correo de usuario existente en la base de datos.
     * @param correoUsuario Objeto CorreoUsuario con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(CorreoUsuario correoUsuario) {
        // Consulta SQL para actualizar la dirección de correo
        String sql = "UPDATE Correos_Usuario SET CORREO = ? WHERE ID_CORREO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer la nueva dirección de correo como primer parámetro
            consulta.setString(1, correoUsuario.getCorreo());
            // Establecer el ID del correo a actualizar como segundo parámetro
            consulta.setInt(2, correoUsuario.getIdCorreo());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error CorreoUsuarioDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina un correo de usuario de la base de datos por su ID.
     * @param id ID del correo a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar un correo por su ID
        String sql = "DELETE FROM Correos_Usuario WHERE ID_CORREO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del correo a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error CorreoUsuarioDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto CorreoUsuario.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto CorreoUsuario con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static CorreoUsuario mapRow(ResultSet resultado) throws SQLException {
        // Crear y retornar un nuevo objeto CorreoUsuario con los valores de las columnas
        return new CorreoUsuario(
                resultado.getInt("ID_CORREO"),      // Obtener el ID del correo
                resultado.getString("CORREO"),       // Obtener la dirección de correo
                resultado.getInt("USUARIO_ID"));     // Obtener el ID del usuario asociado
    }
}
