// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un ítem de una venta
import com.backend.models.DetalleVenta;

// Clases JDBC para conexión, consultas preparadas y resultados
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para consultas de solo lectura sobre Detalle_Venta.
 * La creación de detalles se maneja dentro de la transacción atómica de VentaDAO.
 * Este DAO se utiliza para consultar los ítems de una venta ya registrada.
 */
public class DetalleVentaDAO {

    /**
     * Busca un detalle de venta por su ID.
     * @param id ID del detalle a buscar
     * @return DetalleVenta encontrado o null si no existe
     */
    public static DetalleVenta findById(int id) {
        // SQL para seleccionar un detalle por su clave primaria
        String sql = "SELECT * FROM Detalles_Ventas WHERE ID_DET_VENTA = ?";
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
            System.out.println("Error DetalleVentaDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el detalle
        return null;
    }

    /**
     * Obtiene todos los ítems que pertenecen a una venta específica.
     * @param ventaId ID de la venta cuyos detalles se desean obtener
     * @return Lista de DetalleVenta (vacía si no hay ninguno)
     */
    public static List<DetalleVenta> findByVentaId(int ventaId) {
        // Lista donde se acumularán los detalles encontrados
        List<DetalleVenta> lista = new ArrayList<>();
        // SQL para seleccionar los detalles de una venta ordenados por ID
        String sql = "SELECT * FROM Detalles_Ventas WHERE VENTA_ID = ? ORDER BY ID_DET_VENTA ASC";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID de la venta como parámetro de búsqueda
            consulta.setInt(1, ventaId);
            // Ejecutar consulta y obtener resultados
            ResultSet resultado = consulta.executeQuery();
            // Recorrer todos los registros y agregar cada detalle a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error DetalleVentaDAO.findByVentaId: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los detalles de la venta
        return lista;
    }

    /**
     * Convierte una fila del ResultSet en un objeto DetalleVenta.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto DetalleVenta con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static DetalleVenta mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un DetalleVenta con los datos del registro actual
        return new DetalleVenta(
                // Leer el ID del detalle desde la columna ID_DET_VENTA
                resultado.getInt("ID_DET_VENTA"),
                // Leer la cantidad vendida desde la columna CANTIDAD_VENTA
                resultado.getInt("CANTIDAD_VENTA"),
                // Leer el precio unitario desde la columna PRECIO_UNITARIO
                resultado.getDouble("PRECIO_UNITARIO"),
                // Leer el subtotal del ítem desde la columna SUBTOTAL_VENTA
                resultado.getDouble("SUBTOTAL_VENTA"),
                // Leer el ID de la venta desde la columna VENTA_ID
                resultado.getInt("VENTA_ID"),
                // Leer el ID del producto desde la columna PRODUCTO_ID
                resultado.getInt("PRODUCTO_ID"));
    }
}
