// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Para construir arrays JSON con múltiples elementos (listas de resultados)
import com.google.gson.JsonArray;
// Para construir objetos JSON individuales (un registro o un resumen)
import com.google.gson.JsonObject;

// Clases JDBC necesarias: Connection, PreparedStatement y ResultSet
import java.sql.*;

/**
 * DAO de solo lectura para el módulo Dashboard.
 * Centraliza todas las consultas de agregación necesarias para el home.
 * Responsabilidad única: ejecutar queries y retornar datos crudos de la BD.
 * Los cálculos de negocio (ganancias, márgenes derivados) se realizan en DashboardService.
 * No modifica datos — únicamente realiza SELECT con SUM, COUNT y GROUP BY.
 */
public class DashboardDAO {

    /**
     * Retorna los datos crudos de movimientos y contadores para las tarjetas del dashboard.
     * Ejecuta 6 queries independientes sobre la BD y retorna los resultados sin procesar:
     * - ingresosHoy: suma de ventas del día actual
     * - egresosHoy: suma de compras y gastos del día actual
     * - ingresosMes: suma de ventas del mes actual
     * - egresosMes: suma de compras y gastos del mes actual
     * - productosActivos: conteo de productos con estado activo
     * - clientesActivos: conteo de clientes con estado activo
     * El cálculo de ganancia neta se delega a DashboardService.
     *
     * @return JsonObject con los 6 campos numéricos crudos, todos en 0 si hay error
     */
    public static JsonObject getResumen() {
        // Objeto que acumulará los datos crudos de las 6 consultas para retornar al servicio
        JsonObject resumen = new JsonObject();
        // Inicializar ingresosHoy en 0 por si la consulta no retorna filas
        resumen.addProperty("ingresosHoy", 0.0);
        // Inicializar egresosHoy en 0 por si la consulta no retorna filas
        resumen.addProperty("egresosHoy", 0.0);
        // Inicializar ingresosMes en 0 por si la consulta no retorna filas
        resumen.addProperty("ingresosMes", 0.0);
        // Inicializar egresosMes en 0 por si la consulta no retorna filas
        resumen.addProperty("egresosMes", 0.0);
        // Inicializar productosActivos en 0 por si la consulta no retorna filas
        resumen.addProperty("productosActivos", 0);
        // Inicializar clientesActivos en 0 por si la consulta no retorna filas
        resumen.addProperty("clientesActivos", 0);

        // SQL: suma de montos de tipo 1 (ventas/ingresos) con fecha igual a hoy
        // COALESCE garantiza que si no hay ventas hoy el resultado sea 0 en vez de NULL
        String sqlIngresosHoy = "SELECT COALESCE(SUM(monto), 0) AS total " +
                "FROM movimientos_financieros " +
                "WHERE tipo_movimiento_id = 1 AND DATE(fecha_movimiento) = CURDATE()";

        // SQL: suma de montos de tipo 2 (compras) y tipo 3 (gastos) con fecha igual a hoy
        String sqlEgresosHoy = "SELECT COALESCE(SUM(monto), 0) AS total " +
                "FROM movimientos_financieros " +
                "WHERE tipo_movimiento_id IN (2, 3) AND DATE(fecha_movimiento) = CURDATE()";

        // SQL: suma de ingresos (tipo 1) del mes y año actuales
        String sqlIngresosMes = "SELECT COALESCE(SUM(monto), 0) AS total " +
                "FROM movimientos_financieros " +
                "WHERE tipo_movimiento_id = 1 " +
                "AND MONTH(fecha_movimiento) = MONTH(CURDATE()) " +
                "AND YEAR(fecha_movimiento) = YEAR(CURDATE())";

        // SQL: suma de egresos (tipos 2 y 3) del mes y año actuales
        String sqlEgresosMes = "SELECT COALESCE(SUM(monto), 0) AS total " +
                "FROM movimientos_financieros " +
                "WHERE tipo_movimiento_id IN (2, 3) " +
                "AND MONTH(fecha_movimiento) = MONTH(CURDATE()) " +
                "AND YEAR(fecha_movimiento) = YEAR(CURDATE())";

        // SQL: conteo de productos con estado activo (estado = true)
        String sqlProductos = "SELECT COUNT(*) AS total FROM Producto WHERE ESTADO = true";

        // SQL: conteo de clientes con estado activo (estado = true)
        String sqlClientes = "SELECT COUNT(*) AS total FROM Clientes WHERE ESTADO = true";

        // Abrir una sola conexión y reutilizarla para todas las consultas del resumen
        try (Connection conexion = dbConnection.getConnection()) {

            // Ejecutar la consulta de ingresos del día con su propio PreparedStatement
            try (PreparedStatement stmt = conexion.prepareStatement(sqlIngresosHoy);
                 ResultSet rs = stmt.executeQuery()) {
                // Si hay resultado, actualizar el campo ingresosHoy con el valor de la BD
                if (rs.next()) resumen.addProperty("ingresosHoy", rs.getDouble("total"));
            }

            // Ejecutar la consulta de egresos del día con su propio PreparedStatement
            try (PreparedStatement stmt = conexion.prepareStatement(sqlEgresosHoy);
                 ResultSet rs = stmt.executeQuery()) {
                // Si hay resultado, actualizar el campo egresosHoy con el valor de la BD
                if (rs.next()) resumen.addProperty("egresosHoy", rs.getDouble("total"));
            }

            // Ejecutar la consulta de ingresos del mes con su propio PreparedStatement
            try (PreparedStatement stmt = conexion.prepareStatement(sqlIngresosMes);
                 ResultSet rs = stmt.executeQuery()) {
                // Si hay resultado, actualizar el campo ingresosMes con el valor de la BD
                if (rs.next()) resumen.addProperty("ingresosMes", rs.getDouble("total"));
            }

            // Ejecutar la consulta de egresos del mes con su propio PreparedStatement
            try (PreparedStatement stmt = conexion.prepareStatement(sqlEgresosMes);
                 ResultSet rs = stmt.executeQuery()) {
                // Si hay resultado, actualizar el campo egresosMes con el valor de la BD
                if (rs.next()) resumen.addProperty("egresosMes", rs.getDouble("total"));
            }

            // Ejecutar la consulta de conteo de productos activos
            try (PreparedStatement stmt = conexion.prepareStatement(sqlProductos);
                 ResultSet rs = stmt.executeQuery()) {
                // Si hay resultado, actualizar el campo productosActivos con el conteo de la BD
                if (rs.next()) resumen.addProperty("productosActivos", rs.getInt("total"));
            }

            // Ejecutar la consulta de conteo de clientes activos
            try (PreparedStatement stmt = conexion.prepareStatement(sqlClientes);
                 ResultSet rs = stmt.executeQuery()) {
                // Si hay resultado, actualizar el campo clientesActivos con el conteo de la BD
                if (rs.next()) resumen.addProperty("clientesActivos", rs.getInt("total"));
            }

        } catch (Exception excepcion) {
            // Registrar el error en consola; los campos ya tienen valores por defecto en 0
            System.out.println("Error DashboardDAO.getResumen: " + excepcion.getMessage());
        }

        // Retornar el objeto con los datos crudos de la BD al servicio para su procesamiento
        return resumen;
    }

