// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa un permiso del sistema
import com.backend.models.Permiso;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de permisos
import java.util.ArrayList;
// Para manejar colecciones de permisos
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con los permisos en la base de datos.
 * Los permisos definen acciones específicas que pueden realizar los roles del sistema.
 */
public class PermisoDAO {

    /**
     * Busca un permiso por su ID en la base de datos.
     * @param id ID del permiso a buscar
     * @return Permiso encontrado o null si no existe
     */
    public static Permiso findById(int id) {
        // Consulta SQL para buscar un permiso por su ID
        String sql = "SELECT * FROM permisos WHERE id_permisos = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del permiso como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto Permiso y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error PermisoDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el permiso
        return null;
    }

    /**
     * Obtiene todos los permisos registrados en la base de datos.
     * @return Lista de permisos ordenados por ID ascendente
     */
    public static List<Permiso> findAll() {
        // Crear lista vacía para almacenar los permisos encontrados
        List<Permiso> lista = new ArrayList<>();
        // Consulta SQL para obtener todos los permisos ordenados por ID
        String sql = "SELECT * FROM permisos ORDER BY id_permisos ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto Permiso
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error PermisoDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de permisos (vacía si hubo error)
        return lista;
    }

    /**
     * Crea un nuevo permiso en la base de datos.
     * @param permiso Objeto Permiso con los datos a insertar
     * @return Permiso creado con su ID generado o null si falló
     */
    public static Permiso create(Permiso permiso) {
        // Consulta SQL para insertar un nuevo permiso
        String sql = "INSERT INTO permisos (nombre, descripcion) VALUES (?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer el nombre del permiso como primer parámetro
            consulta.setString(1, permiso.getNombre());
            // Establecer la descripción del permiso como segundo parámetro
            consulta.setString(2, permiso.getDescripcion());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto permiso
                if (clavesGeneradas.next()) permiso.setIdPermisos(clavesGeneradas.getInt(1));
                // Retornar el permiso creado con su ID asignado
                return permiso;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error PermisoDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza un permiso existente en la base de datos.
     * @param permiso Objeto Permiso con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(Permiso permiso) {
        // Consulta SQL para actualizar nombre y descripción de un permiso
        String sql = "UPDATE permisos SET nombre = ?, descripcion = ? WHERE id_permisos = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el nuevo nombre como primer parámetro
            consulta.setString(1, permiso.getNombre());
            // Establecer la nueva descripción como segundo parámetro
            consulta.setString(2, permiso.getDescripcion());
            // Establecer el ID del permiso a actualizar como tercer parámetro
            consulta.setInt(3, permiso.getIdPermisos());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error PermisoDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina un permiso de la base de datos por su ID.
     * @param id ID del permiso a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar un permiso por su ID
        String sql = "DELETE FROM permisos WHERE id_permisos = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del permiso a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error PermisoDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Permiso.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Permiso con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static Permiso mapRow(ResultSet resultado) throws SQLException {
        // Crear y retornar un nuevo objeto Permiso con los valores de las columnas
        return new Permiso(
                resultado.getInt("id_permisos"),       // Obtener el ID del permiso
                resultado.getString("nombre"),          // Obtener el nombre del permiso
                resultado.getString("descripcion"));    // Obtener la descripción del permiso
    }
}
