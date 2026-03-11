// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un tipo de gasto (categoría de gasto)
import com.backend.models.TipoGasto;

// Clases JDBC para conexión, consultas preparadas y resultados
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para consultas de solo lectura sobre Tipo_Gasto.
 * Los tipos de gasto se insertan mediante seeders y sirven para clasificar gastos adicionales.
 * Centraliza todo el acceso a la tabla Tipo_Gasto.
 */
public class TipoGastoDAO {

    /**
     * Busca un tipo de gasto por su ID.
     * @param id ID del tipo de gasto a buscar
     * @return TipoGasto encontrado o null si no existe
     */
    public static TipoGasto findById(int id) {
        // SQL para seleccionar un tipo de gasto por su clave primaria
        String sql = "SELECT * FROM tipo_gasto WHERE id_tipo_gasto = ?";
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
            System.out.println("Error TipoGastoDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el tipo de gasto
        return null;
    }

    /**
     * Obtiene todos los tipos de gasto ordenados por ID ascendente.
     * @return Lista de tipos de gasto (vacía si no hay ninguno)
     */
    public static List<TipoGasto> findAll() {
        // Lista donde se acumularán los tipos de gasto encontrados
        List<TipoGasto> lista = new ArrayList<>();
        // SQL para seleccionar todos los tipos de gasto ordenados por ID ascendente
        String sql = "SELECT * FROM tipo_gasto ORDER BY id_tipo_gasto ASC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada tipo de gasto a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error TipoGastoDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los tipos de gasto encontrados
        return lista;
    }

    /**
     * Convierte una fila del ResultSet en un objeto TipoGasto.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto TipoGasto con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static TipoGasto mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un TipoGasto con los datos del registro actual
        return new TipoGasto(
                // Leer el ID del tipo de gasto desde la columna id_tipo_gasto
                resultado.getInt("id_tipo_gasto"),
                // Leer el nombre desde la columna nombre
                resultado.getString("nombre"),
                // Leer la descripción desde la columna descripcion
                resultado.getString("descripcion"));
    }
}
