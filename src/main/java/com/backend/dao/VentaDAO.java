// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un ítem de una venta
import com.backend.models.DetalleVenta;
// Modelo que representa una venta del negocio
import com.backend.models.Venta;

// Clases JDBC para conexión, consultas preparadas, resultados, sentencias y tipos SQL
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones de Ventas y Detalle_Venta
 * en la base de datos. Implementa una transacción atómica al crear una venta:
 * registra el encabezado, sus ítems, descuenta el stock de cada producto
 * y registra el movimiento financiero correspondiente.
 * Centraliza todo el acceso a las tablas Venta y Detalle_Venta.
 */
public class VentaDAO {

    /**
     * Busca una venta por su ID.
     * @param id ID de la venta a buscar
     * @return Venta encontrada o null si no existe
     */
    public static Venta findById(int id) {
        // SQL para seleccionar una venta por su clave primaria
        String sql = "SELECT * FROM venta WHERE id_venta = ?";
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
            System.out.println("Error VentaDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró la venta
        return null;
    }

    /**
     * Obtiene todas las ventas de la base de datos ordenadas por fecha descendente.
     * @return Lista de ventas (vacía si no hay ninguna)
     */
    public static List<Venta> findAll() {
        // Lista donde se acumularán las ventas encontradas
        List<Venta> lista = new ArrayList<>();
        // SQL para seleccionar todas las ventas ordenadas por fecha y luego por ID descendente
        String sql = "SELECT * FROM venta ORDER BY fecha_venta DESC, id_venta DESC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada venta a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error VentaDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todas las ventas encontradas
        return lista;
    }

    /**
     * Obtiene todos los ítems (detalles) que pertenecen a una venta específica.
     * @param ventaId ID de la venta cuyos detalles se desean obtener
     * @return Lista de DetalleVenta (vacía si no hay ninguno)
     */
    public static List<DetalleVenta> findDetallesByVentaId(int ventaId) {
        // Lista donde se acumularán los detalles encontrados
        List<DetalleVenta> lista = new ArrayList<>();
        // SQL para seleccionar todos los ítems de una venta específica
        String sql = "SELECT * FROM detalle_venta WHERE venta_id = ? ORDER BY id_det_venta ASC";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID de la venta como parámetro de búsqueda
            consulta.setInt(1, ventaId);
            // Ejecutar consulta y obtener resultados
            ResultSet resultado = consulta.executeQuery();
            // Recorrer todos los registros y agregar cada detalle a la lista
            while (resultado.next()) lista.add(mapDetalleRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error VentaDAO.findDetallesByVentaId: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los detalles de la venta
        return lista;
    }

    /**
     * Crea una venta completa de forma atómica en una sola transacción de base de datos.
     * Pasos: (1) INSERT en Venta, (2) INSERT en Detalle_Venta por cada ítem,
     * (3) UPDATE stock descontando la cantidad vendida por producto,
     * (4) INSERT en Movimientos_Financieros con tipo_movimiento_id=1 (Venta/Ingreso).
     * Si cualquier paso falla se hace rollback de toda la operación.
     * @param venta Objeto Venta con los datos del encabezado
     * @param detalles Lista de DetalleVenta con cada ítem de la venta
     * @return La Venta con su ID asignado, o null si la transacción falló
     */
    public static Venta createConDetalles(Venta venta, List<DetalleVenta> detalles) {
        // Declarar la conexión fuera del try para poder acceder en catch y finally
        Connection conexion = null;
        try {
            // Obtener conexión a la base de datos
            conexion = dbConnection.getConnection();
            // Desactivar auto-commit para manejar la transacción manualmente
            conexion.setAutoCommit(false);

            // ===== PASO 1: INSERT en la tabla Venta =====
            // SQL para insertar el encabezado de la venta
            String sqlVenta = "INSERT INTO venta (fecha_venta, total_venta, metodo_pago, usuario_id, cliente_id) VALUES (?, ?, ?, ?, ?)";
            // Preparar la consulta solicitando la clave generada por la BD
            PreparedStatement stmtVenta = conexion.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            // Asignar la fecha de la venta en formato "YYYY-MM-DD"
            stmtVenta.setString(1, venta.getFechaVenta());
            // Asignar el total calculado de la venta
            stmtVenta.setDouble(2, venta.getTotalVenta());
            // Asignar el método de pago ("Transferencia" o "Efectivo")
            stmtVenta.setString(3, venta.getMetodoPago());
            // Asignar el ID del usuario que registra la venta
            stmtVenta.setInt(4, venta.getUsuarioId());
            // Asignar el ID del cliente de la venta
            stmtVenta.setInt(5, venta.getClienteId());
            // Ejecutar INSERT de la venta
            stmtVenta.executeUpdate();
            // Obtener la clave primaria generada por la BD para la venta
            ResultSet clavesVenta = stmtVenta.getGeneratedKeys();
            // Leer el ID generado y asignarlo al objeto venta
            if (clavesVenta.next()) venta.setIdVenta(clavesVenta.getInt(1));
            // Cerrar el statement de la venta para liberar recursos
            stmtVenta.close();

            // ===== PASOS 2 y 3: INSERT Detalle_Venta y UPDATE stock por cada ítem =====
            // SQL para insertar cada ítem del detalle de venta
            String sqlDetalle = "INSERT INTO detalle_venta (cantidad, precio_unitario, subtotal, venta_id, producto_id) VALUES (?, ?, ?, ?, ?)";
            // SQL para descontar la cantidad vendida del stock del producto
            String sqlStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
            // Recorrer cada ítem para insertarlo y descontar su stock
            for (DetalleVenta detalle : detalles) {
                // Asignar el ID de la venta recién creada al detalle
                detalle.setVentaId(venta.getIdVenta());

                // Preparar la consulta de inserción del ítem
                PreparedStatement stmtDetalle = conexion.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
                // Asignar la cantidad de unidades vendidas
                stmtDetalle.setInt(1, detalle.getCantidad());
                // Asignar el precio unitario del producto al momento de la venta
                stmtDetalle.setDouble(2, detalle.getPrecioUnitario());
                // Asignar el subtotal calculado del ítem (precio × cantidad)
                stmtDetalle.setDouble(3, detalle.getSubtotal());
                // Asignar el ID de la venta a la que pertenece el ítem
                stmtDetalle.setInt(4, detalle.getVentaId());
                // Asignar el ID del producto vendido
                stmtDetalle.setInt(5, detalle.getProductoId());
                // Ejecutar INSERT del ítem
                stmtDetalle.executeUpdate();
                // Obtener la clave primaria generada para el detalle
                ResultSet clavesDetalle = stmtDetalle.getGeneratedKeys();
                // Leer el ID generado y asignarlo al objeto detalle
                if (clavesDetalle.next()) detalle.setIdDetVenta(clavesDetalle.getInt(1));
                // Cerrar el statement del detalle para liberar recursos
                stmtDetalle.close();

                // Preparar la consulta para descontar el stock del producto
                PreparedStatement stmtStock = conexion.prepareStatement(sqlStock);
                // Asignar la cantidad a descontar del stock
                stmtStock.setInt(1, detalle.getCantidad());
                // Asignar el ID del producto cuyo stock se actualiza
                stmtStock.setInt(2, detalle.getProductoId());
                // Ejecutar UPDATE del stock del producto
                stmtStock.executeUpdate();
                // Cerrar el statement del stock para liberar recursos
                stmtStock.close();
            }

            // ===== PASO 4: INSERT en Movimientos_Financieros =====
            // SQL para registrar el movimiento financiero de tipo Venta (Ingreso)
            String sqlMovimiento = "INSERT INTO movimientos_financieros (fecha_movimiento, concepto, monto, metodo_pago, tipo_movimiento_id, usuario_id, venta_id, compra_id, gasto_adicional_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Preparar la consulta del movimiento financiero
            PreparedStatement stmtMovimiento = conexion.prepareStatement(sqlMovimiento);
            // Asignar la fecha del movimiento (misma que la venta)
            stmtMovimiento.setString(1, venta.getFechaVenta());
            // Asignar el concepto del movimiento identificando el número de venta
            stmtMovimiento.setString(2, "Venta #" + venta.getIdVenta());
            // Asignar el monto del movimiento (total de la venta)
            stmtMovimiento.setDouble(3, venta.getTotalVenta());
            // Asignar el método de pago del movimiento
            stmtMovimiento.setString(4, venta.getMetodoPago());
            // Asignar el tipo de movimiento: 1 = Venta (Ingreso)
            stmtMovimiento.setInt(5, 1);
            // Asignar el ID del usuario que generó el movimiento
            stmtMovimiento.setInt(6, venta.getUsuarioId());
            // Asignar el ID de la venta asociada al movimiento
            stmtMovimiento.setInt(7, venta.getIdVenta());
            // Asignar NULL al campo compra_id (no aplica para ventas)
            stmtMovimiento.setNull(8, Types.INTEGER);
            // Asignar NULL al campo gasto_adicional_id (no aplica para ventas)
            stmtMovimiento.setNull(9, Types.INTEGER);
            // Ejecutar INSERT del movimiento financiero
            stmtMovimiento.executeUpdate();
            // Cerrar el statement del movimiento para liberar recursos
            stmtMovimiento.close();

            // Confirmar todos los cambios de la transacción en la BD
            conexion.commit();
            // Retornar la venta creada con su ID asignado
            return venta;

        } catch (Exception excepcion) {
            // Registrar el error que causó el fallo de la transacción
            System.out.println("Error VentaDAO.createConDetalles: " + excepcion.getMessage());
            // Intentar revertir todos los cambios de la transacción fallida
            if (conexion != null) {
                try {
                    // Hacer rollback para dejar la BD en su estado anterior a la transacción
                    conexion.rollback();
                } catch (Exception errorRollback) {
                    // Registrar error del rollback en consola
                    System.out.println("Error en rollback VentaDAO: " + errorRollback.getMessage());
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
                    System.out.println("Error al cerrar conexion VentaDAO: " + errorCierre.getMessage());
                }
            }
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Venta.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Venta con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Venta mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar una Venta con los datos del registro actual
        return new Venta(
                // Leer el ID de la venta desde la columna id_venta
                resultado.getInt("id_venta"),
                // Leer la fecha de la venta como String desde la columna fecha_venta
                resultado.getString("fecha_venta"),
                // Leer el total de la venta desde la columna total_venta
                resultado.getDouble("total_venta"),
                // Leer el método de pago desde la columna metodo_pago
                resultado.getString("metodo_pago"),
                // Leer el ID del usuario desde la columna usuario_id
                resultado.getInt("usuario_id"),
                // Leer el ID del cliente desde la columna cliente_id
                resultado.getInt("cliente_id"));
    }

    /**
     * Convierte una fila del ResultSet en un objeto DetalleVenta.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto DetalleVenta con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static DetalleVenta mapDetalleRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un DetalleVenta con los datos del registro actual
        return new DetalleVenta(
                // Leer el ID del ítem desde la columna id_det_venta
                resultado.getInt("id_det_venta"),
                // Leer la cantidad vendida desde la columna cantidad
                resultado.getInt("cantidad"),
                // Leer el precio unitario desde la columna precio_unitario
                resultado.getDouble("precio_unitario"),
                // Leer el subtotal del ítem desde la columna subtotal
                resultado.getDouble("subtotal"),
                // Leer el ID de la venta desde la columna venta_id
                resultado.getInt("venta_id"),
                // Leer el ID del producto desde la columna producto_id
                resultado.getInt("producto_id"));
    }
}