    /**
     * Retorna el total de ventas (ingresos) por cada uno de los últimos 7 días.
     * Solo incluye los días que tienen al menos una venta registrada.
     * Usado para el gráfico de barras "Ventas por Semana" del dashboard.
     *
     * @return JsonArray con objetos { fecha: "YYYY-MM-DD", total: 0.0 } por cada día con ventas
     */
    public static JsonArray getVentasUltimos7Dias() {
        // Lista donde se acumularán los totales por día retornados por la BD
        JsonArray resultado = new JsonArray();

        // SQL: suma de ingresos (tipo 1) agrupada por fecha para los últimos 7 días
        // DATE_SUB(CURDATE(), INTERVAL 6 DAY) calcula la fecha de hace 6 días (hoy = día 7)
        // GROUP BY agrupa los movimientos del mismo día en un solo registro
        // ORDER BY fecha ASC para mostrar los días en orden cronológico en el gráfico
        String sql = "SELECT DATE(fecha_movimiento) AS fecha, COALESCE(SUM(monto), 0) AS total " +
                "FROM movimientos_financieros " +
                "WHERE tipo_movimiento_id = 1 " +
                "AND DATE(fecha_movimiento) >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(fecha_movimiento) " +
                "ORDER BY fecha ASC";

        // Abrir conexión, preparar la consulta y ejecutarla con auto-cierre de recursos
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Recorrer cada fila del resultado (un registro por día con ventas)
            while (rs.next()) {
                // Crear un objeto JSON para representar los datos crudos del día
                JsonObject dia = new JsonObject();
                // Agregar la fecha del día en formato "YYYY-MM-DD"
                dia.addProperty("fecha", rs.getString("fecha"));
                // Agregar el total de ingresos del día retornado por la BD
                dia.addProperty("total", rs.getDouble("total"));
                // Añadir el objeto del día al array de resultados
                resultado.add(dia);
            }

        } catch (Exception excepcion) {
            // Registrar el error en consola; se retornará el array vacío
            System.out.println("Error DashboardDAO.getVentasUltimos7Dias: " + excepcion.getMessage());
        }

