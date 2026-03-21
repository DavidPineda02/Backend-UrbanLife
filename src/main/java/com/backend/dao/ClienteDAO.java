// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un cliente del negocio
import com.backend.models.Cliente;

// Clases JDBC para conexión, consultas preparadas, resultados, sentencias y tipos SQL
import java.sql.*;
// Lista dinámica para retornar múltiples clientes
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con clientes en la base de datos.
 * Centraliza todo el acceso a la tabla Clientes evitando SQL disperso en capas superiores.
 */
public class ClienteDAO {

    /**
     * Busca un cliente por su ID.
     * @param id ID del cliente a buscar
     * @return Cliente encontrado o null si no existe
     */
    public static Cliente findById(int id) {
        // SQL para seleccionar un cliente por su clave primaria
        String sql = "SELECT * FROM Clientes WHERE ID_CLIENTE = ?";
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
            System.out.println("Error ClienteDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el cliente
        return null;
    }

    /**
     * Obtiene todos los clientes de la base de datos ordenados por ID ascendente.
     * @return Lista de clientes (vacía si no hay ninguno)
     */
    public static List<Cliente> findAll() {
        // Lista donde se acumularán los clientes encontrados
        List<Cliente> lista = new ArrayList<>();
        // SQL para seleccionar todos los clientes ordenados por ID
        String sql = "SELECT * FROM Clientes ORDER BY ID_CLIENTE ASC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada cliente a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ClienteDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los clientes encontrados
        return lista;
    }

    /**
     * Busca un cliente por su número de documento de identidad.
     * Usado para verificar unicidad antes de crear o actualizar.
     * @param documento Número de documento a buscar (Long)
     * @return Cliente encontrado o null si no existe
     */
    public static Cliente findByDocumento(Long documento) {
        // SQL para seleccionar un cliente filtrando por número de documento
        String sql = "SELECT * FROM Clientes WHERE DOCUMENTO_CLIENTE = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el documento numérico como parámetro de búsqueda
            consulta.setLong(1, documento);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ClienteDAO.findByDocumento: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el cliente
        return null;
    }

    /**
     * Busca un cliente por su correo electrónico.
     * Usado para verificar unicidad antes de crear o actualizar.
     * @param correo Correo electrónico a buscar
     * @return Cliente encontrado o null si no existe
     */
    public static Cliente findByCorreo(String correo) {
        // SQL para seleccionar un cliente filtrando por correo electrónico
        String sql = "SELECT * FROM Clientes WHERE CORREO_CLIENTE = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el correo como parámetro de búsqueda
            consulta.setString(1, correo);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ClienteDAO.findByCorreo: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el cliente
        return null;
    }

    /**
     * Busca un cliente por su número de teléfono.
     * Usado para verificar unicidad antes de crear o actualizar.
     * @param telefono Número de teléfono a buscar
     * @return Cliente encontrado o null si no existe
     */
    public static Cliente findByTelefono(String telefono) {
        // SQL para seleccionar un cliente filtrando por número de teléfono
        String sql = "SELECT * FROM Clientes WHERE TELEFONO_CLIENTE = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el teléfono como parámetro de búsqueda
            consulta.setString(1, telefono);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ClienteDAO.findByTelefono: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el cliente
        return null;
    }

    /**
     * Inserta un nuevo cliente en la base de datos y asigna el ID generado.
     * @param cliente Objeto Cliente con todos los campos a insertar
     * @return El mismo Cliente con su ID asignado, o null si falló la inserción
     */
    public static Cliente create(Cliente cliente) {
        // SQL para insertar un nuevo cliente con todos sus campos
        String sql = "INSERT INTO Clientes (NOMBRE_CLIENTE, DOCUMENTO_CLIENTE, CORREO_CLIENTE, TELEFONO_CLIENTE, DIRECCION_CLIENTE, CIUDAD_CLIENTE, ESTADO_CLIENTE) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Abrir conexión y preparar consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre del cliente
            consulta.setString(1, cliente.getNombre());
            // Asignar el documento numérico del cliente (null si no tiene)
            if (cliente.getDocumento() != null) {
                // Asignar el valor Long del documento
                consulta.setLong(2, cliente.getDocumento());
            } else {
                // Asignar NULL al campo documento
                consulta.setNull(2, Types.BIGINT);
            }
            // Asignar el correo del cliente
            consulta.setString(3, cliente.getCorreo());
            // Asignar el teléfono del cliente
            consulta.setString(4, cliente.getTelefono());
            // Asignar la dirección del cliente
            consulta.setString(5, cliente.getDireccion());
            // Asignar la ciudad del cliente
            consulta.setString(6, cliente.getCiudad());
            // Asignar el estado del cliente
            consulta.setBoolean(7, cliente.isEstado());
            // Ejecutar INSERT y verificar que se insertó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por la BD (ID auto-incrementado)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si se obtuvo la clave generada, asignarla al objeto
                if (clavesGeneradas.next()) cliente.setIdCliente(clavesGeneradas.getInt(1));
                // Retornar el cliente con su ID asignado
                return cliente;
            }
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ClienteDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la inserción falló
        return null;
    }

