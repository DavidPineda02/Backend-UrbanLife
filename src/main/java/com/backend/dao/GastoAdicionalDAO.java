// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un gasto adicional del negocio
import com.backend.models.GastoAdicional;

// Clases JDBC para conexión, consultas preparadas, resultados, sentencias y tipos SQL
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones de Gastos_Adicionales.
 * Implementa una transacción atómica al crear un gasto: registra el gasto
 * y el movimiento financiero correspondiente (tipo_movimiento_id=3, Egreso).
 * Centraliza todo el acceso a la tabla Gastos_Adicionales.
 */
public class GastoAdicionalDAO {

    /**
     * Busca un gasto adicional por su ID.
     * @param id ID del gasto a buscar
     * @return GastoAdicional encontrado o null si no existe
     */
    public static GastoAdicional findById(int id) {
        // SQL para seleccionar un gasto por su clave primaria
        String sql = "SELECT * FROM gastos_adicionales WHERE id_gastos_adic = ?";
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
            System.out.println("Error GastoAdicionalDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el gasto
        return null;
    }

    /**
     * Obtiene todos los gastos adicionales ordenados por fecha descendente.
     * @return Lista de gastos (vacía si no hay ninguno)
     */
    public static List<GastoAdicional> findAll() {
        // Lista donde se acumularán los gastos encontrados
        List<GastoAdicional> lista = new ArrayList<>();
        // SQL para seleccionar todos los gastos ordenados por fecha descendente
        String sql = "SELECT * FROM gastos_adicionales ORDER BY fecha_registro DESC, id_gastos_adic DESC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada gasto a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error GastoAdicionalDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los gastos encontrados
        return lista;
    }

    /**
     * Crea un gasto adicional y su movimiento financiero de forma atómica.
     * Pasos: (1) INSERT en Gastos_Adicionales,
     * (2) Registrar movimiento financiero vía MovimientoFinancieroDAO.insertEnTransaccion()
     *     con tipo_movimiento_id=3 (Gasto Adicional/Egreso).
     * Si cualquier paso falla se hace rollback de toda la operación.
     * @param gasto Objeto GastoAdicional con los datos a insertar
     * @return El GastoAdicional con su ID asignado, o null si la transacción falló
     */
    public static GastoAdicional create(GastoAdicional gasto) {
        // Declarar la conexión fuera del try para poder acceder en catch y finally
        Connection conexion = null;
        try {
            // Obtener conexión a la base de datos
            conexion = dbConnection.getConnection();
            // Desactivar auto-commit para manejar la transacción manualmente
            conexion.setAutoCommit(false);

            // ===== PASO 1: INSERT en la tabla Gastos_Adicionales =====
            // SQL para insertar el gasto adicional con todos sus campos
            String sqlGasto = "INSERT INTO gastos_adicionales (monto, descripcion, fecha_registro, metodo_pago, compra_id, tipo_gasto_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            // Preparar la consulta solicitando la clave generada por la BD
            PreparedStatement stmtGasto = conexion.prepareStatement(sqlGasto, Statement.RETURN_GENERATED_KEYS);
            // Asignar el monto del gasto
            stmtGasto.setDouble(1, gasto.getMonto());
            // Asignar la descripción del gasto
            stmtGasto.setString(2, gasto.getDescripcion());
            // Asignar la fecha de registro en formato "YYYY-MM-DD"
            stmtGasto.setString(3, gasto.getFechaRegistro());
            // Asignar el método de pago ("Transferencia" o "Efectivo")
            stmtGasto.setString(4, gasto.getMetodoPago());
            // Asignar el compra_id si existe, o NULL si no aplica
            if (gasto.getCompraId() != null) {
                // Asignar el ID de la compra asociada
                stmtGasto.setInt(5, gasto.getCompraId());
            } else {
                // Asignar NULL al campo compra_id
                stmtGasto.setNull(5, Types.INTEGER);
            }
            // Asignar el ID del tipo de gasto
            stmtGasto.setInt(6, gasto.getTipoGastoId());
            // Asignar el ID del usuario que registra el gasto
            stmtGasto.setInt(7, gasto.getUsuarioId());
            // Ejecutar INSERT del gasto
            stmtGasto.executeUpdate();
            // Obtener la clave primaria generada por la BD para el gasto
            ResultSet clavesGasto = stmtGasto.getGeneratedKeys();
            // Leer el ID generado y asignarlo al objeto gasto
            if (clavesGasto.next()) gasto.setIdGastosAdic(clavesGasto.getInt(1));
            // Cerrar el statement del gasto para liberar recursos
            stmtGasto.close();

            // ===== PASO 2: Registrar movimiento financiero en la misma transacción =====
            // Delegar al MovimientoFinancieroDAO pasando la conexión activa para mantener la atomicidad
            // tipo_movimiento_id = 3 → Gasto Adicional (Egreso); venta_id y compra_id son null
            MovimientoFinancieroDAO.insertEnTransaccion(
                    conexion,
                    gasto.getFechaRegistro(),
                    "Gasto #" + gasto.getIdGastosAdic(),
                    gasto.getMonto(),
                    gasto.getMetodoPago(),
                    3,
                    gasto.getUsuarioId(),
                    null,
                    null,
                    gasto.getIdGastosAdic());

            // Confirmar todos los cambios de la transacción en la BD
            conexion.commit();
            // Retornar el gasto creado con su ID asignado
            return gasto;

        } catch (Exception excepcion) {
            // Registrar el error que causó el fallo de la transacción
            System.out.println("Error GastoAdicionalDAO.createConMovimiento: " + excepcion.getMessage());
            // Intentar revertir todos los cambios de la transacción fallida
            if (conexion != null) {
                try {
                    // Hacer rollback para dejar la BD en su estado anterior a la transacción
                    conexion.rollback();
                } catch (Exception errorRollback) {
                    // Registrar error del rollback en consola
                    System.out.println("Error en rollback GastoAdicionalDAO: " + errorRollback.getMessage());
                }
            }
            // Retornar null indicando que la transacción no se completó
            return null;
        } finally {
            // Restaurar auto-commit y cerrar la conexión siempre, incluso si hubo error
            if (conexion != null) {
                try {
                    // Restaurar el comportamiento de auto-commit de la conexión
                    conexion.setAutoCommit(true);
                    // Cerrar la conexión para devolver el recurso al pool
                    conexion.close();
                } catch (Exception errorCierre) {
                    // Registrar error de cierre en consola
                    System.out.println("Error al cerrar conexion GastoAdicionalDAO: " + errorCierre.getMessage());
                }
            }
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto GastoAdicional.
     * Maneja la columna nullable compra_id usando el patrón getInt + wasNull.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto GastoAdicional con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static GastoAdicional mapRow(ResultSet resultado) throws SQLException {
        // Leer el compra_id como int primitivo (retorna 0 si es NULL)
        int compraIdValor = resultado.getInt("compra_id");
        // Convertir a Integer nullable: null si era NULL en la BD
        Integer compraId = resultado.wasNull() ? null : compraIdValor;

        // Construir y retornar un GastoAdicional con los datos del registro actual
        return new GastoAdicional(
                // Leer el ID del gasto desde la columna id_gastos_adic
                resultado.getInt("id_gastos_adic"),
                // Leer el monto desde la columna monto
                resultado.getDouble("monto"),
                // Leer la descripción desde la columna descripcion
                resultado.getString("descripcion"),
                // Leer la fecha de registro como String desde la columna fecha_registro
                resultado.getString("fecha_registro"),
                // Leer el método de pago desde la columna metodo_pago
                resultado.getString("metodo_pago"),
                // Asignar el ID de la compra (nullable)
                compraId,
                // Leer el ID del tipo de gasto desde la columna tipo_gasto_id
                resultado.getInt("tipo_gasto_id"),
                // Leer el ID del usuario desde la columna usuario_id
                resultado.getInt("usuario_id"));
    }
}
