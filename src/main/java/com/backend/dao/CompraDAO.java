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
        String sql = "SELECT * FROM compra WHERE id_compra = ?";
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
        // SQL para seleccionar todas las compras ordenadas por fecha y luego por ID descendente
        String sql = "SELECT * FROM compra ORDER BY fecha_compra DESC, id_compra DESC";
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
        String sql = "SELECT * FROM detalle_compra WHERE compra_id = ? ORDER BY id_det_compra ASC";
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
     * (3) UPDATE stock sumando la cantidad comprada y recalculando costo_promedio,
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
            String sqlCompra = "INSERT INTO compra (fecha_compra, total_compra, metodo_pago, usuario_id, proveedor_id) VALUES (?, ?, ?, ?, ?)";
            // Preparar la consulta solicitando la clave generada por la BD
            PreparedStatement stmtCompra = conexion.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            // Asignar la fecha de la compra en formato "YYYY-MM-DD"
            stmtCompra.setString(1, compra.getFechaCompra());
            // Asignar el total calculado de la compra
            stmtCompra.setDouble(2, compra.getTotalCompra());
            // Asignar el método de pago ("Transferencia" o "Efectivo")
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
            String sqlDetalle = "INSERT INTO detalle_compra (cantidad, costo_unitario, subtotal, compra_id, producto_id) VALUES (?, ?, ?, ?, ?)";
            // SQL para sumar la cantidad comprada al stock y recalcular el costo promedio
            String sqlStock = "UPDATE producto SET stock = stock + ?, costo_promedio = ? WHERE id_producto = ?";
            // SQL para obtener el stock y costo promedio actual del producto
            String sqlProducto = "SELECT stock, costo_promedio FROM producto WHERE id_producto = ?";
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

                // Consultar el stock y costo promedio actual del producto para recalcular
                PreparedStatement stmtProducto = conexion.prepareStatement(sqlProducto);
                // Asignar el ID del producto a consultar
                stmtProducto.setInt(1, detalle.getProductoId());
                // Ejecutar consulta del producto
                ResultSet rsProducto = stmtProducto.executeQuery();
                // Leer los valores actuales del producto
                rsProducto.next();
                // Obtener el stock actual antes de sumar la compra
                int stockActual = rsProducto.getInt("stock");
                // Obtener el costo promedio actual del producto
                double costoPromedioActual = rsProducto.getDouble("costo_promedio");
                // Cerrar el statement del producto para liberar recursos
                stmtProducto.close();

                // Calcular el nuevo costo promedio ponderado: (stockActual * costoActual + cantidadComprada * costoCompra) / stockTotal
                int stockTotal = stockActual + detalle.getCantidad();
                // Calcular el costo promedio ponderado con el nuevo lote
                double nuevoCostoPromedio = ((stockActual * costoPromedioActual) + (detalle.getCantidad() * detalle.getCostoUnitario())) / stockTotal;

                // Preparar la consulta para actualizar stock y costo promedio del producto
                PreparedStatement stmtStock = conexion.prepareStatement(sqlStock);
                // Asignar la cantidad a sumar al stock
                stmtStock.setInt(1, detalle.getCantidad());
                // Asignar el nuevo costo promedio calculado
                stmtStock.setDouble(2, nuevoCostoPromedio);
                // Asignar el ID del producto cuyo stock se actualiza
                stmtStock.setInt(3, detalle.getProductoId());
                // Ejecutar UPDATE del stock y costo promedio del producto
                stmtStock.executeUpdate();
                // Cerrar el statement del stock para liberar recursos
                stmtStock.close();
            }

            // ===== PASO 4: INSERT en Movimientos_Financieros =====
            // SQL para registrar el movimiento financiero de tipo Compra (Egreso)
            String sqlMovimiento = "INSERT INTO movimientos_financieros (fecha_movimiento, concepto, monto, metodo_pago, tipo_movimiento_id, usuario_id, venta_id, compra_id, gasto_adicional_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Preparar la consulta del movimiento financiero
            PreparedStatement stmtMovimiento = conexion.prepareStatement(sqlMovimiento);
            // Asignar la fecha del movimiento (misma que la compra)
            stmtMovimiento.setString(1, compra.getFechaCompra());
            // Asignar el concepto del movimiento identificando el número de compra
            stmtMovimiento.setString(2, "Compra #" + compra.getIdCompra());
            // Asignar el monto del movimiento (total de la compra)
            stmtMovimiento.setDouble(3, compra.getTotalCompra());
            // Asignar el método de pago del movimiento
            stmtMovimiento.setString(4, compra.getMetodoPago());
            // Asignar el tipo de movimiento: 2 = Compra (Egreso)
            stmtMovimiento.setInt(5, 2);
            // Asignar el ID del usuario que generó el movimiento
            stmtMovimiento.setInt(6, compra.getUsuarioId());
            // Asignar NULL al campo venta_id (no aplica para compras)
            stmtMovimiento.setNull(7, Types.INTEGER);
            // Asignar el ID de la compra asociada al movimiento
            stmtMovimiento.setInt(8, compra.getIdCompra());
            // Asignar NULL al campo gasto_adicional_id (no aplica para compras)
            stmtMovimiento.setNull(9, Types.INTEGER);
            // Ejecutar INSERT del movimiento financiero
            stmtMovimiento.executeUpdate();
            // Cerrar el statement del movimiento para liberar recursos
            stmtMovimiento.close();

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
        // Construir y retornar una Compra con los datos del registro actual
        return new Compra(
                // Leer el ID de la compra desde la columna id_compra
                resultado.getInt("id_compra"),
                // Leer la fecha de la compra como String desde la columna fecha_compra
                resultado.getString("fecha_compra"),
                // Leer el total de la compra desde la columna total_compra
                resultado.getDouble("total_compra"),
                // Leer el método de pago desde la columna metodo_pago
                resultado.getString("metodo_pago"),
                // Leer el ID del usuario desde la columna usuario_id
                resultado.getInt("usuario_id"),
                // Leer el ID del proveedor desde la columna proveedor_id
                resultado.getInt("proveedor_id"));
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
                // Leer el ID del ítem desde la columna id_det_compra
                resultado.getInt("id_det_compra"),
                // Leer la cantidad comprada desde la columna cantidad
                resultado.getInt("cantidad"),
                // Leer el costo unitario desde la columna costo_unitario
                resultado.getDouble("costo_unitario"),
                // Leer el subtotal del ítem desde la columna subtotal
                resultado.getDouble("subtotal"),
                // Leer el ID de la compra desde la columna compra_id
                resultado.getInt("compra_id"),
                // Leer el ID del producto desde la columna producto_id
                resultado.getInt("producto_id"));
    }
}
