// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un ítem de una compra
import com.backend.models.DetalleCompra;
// Modelo que representa una compra a proveedor
import com.backend.models.Compra;

// Clases JDBC para conexión, consultas preparadas, resultados, sentencias y tipos SQL
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones de Compras y Detalle_Compra
 * en la base de datos. Implementa una transacción atómica al crear una compra:
 * registra el encabezado, sus ítems, aumenta el stock de cada producto,
 * actualiza el costo promedio y registra el movimiento financiero correspondiente.
 * Centraliza todo el acceso a las tablas Compra y Detalle_Compra.
 */
public class CompraDAO {

    /**
     * Busca una compra por su ID.
     * @param id ID de la compra a buscar
     * @return Compra encontrada o null si no existe
     */
    public static Compra findById(int id) {
        // SQL para seleccionar una compra por su clave primaria
        String sql = "SELECT * FROM Compras WHERE ID_COMPRA = ?";
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
            System.out.println("Error CompraDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró la compra
        return null;
    }

    /**
     * Obtiene todas las compras de la base de datos ordenadas por fecha descendente.
     * @return Lista de compras (vacía si no hay ninguna)
     */
    public static List<Compra> findAll() {
        // Lista donde se acumularán las compras encontradas
        List<Compra> lista = new ArrayList<>();
        // SQL con JOIN a Proveedores para obtener el nombre del proveedor en una sola consulta
        String sql = "SELECT co.*, p.NOMBRE_PROVEEDOR " +
                "FROM Compras co " +
                "JOIN Proveedores p ON co.PROVEEDOR_ID = p.ID_PROVEEDOR " +
                "ORDER BY co.FECHA_COMPRA DESC, co.ID_COMPRA DESC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada compra a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error CompraDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todas las compras encontradas
        return lista;
    }

    /**
     * Obtiene todos los ítems (detalles) que pertenecen a una compra específica.
     * @param compraId ID de la compra cuyos detalles se desean obtener
     * @return Lista de DetalleCompra (vacía si no hay ninguno)
     */
    public static List<DetalleCompra> findDetallesByCompraId(int compraId) {
        // Lista donde se acumularán los detalles encontrados
        List<DetalleCompra> lista = new ArrayList<>();
        // SQL para seleccionar todos los ítems de una compra específica
        String sql = "SELECT * FROM Detalles_Compras WHERE COMPRA_ID = ? ORDER BY ID_DET_COMPRA ASC";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID de la compra como parámetro de búsqueda
            consulta.setInt(1, compraId);
            // Ejecutar consulta y obtener resultados
            ResultSet resultado = consulta.executeQuery();
            // Recorrer todos los registros y agregar cada detalle a la lista
            while (resultado.next()) lista.add(mapDetalleRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error CompraDAO.findDetallesByCompraId: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los detalles de la compra
        return lista;
    }

    /**
     * Crea una compra completa de forma atómica en una sola transacción de base de datos.
     * Pasos: (1) INSERT en Compra, (2) INSERT en Detalle_Compra por cada ítem,
     * (3) UPDATE stock sumando la cantidad comprada y recalculando COSTO_PROMEDIO,
     * (4) INSERT en Movimientos_Financieros con tipo_movimiento_id=2 (Compra/Egreso).
     * Si cualquier paso falla se hace rollback de toda la operación.
     * @param compra Objeto Compra con los datos del encabezado
     * @param detalles Lista de DetalleCompra con cada ítem de la compra
     * @return La Compra con su ID asignado, o null si la transacción falló
     */
    public static Compra createConDetalles(Compra compra, List<DetalleCompra> detalles) {
        // Declarar la conexión fuera del try para poder acceder en catch y finally
        Connection conexion = null;
        try {
            // Obtener conexión a la base de datos
            conexion = dbConnection.getConnection();
            // Desactivar auto-commit para manejar la transacción manualmente
            conexion.setAutoCommit(false);

            // ===== PASO 1: INSERT en la tabla Compra =====
            // SQL para insertar el encabezado de la compra
            String sqlCompra = "INSERT INTO Compras (FECHA_COMPRA, TOTAL_COMPRA, METODO_PAGO_COMPRA, USUARIO_ID, PROVEEDOR_ID) VALUES (?, ?, ?, ?, ?)";
            // Preparar la consulta solicitando la clave generada por la BD
            PreparedStatement stmtCompra = conexion.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            // Asignar la fecha de la compra en formato "YYYY-MM-DD"
            stmtCompra.setString(1, compra.getFechaCompra());
            // Asignar el total calculado de la compra
            stmtCompra.setDouble(2, compra.getTotalCompra());
            // Asignar el método de pago de la compra ("Transferencia" o "Efectivo")
            stmtCompra.setString(3, compra.getMetodoPago());
            // Asignar el ID del usuario que registra la compra
            stmtCompra.setInt(4, compra.getUsuarioId());
            // Asignar el ID del proveedor de la compra
            stmtCompra.setInt(5, compra.getProveedorId());
            // Ejecutar INSERT de la compra
            stmtCompra.executeUpdate();
            // Obtener la clave primaria generada por la BD para la compra
            ResultSet clavesCompra = stmtCompra.getGeneratedKeys();
            // Leer el ID generado y asignarlo al objeto compra
            if (clavesCompra.next()) compra.setIdCompra(clavesCompra.getInt(1));
            // Cerrar el statement de la compra para liberar recursos
            stmtCompra.close();

            // ===== PASOS 2 y 3: INSERT Detalle_Compra y UPDATE stock por cada ítem =====
            // SQL para insertar cada ítem del detalle de compra
            String sqlDetalle = "INSERT INTO Detalles_Compras (CANTIDAD_COMPRA, COSTO_UNITARIO, SUBTOTAL_COMPRA, COMPRA_ID, PRODUCTO_ID) VALUES (?, ?, ?, ?, ?)";
            // SQL para sumar la cantidad comprada al stock y aplicar el costo promedio ya calculado
            // El costo promedio ponderado NO se calcula aquí — viene pre-calculado desde CompraService
            String sqlStock = "UPDATE Productos SET STOCK = STOCK + ?, COSTO_PROMEDIO = ? WHERE ID_PRODUCTO = ?";
            // Recorrer cada ítem para insertarlo y actualizar el stock
            for (DetalleCompra detalle : detalles) {
                // Asignar el ID de la compra recién creada al detalle
                detalle.setCompraId(compra.getIdCompra());

                // Preparar la consulta de inserción del ítem
                PreparedStatement stmtDetalle = conexion.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
                // Asignar la cantidad de unidades compradas
                stmtDetalle.setInt(1, detalle.getCantidad());
                // Asignar el costo unitario del producto al momento de la compra
                stmtDetalle.setDouble(2, detalle.getCostoUnitario());
                // Asignar el subtotal calculado del ítem (costo × cantidad)
                stmtDetalle.setDouble(3, detalle.getSubtotal());
                // Asignar el ID de la compra a la que pertenece el ítem
                stmtDetalle.setInt(4, detalle.getCompraId());
                // Asignar el ID del producto comprado
                stmtDetalle.setInt(5, detalle.getProductoId());
                // Ejecutar INSERT del ítem
                stmtDetalle.executeUpdate();
                // Obtener la clave primaria generada para el detalle
                ResultSet clavesDetalle = stmtDetalle.getGeneratedKeys();
                // Leer el ID generado y asignarlo al objeto detalle
                if (clavesDetalle.next()) detalle.setIdDetCompra(clavesDetalle.getInt(1));
                // Cerrar el statement del detalle para liberar recursos
                stmtDetalle.close();

                // Preparar la consulta para sumar stock y aplicar el costo promedio pre-calculado
                PreparedStatement stmtStock = conexion.prepareStatement(sqlStock);
                // Asignar la cantidad a sumar al stock
                stmtStock.setInt(1, detalle.getCantidad());
                // Usar el costo promedio ponderado calculado en CompraService (no se recalcula aquí)
                stmtStock.setDouble(2, detalle.getCostoPromedioNuevo());
                // Asignar el ID del producto cuyo stock se actualiza
                stmtStock.setInt(3, detalle.getProductoId());
                // Ejecutar UPDATE del stock y costo promedio del producto
                stmtStock.executeUpdate();
                // Cerrar el statement del stock para liberar recursos
                stmtStock.close();
            }

            // ===== PASO 4: Registrar movimiento financiero en la misma transacción =====
            // Delegar al MovimientoFinancieroDAO pasando la conexión activa para mantener la atomicidad
            // tipo_movimiento_id = 2 → Compra (Egreso); compraId = ID de la compra creada, ventaId y gastoId = null
            MovimientoFinancieroDAO.insertEnTransaccion(
                    conexion,
                    "Compra #" + compra.getIdCompra(),
                    compra.getTotalCompra(),
                    compra.getFechaCompra(),
                    2,
                    null, compra.getIdCompra(), null);

            // Confirmar todos los cambios de la transacción en la BD
            conexion.commit();
            // Retornar la compra creada con su ID asignado
            return compra;

        } catch (Exception excepcion) {
            // Registrar el error que causó el fallo de la transacción
            System.out.println("Error CompraDAO.createConDetalles: " + excepcion.getMessage());
            // Intentar revertir todos los cambios de la transacción fallida
            if (conexion != null) {
                try {
                    // Hacer rollback para dejar la BD en su estado anterior a la transacción
                    conexion.rollback();
                } catch (Exception errorRollback) {
                    // Registrar error del rollback en consola
                    System.out.println("Error en rollback CompraDAO: " + errorRollback.getMessage());
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
                    System.out.println("Error al cerrar conexion CompraDAO: " + errorCierre.getMessage());
                }
            }
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Compra.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Compra con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Compra mapRow(ResultSet resultado) throws SQLException {
        // Construir la Compra con los datos del registro actual
        Compra compra = new Compra(
                // Leer el ID de la compra desde la columna ID_COMPRA
                resultado.getInt("ID_COMPRA"),
                // Leer la fecha de la compra como String desde la columna FECHA_COMPRA
                resultado.getString("FECHA_COMPRA"),
                // Leer el total de la compra desde la columna TOTAL_COMPRA
                resultado.getDouble("TOTAL_COMPRA"),
                // Leer el método de pago desde la columna METODO_PAGO_COMPRA
                resultado.getString("METODO_PAGO_COMPRA"),
                // Leer el ID del usuario desde la columna USUARIO_ID
                resultado.getInt("USUARIO_ID"),
                // Leer el ID del proveedor desde la columna PROVEEDOR_ID
                resultado.getInt("PROVEEDOR_ID"));
        // Poblar el nombre del proveedor si la columna existe en el ResultSet (viene del JOIN)
        try {
            // Leer el nombre del proveedor desde la columna NOMBRE_PROVEEDOR del JOIN
            compra.setNombreProveedor(resultado.getString("NOMBRE_PROVEEDOR"));
        } catch (SQLException excepcion) {
            // Si la columna no existe (ej: findById sin JOIN), ignorar silenciosamente
        }
        // Retornar la compra con todos los campos poblados
        return compra;
    }

    /**
     * Convierte una fila del ResultSet en un objeto DetalleCompra.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto DetalleCompra con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static DetalleCompra mapDetalleRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un DetalleCompra con los datos del registro actual
        return new DetalleCompra(
                // Leer el ID del ítem desde la columna ID_DET_COMPRA
                resultado.getInt("ID_DET_COMPRA"),
                // Leer la cantidad comprada desde la columna CANTIDAD_COMPRA
                resultado.getInt("CANTIDAD_COMPRA"),
                // Leer el costo unitario desde la columna COSTO_UNITARIO
                resultado.getDouble("COSTO_UNITARIO"),
                // Leer el subtotal del ítem desde la columna SUBTOTAL_COMPRA
                resultado.getDouble("SUBTOTAL_COMPRA"),
                // Leer el ID de la compra desde la columna COMPRA_ID
                resultado.getInt("COMPRA_ID"),
                // Leer el ID del producto desde la columna PRODUCTO_ID
                resultado.getInt("PRODUCTO_ID"));
    }
}
