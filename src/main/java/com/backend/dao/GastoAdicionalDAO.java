// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un gasto adicional del negocio
import com.backend.models.GastoAdicional;

// Clases JDBC para conexión, consultas preparadas, resultados y sentencias
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
        String sql = "SELECT * FROM Gastos_Adicionales WHERE ID_GASTOS_ADIC = ?";
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
        String sql = "SELECT * FROM Gastos_Adicionales ORDER BY FECHA_REGISTRO DESC, ID_GASTOS_ADIC DESC";
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
     * Pasos: (1) INSERT en Gastos_Adicionales con USUARIO_ID,
     * (2) Registrar movimiento financiero vía MovimientoFinancieroDAO.insertEnTransaccion()
     *     con tipo_movimiento_id=3 (Gasto Adicional/Egreso).
     * Si cualquier paso falla se hace rollback de toda la operación.
     * @param gasto Objeto GastoAdicional con los datos a insertar (incluye usuarioId)
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
            // SQL para insertar el gasto adicional con sus campos incluyendo USUARIO_ID
            String sqlGasto = "INSERT INTO Gastos_Adicionales (MONTO, DESCRIPCION_GASTO, FECHA_REGISTRO, METODO_PAGO_GASTO, USUARIO_ID) VALUES (?, ?, ?, ?, ?)";
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
            // Asignar el ID del usuario que registra el gasto
            stmtGasto.setInt(5, gasto.getUsuarioId());
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
            // tipo_movimiento_id = 3 → Gasto Adicional (Egreso); gastoAdicionalId = ID del gasto creado, ventaId y compraId = null
            MovimientoFinancieroDAO.insertEnTransaccion(
                    conexion,
                    "Gasto #" + gasto.getIdGastosAdic(),
                    gasto.getMonto(),
                    gasto.getFechaRegistro(),
                    3,
                    null, null, gasto.getIdGastosAdic());

            // Confirmar todos los cambios de la transacción en la BD
            conexion.commit();
            // Retornar el gasto creado con su ID asignado
            return gasto;

        } catch (Exception excepcion) {
            // Registrar el error que causó el fallo de la transacción
            System.out.println("Error GastoAdicionalDAO.create: " + excepcion.getMessage());
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
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto GastoAdicional con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static GastoAdicional mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un GastoAdicional con los datos del registro actual
        return new GastoAdicional(
                // Leer el ID del gasto desde la columna ID_GASTOS_ADIC
                resultado.getInt("ID_GASTOS_ADIC"),
                // Leer el monto desde la columna MONTO
                resultado.getDouble("MONTO"),
                // Leer la descripción desde la columna DESCRIPCION_GASTO
                resultado.getString("DESCRIPCION_GASTO"),
                // Leer la fecha de registro como String desde la columna FECHA_REGISTRO
                resultado.getString("FECHA_REGISTRO"),
                // Leer el método de pago desde la columna METODO_PAGO_GASTO
                resultado.getString("METODO_PAGO_GASTO"),
                // Leer el ID del usuario desde la columna USUARIO_ID
                resultado.getInt("USUARIO_ID"));
    }
}
