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
 * DAO (Data Access Object) para operaciones sobre Movimientos_Financieros.
 * Centraliza tanto las consultas de lectura como la inserción transaccional de movimientos.
 * El método insertEnTransaccion() es invocado por VentaDAO, CompraDAO y GastoAdicionalDAO
 * dentro de sus transacciones atómicas, pasando la conexión activa para mantener la atomicidad.
 * Usa VENTA_ID, COMPRA_ID o GASTO_ADICIONAL_ID (FKs separadas) para identificar la operación origen.
 */
public class MovimientoFinancieroDAO {

    /**
     * Busca un movimiento financiero por su ID.
     * @param id ID del movimiento a buscar
     * @return MovimientoFinanciero encontrado o null si no existe
     */
    public static MovimientoFinanciero findById(int id) {
        // SQL para seleccionar un movimiento por su clave primaria
        String sql = "SELECT * FROM Movimientos_Financieros WHERE ID_MOVS_FINANCIEROS = ?";
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
        // SQL con JOIN a Tipos_Movimientos para obtener nombre del tipo y naturaleza en una sola consulta
        String sql = "SELECT mf.*, tm.MOVIMIENTO, tm.NATURALEZA " +
                "FROM Movimientos_Financieros mf " +
                "JOIN Tipos_Movimientos tm ON mf.TIPO_MOVIMIENTO_ID = tm.ID_TIPO_MOVIMIENTOS " +
                "ORDER BY mf.ID_MOVS_FINANCIEROS DESC";
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
     * Inserta un movimiento financiero usando una conexión activa de una transacción externa.
     * Se invoca desde VentaDAO, CompraDAO y GastoAdicionalDAO para centralizar el SQL
     * del INSERT en un solo lugar y mantener la atomicidad de la transacción padre.
     * Lanza SQLException para que el DAO invocador pueda hacer rollback si falla.
     *
     * @param conexion          Conexión activa con auto-commit desactivado (de la transacción padre)
     * @param concepto          Descripción del movimiento (ej: "Venta #5", "Compra #3")
     * @param monto             Monto del movimiento financiero
     * @param fecha             Fecha del movimiento en formato "YYYY-MM-DD"
     * @param tipoMovimientoId  1=Venta(Ingreso), 2=Compra(Egreso), 3=Gasto(Egreso)
     * @param ventaId           ID de la venta asociada (null si no es tipo Venta)
     * @param compraId          ID de la compra asociada (null si no es tipo Compra)
     * @param gastoAdicionalId  ID del gasto adicional asociado (null si no es tipo Gasto)
     * @throws SQLException si el INSERT falla, para que el DAO padre haga rollback
     */
    public static void insertEnTransaccion(Connection conexion, String concepto, double monto,
                                            String fecha, int tipoMovimientoId,
                                            Integer ventaId, Integer compraId,
                                            Integer gastoAdicionalId) throws SQLException {
        // SQL para insertar el movimiento financiero con las 3 FKs separadas
        String sql = "INSERT INTO Movimientos_Financieros " +
                "(CONCEPTO, MONTO, FECHA, TIPO_MOVIMIENTO_ID, VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Preparar la consulta usando la conexión de la transacción activa (no abrir una nueva)
        PreparedStatement stmt = conexion.prepareStatement(sql);
        // Asignar el concepto descriptivo del movimiento
        stmt.setString(1, concepto);
        // Asignar el monto del movimiento financiero
        stmt.setDouble(2, monto);
        // Asignar la fecha del movimiento
        stmt.setString(3, fecha);
        // Asignar el tipo de movimiento (1=Venta, 2=Compra, 3=Gasto)
        stmt.setInt(4, tipoMovimientoId);
        // Asignar el ID de la venta (null si no aplica)
        if (ventaId != null) stmt.setInt(5, ventaId); else stmt.setNull(5, Types.INTEGER);
        // Asignar el ID de la compra (null si no aplica)
        if (compraId != null) stmt.setInt(6, compraId); else stmt.setNull(6, Types.INTEGER);
        // Asignar el ID del gasto adicional (null si no aplica)
        if (gastoAdicionalId != null) stmt.setInt(7, gastoAdicionalId); else stmt.setNull(7, Types.INTEGER);
        // Ejecutar el INSERT del movimiento financiero dentro de la transacción padre
        stmt.executeUpdate();
        // Cerrar el statement para liberar recursos (la conexión sigue abierta para la transacción)
        stmt.close();
    }

    /**
     * Convierte una fila del ResultSet en un objeto MovimientoFinanciero.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto MovimientoFinanciero con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static MovimientoFinanciero mapRow(ResultSet resultado) throws SQLException {
        // Leer VENTA_ID como Integer nullable (getObject retorna null si la columna es NULL)
        Integer ventaId = (Integer) resultado.getObject("VENTA_ID");
        // Leer COMPRA_ID como Integer nullable
        Integer compraId = (Integer) resultado.getObject("COMPRA_ID");
        // Leer GASTO_ADICIONAL_ID como Integer nullable
        Integer gastoAdicionalId = (Integer) resultado.getObject("GASTO_ADICIONAL_ID");
        // Construir el MovimientoFinanciero con los datos del registro
        MovimientoFinanciero movimiento = new MovimientoFinanciero(
                // Leer el ID del movimiento desde la columna ID_MOVS_FINANCIEROS
                resultado.getInt("ID_MOVS_FINANCIEROS"),
                // Leer el concepto desde la columna CONCEPTO
                resultado.getString("CONCEPTO"),
                // Leer el monto desde la columna MONTO
                resultado.getDouble("MONTO"),
                // Leer la fecha como String desde la columna FECHA
                resultado.getString("FECHA"),
                // Leer el ID del tipo de movimiento desde la columna TIPO_MOVIMIENTO_ID
                resultado.getInt("TIPO_MOVIMIENTO_ID"),
                // Pasar el ID de la venta asociada (null si no es tipo Venta)
                ventaId,
                // Pasar el ID de la compra asociada (null si no es tipo Compra)
                compraId,
                // Pasar el ID del gasto adicional asociado (null si no es tipo Gasto)
                gastoAdicionalId);
        // Poblar campos del JOIN si las columnas existen en el ResultSet
        try {
            // Leer el nombre del tipo de movimiento desde la columna MOVIMIENTO del JOIN
            movimiento.setTipoMovimiento(resultado.getString("MOVIMIENTO"));
            // Leer la naturaleza del movimiento desde la columna NATURALEZA del JOIN
            movimiento.setNaturaleza(resultado.getString("NATURALEZA"));
        } catch (SQLException excepcion) {
            // Si las columnas no existen (ej: findById sin JOIN), ignorar silenciosamente
        }
        // Retornar el movimiento con todos los campos poblados
        return movimiento;
    }
}