    /**
     * Actualiza todos los campos de un cliente existente en la base de datos.
     * @param cliente Objeto Cliente con los nuevos datos y el ID a actualizar
     * @return true si se actualizó al menos una fila, false en caso contrario
     */
    public static boolean update(Cliente cliente) {
        // SQL para actualizar todos los campos del cliente por su ID
        String sql = "UPDATE Clientes SET NOMBRE_CLIENTE = ?, DOCUMENTO_CLIENTE = ?, CORREO_CLIENTE = ?, TELEFONO_CLIENTE = ?, DIRECCION_CLIENTE = ?, CIUDAD_CLIENTE = ?, ESTADO_CLIENTE = ? WHERE ID_CLIENTE = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo nombre del cliente
            consulta.setString(1, cliente.getNombre());
            // Asignar el nuevo documento numérico del cliente (null si no tiene)
            if (cliente.getDocumento() != null) {
                // Asignar el valor Long del documento
                consulta.setLong(2, cliente.getDocumento());
            } else {
                // Asignar NULL al campo documento
                consulta.setNull(2, Types.BIGINT);
            }
            // Asignar el nuevo correo del cliente
            consulta.setString(3, cliente.getCorreo());
            // Asignar el nuevo teléfono del cliente
            consulta.setString(4, cliente.getTelefono());
            // Asignar la nueva dirección del cliente
            consulta.setString(5, cliente.getDireccion());
            // Asignar la nueva ciudad del cliente
            consulta.setString(6, cliente.getCiudad());
            // Asignar el nuevo estado del cliente
            consulta.setBoolean(7, cliente.isEstado());
            // Asignar el ID del cliente a actualizar como condición WHERE
            consulta.setInt(8, cliente.getIdCliente());
            // Ejecutar UPDATE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ClienteDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Cliente.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Cliente con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Cliente mapRow(ResultSet resultado) throws SQLException {
        // Leer el documento numérico (BIGINT nullable) desde la columna DOCUMENTO_CLIENTE
        long docValor = resultado.getLong("DOCUMENTO_CLIENTE");
        // Si el valor fue NULL en la BD, getLong retorna 0 y wasNull() retorna true
        Long documento = resultado.wasNull() ? null : docValor;
        // Construir y retornar un Cliente con los datos del registro actual
        return new Cliente(
                // Leer el ID del cliente desde la columna ID_CLIENTE
                resultado.getInt("ID_CLIENTE"),
                // Leer el nombre desde la columna NOMBRE_CLIENTE
                resultado.getString("NOMBRE_CLIENTE"),
                // Asignar el documento Long (null si era NULL en la BD)
                documento,
                // Leer el correo desde la columna CORREO_CLIENTE
                resultado.getString("CORREO_CLIENTE"),
                // Leer el teléfono desde la columna TELEFONO_CLIENTE
                resultado.getString("TELEFONO_CLIENTE"),
                // Leer la dirección desde la columna DIRECCION_CLIENTE
                resultado.getString("DIRECCION_CLIENTE"),
                // Leer la ciudad desde la columna CIUDAD_CLIENTE
                resultado.getString("CIUDAD_CLIENTE"),
                // Leer el estado desde la columna ESTADO_CLIENTE
                resultado.getBoolean("ESTADO_CLIENTE"));
    }
}
