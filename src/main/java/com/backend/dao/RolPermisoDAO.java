// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa la relación entre un rol y un permiso
import com.backend.models.RolPermiso;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de relaciones rol-permiso
import java.util.ArrayList;
// Para manejar colecciones de relaciones rol-permiso
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con las relaciones rol-permiso en la base de datos.
 * Gestiona la tabla intermedia que asigna permisos a roles del sistema.
 */
public class RolPermisoDAO {

    /**
     * Busca una relación rol-permiso por su ID.
     * @param id ID de la relación a buscar
     * @return Relación RolPermiso encontrada o null si no existe
     */
    public static RolPermiso findById(int id) {
        // Consulta SQL para buscar una relación rol-permiso por su ID
        String sql = "SELECT * FROM rol_permisos WHERE id_rol_permiso = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID de la relación como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto RolPermiso y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró la relación
        return null;
    }

    /**
     * Busca todas las relaciones rol-permiso asociadas a un rol específico.
     * @param rolId ID del rol a consultar
     * @return Lista de relaciones RolPermiso del rol especificado
     */
    public static List<RolPermiso> findByRolId(int rolId) {
        // Crear lista vacía para almacenar las relaciones encontradas
        List<RolPermiso> lista = new ArrayList<>();
        // Consulta SQL para buscar relaciones por ID del rol
        String sql = "SELECT * FROM rol_permisos WHERE rol_id = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del rol como parámetro de la consulta
            consulta.setInt(1, rolId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Recorrer cada registro y agregarlo a la lista como objeto RolPermiso
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.findByRolId: " + excepcion.getMessage());
        }
        // Retornar la lista de relaciones (vacía si hubo error o no hay datos)
        return lista;
    }

    /**
     * Obtiene todas las relaciones rol-permiso registradas en la base de datos.
     * @return Lista de relaciones ordenadas por ID ascendente
     */
    public static List<RolPermiso> findAll() {
        // Crear lista vacía para almacenar las relaciones encontradas
        List<RolPermiso> lista = new ArrayList<>();
        // Consulta SQL para obtener todas las relaciones ordenadas por ID
        String sql = "SELECT * FROM rol_permisos ORDER BY id_rol_permiso ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto RolPermiso
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de relaciones (vacía si hubo error)
        return lista;
    }

    /**
     * Crea una nueva relación rol-permiso en la base de datos.
     * @param rolPermiso Objeto RolPermiso con los datos a insertar
     * @return RolPermiso creado con su ID generado o null si falló
     */
    public static RolPermiso create(RolPermiso rolPermiso) {
        // Consulta SQL para insertar una nueva relación rol-permiso
        String sql = "INSERT INTO rol_permisos (rol_id, permisos_id) VALUES (?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer el ID del rol como primer parámetro
            consulta.setInt(1, rolPermiso.getRolId());
            // Establecer el ID del permiso como segundo parámetro
            consulta.setInt(2, rolPermiso.getPermisosId());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto rolPermiso
                if (clavesGeneradas.next()) rolPermiso.setIdRolPermiso(clavesGeneradas.getInt(1));
                // Retornar la relación creada con su ID asignado
                return rolPermiso;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza una relación rol-permiso existente en la base de datos.
     * @param rolPermiso Objeto RolPermiso con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(RolPermiso rolPermiso) {
        // Consulta SQL para actualizar los IDs de rol y permiso de una relación
        String sql = "UPDATE rol_permisos SET rol_id = ?, permisos_id = ? WHERE id_rol_permiso = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el nuevo ID del rol como primer parámetro
            consulta.setInt(1, rolPermiso.getRolId());
            // Establecer el nuevo ID del permiso como segundo parámetro
            consulta.setInt(2, rolPermiso.getPermisosId());
            // Establecer el ID de la relación a actualizar como tercer parámetro
            consulta.setInt(3, rolPermiso.getIdRolPermiso());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina una relación rol-permiso de la base de datos por su ID.
     * @param id ID de la relación a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar una relación por su ID
        String sql = "DELETE FROM rol_permisos WHERE id_rol_permiso = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID de la relación a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Elimina todas las relaciones rol-permiso asociadas a un rol específico.
     * @param rolId ID del rol cuyas relaciones se eliminarán
     * @return true si se eliminaron correctamente, false si falló
     */
    public static boolean deleteByRolId(int rolId) {
        // Consulta SQL para eliminar todas las relaciones de un rol específico
        String sql = "DELETE FROM rol_permisos WHERE rol_id = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del rol como parámetro de la consulta
            consulta.setInt(1, rolId);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error RolPermisoDAO.deleteByRolId: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto RolPermiso.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto RolPermiso con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static RolPermiso mapRow(ResultSet resultado) throws SQLException {
        // Crear y retornar un nuevo objeto RolPermiso con los valores de las columnas
        return new RolPermiso(
                resultado.getInt("id_rol_permiso"),  // Obtener el ID de la relación
                resultado.getInt("rol_id"),           // Obtener el ID del rol
                resultado.getInt("permisos_id"));     // Obtener el ID del permiso
    }
}
