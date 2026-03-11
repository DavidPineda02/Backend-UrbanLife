// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa un tipo de movimiento financiero
import com.backend.models.TipoMovimiento;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de tipos de movimiento
import java.util.ArrayList;
// Para manejar colecciones de tipos de movimiento
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con los tipos de movimientos financieros en la base de datos.
 * Los tipos de movimiento definen la naturaleza de las transacciones:
 * 1=Venta(Ingreso), 2=Compra(Egreso), 3=Gasto Adicional(Egreso).
 */
public class TipoMovimientoDAO {

    /**
     * Busca un tipo de movimiento por su ID en la base de datos.
     * @param id ID del tipo de movimiento a buscar
     * @return TipoMovimiento encontrado o null si no existe
     */
    public static TipoMovimiento findById(int id) {
        // Consulta SQL para buscar un tipo de movimiento por su ID
        String sql = "SELECT * FROM tipo_movimientos WHERE id_tipo_movimientos = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del tipo de movimiento como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto TipoMovimiento y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TipoMovimientoDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el tipo de movimiento
        return null;
    }

    /**
     * Obtiene todos los tipos de movimientos registrados en la base de datos.
     * @return Lista de tipos de movimiento ordenados por ID ascendente
     */
    public static List<TipoMovimiento> findAll() {
        // Crear lista vacía para almacenar los tipos de movimiento encontrados
        List<TipoMovimiento> lista = new ArrayList<>();
        // Consulta SQL para obtener todos los tipos de movimiento ordenados por ID
        String sql = "SELECT * FROM tipo_movimientos ORDER BY id_tipo_movimientos ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto TipoMovimiento
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TipoMovimientoDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de tipos de movimiento (vacía si hubo error)
        return lista;
    }

    /**
     * Crea un nuevo tipo de movimiento en la base de datos.
     * @param tipoMovimiento Objeto TipoMovimiento con los datos a insertar
     * @return TipoMovimiento creado con su ID generado o null si falló
     */
    public static TipoMovimiento create(TipoMovimiento tipoMovimiento) {
        // Consulta SQL para insertar un nuevo tipo de movimiento
        String sql = "INSERT INTO tipo_movimientos (movimiento, naturaleza) VALUES (?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer el nombre del movimiento como primer parámetro
            consulta.setString(1, tipoMovimiento.getMovimiento());
            // Establecer la naturaleza (Ingreso/Egreso) como segundo parámetro
            consulta.setString(2, tipoMovimiento.getNaturaleza());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto tipoMovimiento
                if (clavesGeneradas.next()) tipoMovimiento.setIdTipoMovimientos(clavesGeneradas.getInt(1));
                // Retornar el tipo de movimiento creado con su ID asignado
                return tipoMovimiento;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TipoMovimientoDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza un tipo de movimiento existente en la base de datos.
     * @param tipoMovimiento Objeto TipoMovimiento con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(TipoMovimiento tipoMovimiento) {
        // Consulta SQL para actualizar movimiento y naturaleza de un tipo
        String sql = "UPDATE tipo_movimientos SET movimiento = ?, naturaleza = ? WHERE id_tipo_movimientos = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el nuevo nombre del movimiento como primer parámetro
            consulta.setString(1, tipoMovimiento.getMovimiento());
            // Establecer la nueva naturaleza como segundo parámetro
            consulta.setString(2, tipoMovimiento.getNaturaleza());
            // Establecer el ID del tipo a actualizar como tercer parámetro
            consulta.setInt(3, tipoMovimiento.getIdTipoMovimientos());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TipoMovimientoDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina un tipo de movimiento de la base de datos por su ID.
     * @param id ID del tipo de movimiento a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar un tipo de movimiento por su ID
        String sql = "DELETE FROM tipo_movimientos WHERE id_tipo_movimientos = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del tipo a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TipoMovimientoDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto TipoMovimiento.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto TipoMovimiento con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static TipoMovimiento mapRow(ResultSet resultado) throws SQLException {
        // Crear y retornar un nuevo objeto TipoMovimiento con los valores de las columnas
        return new TipoMovimiento(
                resultado.getInt("id_tipo_movimientos"),  // Obtener el ID del tipo de movimiento
                resultado.getString("movimiento"),         // Obtener el nombre del movimiento
                resultado.getString("naturaleza"));        // Obtener la naturaleza (Ingreso/Egreso)
    }
}
