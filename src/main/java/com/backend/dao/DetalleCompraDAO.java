// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un ítem de una compra
import com.backend.models.DetalleCompra;

// Clases JDBC para conexión, consultas preparadas y resultados
import java.sql.*;
// Lista dinámica para retornar múltiples registros
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para consultas de solo lectura sobre Detalle_Compra.
 * La creación de detalles se maneja dentro de la transacción atómica de CompraDAO.
 * Este DAO se utiliza para consultar los ítems de una compra ya registrada.
 */
public class DetalleCompraDAO {

    /**
     * Busca un detalle de compra por su ID.
     * @param id ID del detalle a buscar
     * @return DetalleCompra encontrado o null si no existe
     */
    public static DetalleCompra findById(int id) {
        // SQL para seleccionar un detalle por su clave primaria
        String sql = "SELECT * FROM Detalles_Compras WHERE ID_DET_COMPRA = ?";
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
            System.out.println("Error DetalleCompraDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el detalle
        return null;
    }

    /**
     * Obtiene todos los ítems que pertenecen a una compra específica.
     * @param compraId ID de la compra cuyos detalles se desean obtener
     * @return Lista de DetalleCompra (vacía si no hay ninguno)
     */
    public static List<DetalleCompra> findByCompraId(int compraId) {
        // Lista donde se acumularán los detalles encontrados
        List<DetalleCompra> lista = new ArrayList<>();
        // SQL para seleccionar los detalles de una compra ordenados por ID
        String sql = "SELECT * FROM Detalles_Compras WHERE COMPRA_ID = ? ORDER BY ID_DET_COMPRA ASC";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID de la compra como parámetro de búsqueda
            consulta.setInt(1, compraId);
            // Ejecutar consulta y obtener resultados
            ResultSet resultado = consulta.executeQuery();
            // Recorrer todos los registros y agregar cada detalle a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error DetalleCompraDAO.findByCompraId: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los detalles de la compra
        return lista;
    }

    /**
     * Convierte una fila del ResultSet en un objeto DetalleCompra.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto DetalleCompra con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static DetalleCompra mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un DetalleCompra con los datos del registro actual
        return new DetalleCompra(
                // Leer el ID del detalle desde la columna ID_DET_COMPRA
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
