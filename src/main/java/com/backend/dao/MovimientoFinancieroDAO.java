// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un movimiento financiero del negocio
import com.backend.models.MovimientoFinanciero;

// Clases JDBC para conexión, consultas preparadas, resultados y tipos SQL
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para consultas de solo lectura sobre Movimientos_Financieros.
 * La creación de movimientos se maneja dentro de las transacciones atómicas
 * de VentaDAO, CompraDAO y GastoAdicionalDAO.
 * Este DAO se utiliza para consultar el historial contable del negocio.
 */
public class MovimientoFinancieroDAO {

    /**
     * Busca un movimiento financiero por su ID.
     * @param id ID del movimiento a buscar
     * @return MovimientoFinanciero encontrado o null si no existe
     */
    public static MovimientoFinanciero findById(int id) {
        // SQL para seleccionar un movimiento por su clave primaria
        String sql = "SELECT * FROM movimientos_financieros WHERE id_movs_financieros = ?";
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
            System.out.println("Error MovimientoFinancieroDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el movimiento
        return null;
    }

    /**
     * Obtiene todos los movimientos financieros ordenados por fecha descendente.
     * @return Lista de movimientos (vacía si no hay ninguno)
     */
    public static List<MovimientoFinanciero> findAll() {
        // Lista donde se acumularán los movimientos encontrados
        List<MovimientoFinanciero> lista = new ArrayList<>();
        // SQL para seleccionar todos los movimientos ordenados por fecha descendente
        String sql = "SELECT * FROM movimientos_financieros ORDER BY fecha_movimiento DESC, id_movs_financieros DESC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada movimiento a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error MovimientoFinancieroDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los movimientos encontrados
        return lista;
    }

    /**
     * Convierte una fila del ResultSet en un objeto MovimientoFinanciero.
     * Maneja las columnas nullable (venta_id, compra_id, gasto_adicional_id)
     * usando el patrón getInt + wasNull para obtener Integer nullable.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto MovimientoFinanciero con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static MovimientoFinanciero mapRow(ResultSet resultado) throws SQLException {
        // Leer el venta_id como int primitivo (retorna 0 si es NULL)
        int ventaIdValor = resultado.getInt("venta_id");
        // Convertir a Integer nullable: null si era NULL en la BD
        Integer ventaId = resultado.wasNull() ? null : ventaIdValor;

        // Leer el compra_id como int primitivo (retorna 0 si es NULL)
        int compraIdValor = resultado.getInt("compra_id");
        // Convertir a Integer nullable: null si era NULL en la BD
        Integer compraId = resultado.wasNull() ? null : compraIdValor;

        // Leer el gasto_adicional_id como int primitivo (retorna 0 si es NULL)
        int gastoIdValor = resultado.getInt("gasto_adicional_id");
        // Convertir a Integer nullable: null si era NULL en la BD
        Integer gastoAdicionalId = resultado.wasNull() ? null : gastoIdValor;

        // Construir y retornar un MovimientoFinanciero con los datos del registro
        return new MovimientoFinanciero(
                // Leer el ID del movimiento desde la columna id_movs_financieros
                resultado.getInt("id_movs_financieros"),
                // Leer la fecha del movimiento como String desde la columna fecha_movimiento
                resultado.getString("fecha_movimiento"),
                // Leer el concepto desde la columna concepto
                resultado.getString("concepto"),
                // Leer el monto desde la columna monto
                resultado.getDouble("monto"),
                // Leer el método de pago desde la columna metodo_pago
                resultado.getString("metodo_pago"),
                // Leer el ID del tipo de movimiento desde la columna tipo_movimiento_id
                resultado.getInt("tipo_movimiento_id"),
                // Leer el ID del usuario desde la columna usuario_id
                resultado.getInt("usuario_id"),
                // Asignar el ID de la venta (nullable)
                ventaId,
                // Asignar el ID de la compra (nullable)
                compraId,
                // Asignar el ID del gasto adicional (nullable)
                gastoAdicionalId);
    }
}