        // Retornar el array con un elemento por cada día que tuvo ventas
        return resultado;
    }

    /**
     * Retorna los ingresos y egresos crudos agrupados por día para los últimos 7 días.
     * Solo incluye los días que tienen al menos un movimiento financiero.
     * El cálculo de ganancia neta por día (ingresos - egresos) se delega a DashboardService.
     * Usado para alimentar el gráfico de barras agrupadas "Resumen Semanal".
     *
     * @return JsonArray con objetos { fecha, ingresos, egresos } por cada día con movimientos
     */
    public static JsonArray getResumenSemanal() {
        // Lista donde se acumularán los datos crudos de ingresos y egresos por día
        JsonArray resultado = new JsonArray();

        // SQL: usa CASE WHEN para separar ingresos y egresos en columnas distintas dentro del mismo GROUP BY
        // tipo_movimiento_id = 1 → ingresos (ventas)
        // tipo_movimiento_id IN (2, 3) → egresos (compras + gastos adicionales)
        // COALESCE asegura 0 en vez de NULL si no hay movimientos de ese tipo en el día
        String sql = "SELECT " +
                "    DATE(fecha_movimiento) AS fecha, " +
                "    COALESCE(SUM(CASE WHEN tipo_movimiento_id = 1 THEN monto ELSE 0 END), 0) AS ingresos, " +
                "    COALESCE(SUM(CASE WHEN tipo_movimiento_id IN (2, 3) THEN monto ELSE 0 END), 0) AS egresos " +
                "FROM movimientos_financieros " +
                "WHERE DATE(fecha_movimiento) >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(fecha_movimiento) " +
                "ORDER BY fecha ASC";

        // Abrir conexión, preparar la consulta y ejecutarla con auto-cierre de recursos
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Recorrer cada fila del resultado (un registro por día con movimientos)
            while (rs.next()) {
                // Crear un objeto JSON para representar los datos crudos del día
                JsonObject dia = new JsonObject();
                // Agregar la fecha del día en formato "YYYY-MM-DD"
                dia.addProperty("fecha",    rs.getString("fecha"));
                // Agregar el total de ingresos del día retornado por la BD
                dia.addProperty("ingresos", rs.getDouble("ingresos"));
                // Agregar el total de egresos del día retornado por la BD
                dia.addProperty("egresos",  rs.getDouble("egresos"));
                // Añadir el objeto del día al array de resultados (sin calcular ganancias aquí)
                resultado.add(dia);
            }

        } catch (Exception excepcion) {
            // Registrar el error en consola; se retornará el array vacío
            System.out.println("Error DashboardDAO.getResumenSemanal: " + excepcion.getMessage());
        }

        // Retornar el array con los datos crudos de cada día al servicio para su procesamiento
        return resultado;
    }

    /**
     * Retorna el stock total agrupado por categoría para los productos activos.
     * Usa JOIN entre producto y categoria para obtener el nombre de la categoría.
     * Usado para el gráfico de dona "Stock por Categoría" del dashboard.
     *
     * @return JsonArray con objetos { categoria: "Nombre", totalStock: 0 } ordenados por stock descendente
     */
    public static JsonArray getStockPorCategoria() {
        // Lista donde se acumularán los totales de stock por categoría retornados por la BD
        JsonArray resultado = new JsonArray();

        // SQL: JOIN de producto con categoria para unir el stock con el nombre de la categoría
        // WHERE p.estado = true filtra solo los productos activos en el inventario
        // GROUP BY agrupa todos los productos de la misma categoría en un solo registro
        // SUM(p.stock) acumula el stock total de todos los productos de cada categoría
        // ORDER BY totalStock DESC muestra primero las categorías con más stock
        String sql = "SELECT c.NOMBRE AS categoria, COALESCE(SUM(p.STOCK), 0) AS totalStock " +
                "FROM Producto p " +
                "JOIN Categoria c ON p.CATEGORIA_ID = c.ID_CATEGORIA " +
                "WHERE p.ESTADO = true " +
                "GROUP BY c.ID_CATEGORIA, c.NOMBRE " +
                "ORDER BY totalStock DESC";

        // Abrir conexión, preparar la consulta y ejecutarla con auto-cierre de recursos
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Recorrer cada fila del resultado (un registro por categoría)
            while (rs.next()) {
                // Crear un objeto JSON para representar el stock de una categoría
                JsonObject fila = new JsonObject();
                // Agregar el nombre de la categoría obtenido del JOIN con la tabla categoria
                fila.addProperty("categoria",  rs.getString("categoria"));
                // Agregar el stock total acumulado de todos los productos de esa categoría
                fila.addProperty("totalStock", rs.getInt("totalStock"));
                // Añadir la fila de la categoría al array de resultados
                resultado.add(fila);
            }

        } catch (Exception excepcion) {
            // Registrar el error en consola; se retornará el array vacío
            System.out.println("Error DashboardDAO.getStockPorCategoria: " + excepcion.getMessage());
        }

        // Retornar el array con el stock agrupado por cada categoría activa
        return resultado;
    }

    /**
     * Retorna los datos crudos de los productos más rentables basados en ventas reales.
     * Hace JOIN con Detalle_Venta para obtener las unidades vendidas de cada producto.
     * Retorna: nombre, precioVenta, costoPromedio y unidadesVendidas.
     * El cálculo de ganancia total y margen porcentual se delega a DashboardService.
     * Solo incluye productos activos que hayan sido vendidos al menos una vez.
     * Usado para la lista "Productos Más Rentables" del dashboard.
     *
     * @return JsonArray con objetos { nombre, precioVenta, costoPromedio, unidadesVendidas } por producto
     */
    public static JsonArray getProductosRentables() {
        // Lista donde se acumularán los datos crudos de los productos retornados por la BD
        JsonArray resultado = new JsonArray();

        // SQL: JOIN de Producto con Detalle_Venta para obtener unidades realmente vendidas
        // SUM(dv.CANTIDAD) acumula todas las unidades vendidas de cada producto en todas las ventas
        // WHERE p.ESTADO = true filtra solo productos activos
        // WHERE p.PRECIO_VENTA > 0 evita productos sin precio que causarían división por cero
        // HAVING asegura que solo se incluyan productos con al menos una venta registrada
        // Sin ORDER BY ni LIMIT — el ordenamiento y la selección del top 10 se delegan al Service
        String sql = "SELECT " +
                "    p.NOMBRE AS nombre, " +
                "    p.PRECIO_VENTA AS precioVenta, " +
                "    p.COSTO_PROMEDIO AS costoPromedio, " +
                "    SUM(dv.CANTIDAD) AS unidadesVendidas " +
                "FROM Producto p " +
                "JOIN Detalle_Venta dv ON p.ID_PRODUCTO = dv.PRODUCTO_ID " +
                "WHERE p.ESTADO = true AND p.PRECIO_VENTA > 0 " +
                "GROUP BY p.ID_PRODUCTO, p.NOMBRE, p.PRECIO_VENTA, p.COSTO_PROMEDIO " +
                "HAVING SUM(dv.CANTIDAD) > 0";

        // Abrir conexión, preparar la consulta y ejecutarla con auto-cierre de recursos
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Recorrer cada fila del resultado (un registro por producto vendido)
            while (rs.next()) {
                // Crear un objeto JSON para representar los datos crudos del producto
                JsonObject producto = new JsonObject();
                // Agregar el nombre del producto desde la columna nombre
                producto.addProperty("nombre",           rs.getString("nombre"));
                // Agregar el precio de venta actual del producto desde la columna precioVenta
                producto.addProperty("precioVenta",      rs.getDouble("precioVenta"));
                // Agregar el costo promedio ponderado del producto desde la columna costoPromedio
                producto.addProperty("costoPromedio",    rs.getDouble("costoPromedio"));
                // Agregar las unidades totales vendidas del producto desde la columna unidadesVendidas
                producto.addProperty("unidadesVendidas", rs.getInt("unidadesVendidas"));
                // Añadir el producto al array de resultados sin calcular márgenes aquí
                resultado.add(producto);
            }

        } catch (Exception excepcion) {
            // Registrar el error en consola; se retornará el array vacío
            System.out.println("Error DashboardDAO.getProductosRentables: " + excepcion.getMessage());
        }

        // Retornar el array con los datos crudos de los productos al servicio para su procesamiento
        return resultado;
    }
}
