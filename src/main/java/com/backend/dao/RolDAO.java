// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un rol del sistema
import com.backend.models.Rol;

// Clases JDBC para conexión, consultas preparadas, resultados y sentencias
import java.sql.*;
// Lista dinámica para retornar múltiples roles
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con roles en la base de datos.
 * Centraliza todo el acceso a la tabla Roles evitando SQL disperso en capas superiores.
 */
public class RolDAO {

    /**
     * Busca un rol por su ID.
     * @param id ID del rol a buscar
     * @return Rol encontrado o null si no existe
     */
    public static Rol findById(int id) {
        // SQL para seleccionar un rol por su clave primaria
        String sql = "SELECT * FROM Roles WHERE ID_ROLES = ?";
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
            System.out.println("Error RolDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el rol
        return null;
    }

    /**
     * Busca un rol por su nombre exacto.
     * @param nombre Nombre del rol a buscar (ej: "SUPER_ADMIN")
     * @return Rol encontrado o null si no existe
     */
    public static Rol findByNombre(String nombre) {
        // SQL para seleccionar un rol filtrando por nombre
        String sql = "SELECT * FROM Roles WHERE NOMBRE_ROL = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nombre como parámetro de búsqueda
            consulta.setString(1, nombre);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error RolDAO.findByNombre: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el rol
        return null;
    }

    /**
     * Obtiene todos los roles de la base de datos ordenados por ID ascendente.
     * @return Lista de roles (vacía si no hay ninguno)
     */
    public static List<Rol> findAll() {
        // Lista donde se acumularán los roles encontrados
        List<Rol> lista = new ArrayList<>();
        // SQL para seleccionar todos los roles ordenados por ID
        String sql = "SELECT * FROM Roles ORDER BY ID_ROLES ASC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada rol a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error RolDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los roles encontrados
        return lista;
    }

    /**
     * Inserta un nuevo rol en la base de datos y asigna el ID generado.
     * @param rol Objeto Rol con nombre y descripción a insertar
     * @return El mismo Rol con su ID asignado, o null si falló la inserción
     */
    public static Rol create(Rol rol) {
        // SQL para insertar un nuevo rol con nombre y descripción
        String sql = "INSERT INTO Roles (NOMBRE_ROL, DESCRIPCION_ROL) VALUES (?, ?)";
        // Abrir conexión y preparar consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre del rol
            consulta.setString(1, rol.getNombre());
            // Asignar la descripción del rol
            consulta.setString(2, rol.getDescripcion());
            // Ejecutar INSERT y verificar que se insertó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por la BD (ID auto-incrementado)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si se obtuvo la clave generada, asignarla al objeto
                if (clavesGeneradas.next()) rol.setIdRoles(clavesGeneradas.getInt(1));
                // Retornar el rol con su ID asignado
                return rol;
            }
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error RolDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la inserción falló
        return null;
    }

    /**
     * Actualiza el nombre y descripción de un rol existente.
     * @param rol Objeto Rol con los nuevos datos y el ID a actualizar
     * @return true si se actualizó al menos una fila, false en caso contrario
     */
    public static boolean update(Rol rol) {
        // SQL para actualizar nombre y descripción de un rol por su ID
        String sql = "UPDATE Roles SET NOMBRE_ROL = ?, DESCRIPCION_ROL = ? WHERE ID_ROLES = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo nombre del rol
            consulta.setString(1, rol.getNombre());
            // Asignar la nueva descripción del rol
            consulta.setString(2, rol.getDescripcion());
            // Asignar el ID del rol a actualizar como condición WHERE
            consulta.setInt(3, rol.getIdRoles());
            // Ejecutar UPDATE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error RolDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina un rol de la base de datos por su ID.
     * @param id ID del rol a eliminar
     * @return true si se eliminó al menos una fila, false en caso contrario
     */
    public static boolean delete(int id) {
        // SQL para eliminar un rol por su clave primaria
        String sql = "DELETE FROM Roles WHERE ID_ROLES = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del rol a eliminar
            consulta.setInt(1, id);
            // Ejecutar DELETE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error RolDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Rol.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Rol con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Rol mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un Rol con los datos del registro actual
        return new Rol(
                // Leer el ID del rol desde la columna ID_ROLES
                resultado.getInt("ID_ROLES"),
                // Leer el nombre del rol desde la columna NOMBRE_ROL
                resultado.getString("NOMBRE_ROL"),
                // Leer la descripción del rol desde la columna DESCRIPCION_ROL
                resultado.getString("DESCRIPCION_ROL"));
    }
}
