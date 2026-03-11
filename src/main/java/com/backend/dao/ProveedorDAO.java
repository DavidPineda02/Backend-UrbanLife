// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un proveedor del negocio
import com.backend.models.Proveedor;

// Clases JDBC para conexión, consultas preparadas, resultados y sentencias
import java.sql.*;
// Lista dinámica para retornar múltiples proveedores
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con proveedores en la base de datos.
 * Centraliza todo el acceso a la tabla Proveedores evitando SQL disperso en capas superiores.
 */
public class ProveedorDAO {

    /**
     * Busca un proveedor por su ID.
     * @param id ID del proveedor a buscar
     * @return Proveedor encontrado o null si no existe
     */
    public static Proveedor findById(int id) {
        // SQL para seleccionar un proveedor por su clave primaria
        String sql = "SELECT * FROM proveedores WHERE id_proveedor = ?";
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
            System.out.println("Error ProveedorDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el proveedor
        return null;
    }

    /**
     * Obtiene todos los proveedores de la base de datos ordenados por ID ascendente.
     * @return Lista de proveedores (vacía si no hay ninguno)
     */
    public static List<Proveedor> findAll() {
        // Lista donde se acumularán los proveedores encontrados
        List<Proveedor> lista = new ArrayList<>();
        // SQL para seleccionar todos los proveedores ordenados por ID
        String sql = "SELECT * FROM proveedores ORDER BY id_proveedor ASC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada proveedor a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProveedorDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los proveedores encontrados
        return lista;
    }

    /**
     * Busca un proveedor por su número de NIT.
     * Usado para verificar unicidad antes de crear o actualizar.
     * @param nit Número de identificación tributaria a buscar
     * @return Proveedor encontrado o null si no existe
     */
    public static Proveedor findByNit(String nit) {
        // SQL para seleccionar un proveedor filtrando por NIT
        String sql = "SELECT * FROM proveedores WHERE nit = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el NIT como parámetro de búsqueda
            consulta.setString(1, nit);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProveedorDAO.findByNit: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el proveedor
        return null;
    }

    /**
     * Inserta un nuevo proveedor en la base de datos y asigna el ID generado.
     * @param proveedor Objeto Proveedor con todos los campos a insertar
     * @return El mismo Proveedor con su ID asignado, o null si falló la inserción
     */
    public static Proveedor create(Proveedor proveedor) {
        // SQL para insertar un nuevo proveedor con todos sus campos
        String sql = "INSERT INTO proveedores (nombre, razon_social, nit, correo, telefono, direccion, ciudad, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // Abrir conexión y preparar consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre comercial del proveedor
            consulta.setString(1, proveedor.getNombre());
            // Asignar la razón social del proveedor
            consulta.setString(2, proveedor.getRazonSocial());
            // Asignar el NIT del proveedor
            consulta.setString(3, proveedor.getNit());
            // Asignar el correo del proveedor
            consulta.setString(4, proveedor.getCorreo());
            // Asignar el teléfono del proveedor
            consulta.setString(5, proveedor.getTelefono());
            // Asignar la dirección del proveedor
            consulta.setString(6, proveedor.getDireccion());
            // Asignar la ciudad del proveedor
            consulta.setString(7, proveedor.getCiudad());
            // Asignar el estado del proveedor
            consulta.setBoolean(8, proveedor.isEstado());
            // Ejecutar INSERT y verificar que se insertó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por la BD (ID auto-incrementado)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si se obtuvo la clave generada, asignarla al objeto
                if (clavesGeneradas.next()) proveedor.setIdProveedor(clavesGeneradas.getInt(1));
                // Retornar el proveedor con su ID asignado
                return proveedor;
            }
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProveedorDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la inserción falló
        return null;
    }

    /**
     * Actualiza todos los campos de un proveedor existente en la base de datos.
     * @param proveedor Objeto Proveedor con los nuevos datos y el ID a actualizar
     * @return true si se actualizó al menos una fila, false en caso contrario
     */
    public static boolean update(Proveedor proveedor) {
        // SQL para actualizar todos los campos del proveedor por su ID
        String sql = "UPDATE proveedores SET nombre = ?, razon_social = ?, nit = ?, correo = ?, telefono = ?, direccion = ?, ciudad = ?, estado = ? WHERE id_proveedor = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo nombre comercial del proveedor
            consulta.setString(1, proveedor.getNombre());
            // Asignar la nueva razón social del proveedor
            consulta.setString(2, proveedor.getRazonSocial());
            // Asignar el nuevo NIT del proveedor
            consulta.setString(3, proveedor.getNit());
            // Asignar el nuevo correo del proveedor
            consulta.setString(4, proveedor.getCorreo());
            // Asignar el nuevo teléfono del proveedor
            consulta.setString(5, proveedor.getTelefono());
            // Asignar la nueva dirección del proveedor
            consulta.setString(6, proveedor.getDireccion());
            // Asignar la nueva ciudad del proveedor
            consulta.setString(7, proveedor.getCiudad());
            // Asignar el nuevo estado del proveedor
            consulta.setBoolean(8, proveedor.isEstado());
            // Asignar el ID del proveedor a actualizar como condición WHERE
            consulta.setInt(9, proveedor.getIdProveedor());
            // Ejecutar UPDATE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProveedorDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Proveedor.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Proveedor con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Proveedor mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un Proveedor con los datos del registro actual
        return new Proveedor(
                // Leer el ID del proveedor desde la columna id_proveedor
                resultado.getInt("id_proveedor"),
                // Leer el nombre desde la columna nombre
                resultado.getString("nombre"),
                // Leer la razón social desde la columna razon_social
                resultado.getString("razon_social"),
                // Leer el NIT desde la columna nit
                resultado.getString("nit"),
                // Leer el correo desde la columna correo
                resultado.getString("correo"),
                // Leer el teléfono desde la columna telefono
                resultado.getString("telefono"),
                // Leer la dirección desde la columna direccion
                resultado.getString("direccion"),
                // Leer la ciudad desde la columna ciudad
                resultado.getString("ciudad"),
                // Leer el estado desde la columna estado
                resultado.getBoolean("estado"));
    }
}
