// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa un número telefónico asociado a un usuario
import com.backend.models.NumeroUsuario;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de números
import java.util.ArrayList;
// Para manejar colecciones de números
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con los números telefónicos de usuarios en la base de datos.
 * Un usuario puede tener múltiples números de teléfono asociados.
 */
public class NumeroUsuarioDAO {

    /**
     * Busca un número de usuario por su ID en la base de datos.
     * @param id ID del número a buscar
     * @return NumeroUsuario encontrado o null si no existe
     */
    public static NumeroUsuario findById(int id) {
        // Consulta SQL para buscar un número por su ID
        String sql = "SELECT * FROM Numeros_Usuario WHERE ID_NUMERO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del número como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto NumeroUsuario y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error NumeroUsuarioDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el número
        return null;
    }

    /**
     * Busca todos los números telefónicos asociados a un usuario específico.
     * @param usuarioId ID del usuario a consultar
     * @return Lista de números del usuario especificado
     */
    public static List<NumeroUsuario> findByUsuarioId(int usuarioId) {
        // Crear lista vacía para almacenar los números encontrados
        List<NumeroUsuario> lista = new ArrayList<>();
        // Consulta SQL para buscar números por ID del usuario
        String sql = "SELECT * FROM Numeros_Usuario WHERE USUARIO_ID = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del usuario como parámetro de la consulta
            consulta.setInt(1, usuarioId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Recorrer cada registro y agregarlo a la lista como objeto NumeroUsuario
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error NumeroUsuarioDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        // Retornar la lista de números (vacía si hubo error o no hay datos)
        return lista;
    }

    /**
     * Obtiene todos los números de usuarios registrados en la base de datos.
     * @return Lista de números ordenados por ID ascendente
     */
    public static List<NumeroUsuario> findAll() {
        // Crear lista vacía para almacenar los números encontrados
        List<NumeroUsuario> lista = new ArrayList<>();
        // Consulta SQL para obtener todos los números ordenados por ID
        String sql = "SELECT * FROM Numeros_Usuario ORDER BY ID_NUMERO ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto NumeroUsuario
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error NumeroUsuarioDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de números (vacía si hubo error)
        return lista;
    }

    /**
     * Crea un nuevo número de usuario en la base de datos.
     * @param numeroUsuario Objeto NumeroUsuario con los datos a insertar
     * @return NumeroUsuario creado con su ID generado o null si falló
     */
    public static NumeroUsuario create(NumeroUsuario numeroUsuario) {
        // Consulta SQL para insertar un nuevo número de usuario
        String sql = "INSERT INTO Numeros_Usuario (NUMERO, USUARIO_ID) VALUES (?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer el número de teléfono como primer parámetro
            consulta.setString(1, numeroUsuario.getNumero());
            // Establecer el ID del usuario asociado como segundo parámetro
            consulta.setInt(2, numeroUsuario.getUsuarioId());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto numeroUsuario
                if (clavesGeneradas.next()) numeroUsuario.setIdNumero(clavesGeneradas.getInt(1));
                // Retornar el número creado con su ID asignado
                return numeroUsuario;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error NumeroUsuarioDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza un número de usuario existente en la base de datos.
     * @param numeroUsuario Objeto NumeroUsuario con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(NumeroUsuario numeroUsuario) {
        // Consulta SQL para actualizar el número de teléfono
        String sql = "UPDATE Numeros_Usuario SET NUMERO = ? WHERE ID_NUMERO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el nuevo número de teléfono como primer parámetro
            consulta.setString(1, numeroUsuario.getNumero());
            // Establecer el ID del número a actualizar como segundo parámetro
            consulta.setInt(2, numeroUsuario.getIdNumero());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error NumeroUsuarioDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina un número de usuario de la base de datos por su ID.
     * @param id ID del número a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar un número por su ID
        String sql = "DELETE FROM Numeros_Usuario WHERE ID_NUMERO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del número a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error NumeroUsuarioDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto NumeroUsuario.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto NumeroUsuario con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static NumeroUsuario mapRow(ResultSet resultado) throws SQLException {
        // Crear y retornar un nuevo objeto NumeroUsuario con los valores de las columnas
        return new NumeroUsuario(
                resultado.getInt("ID_NUMERO"),      // Obtener el ID del número
                resultado.getString("NUMERO"),       // Obtener el número de teléfono
                resultado.getInt("USUARIO_ID"));     // Obtener el ID del usuario asociado
    }
}
