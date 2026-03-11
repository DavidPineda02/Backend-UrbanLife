// Paquete de servicios de la aplicación
package com.backend.services;

// DAO que ejecuta las consultas de la BD y retorna datos crudos
import com.backend.dao.DashboardDAO;
// Para iterar sobre los arrays JSON retornados por el DAO
import com.google.gson.JsonArray;
// Para construir y manipular objetos JSON de respuesta
import com.google.gson.JsonObject;
// Para ordenar la lista de productos por margen en el Service
import java.util.ArrayList;
// Estructura mutable para ordenar objetos antes de convertirlos a JsonArray
import java.util.List;
// Comparator para ordenar por un campo numérico de forma descendente
import java.util.Comparator;

/**
 * Servicio del módulo Dashboard.
 * Capa de lógica de negocio entre DashboardController y DashboardDAO.
 * Responsabilidades:
 * - Solicitar datos crudos al DAO
 * - Aplicar cálculos de negocio (ganancias, márgenes)
 * - Construir respuestas estandarizadas { success, message, data, status }
 * Los cálculos numéricos derivados (ganancia = ingresos - egresos, margen%) se realizan aquí,
 * nunca en el DAO.
 */
public class DashboardService {

    /**
     * Obtiene el resumen general para las tarjetas del dashboard.
     * Solicita al DAO los 6 valores crudos (ingresosHoy, egresosHoy, ingresosMes,
     * egresosMes, productosActivos, clientesActivos) y calcula aquí la lógica de negocio:
     * - gananciasHoy = ingresosHoy - egresosHoy
     * - gananciaMes  = ingresosMes - egresosMes
     *
     * @return JsonObject con { success, message, data: { resumen completo }, status }
     */
    public static JsonObject getResumen() {
        // Objeto de respuesta estandarizada que se retornará al controller
        JsonObject respuesta = new JsonObject();
        try {
            // Solicitar al DAO los datos crudos de movimientos y contadores desde la BD
            JsonObject datosCrudos = DashboardDAO.getResumen();

            // ===== LÓGICA DE NEGOCIO =====
            // Leer los ingresos del día retornados por la BD
            double ingresosHoy  = datosCrudos.get("ingresosHoy").getAsDouble();
            // Leer los egresos del día retornados por la BD
            double egresosHoy   = datosCrudos.get("egresosHoy").getAsDouble();
            // Leer los ingresos del mes retornados por la BD
            double ingresosMes  = datosCrudos.get("ingresosMes").getAsDouble();
            // Leer los egresos del mes retornados por la BD
            double egresosMes   = datosCrudos.get("egresosMes").getAsDouble();

            // Calcular la ganancia neta del día (ingresos del día - egresos del día)
            double gananciasHoy = ingresosHoy - egresosHoy;
            // Calcular la ganancia neta del mes (ingresos del mes - egresos del mes)
            double gananciaMes  = ingresosMes - egresosMes;

            // Agregar los valores calculados al objeto de datos crudos para completar el resumen
            datosCrudos.addProperty("gananciasHoy", gananciasHoy);
            // Agregar la ganancia neta del mes calculada por el servicio
            datosCrudos.addProperty("gananciaMes",  gananciaMes);
            // ===== FIN LÓGICA DE NEGOCIO =====

            // Indicar que la operación fue exitosa
            respuesta.addProperty("success", true);
            // Mensaje descriptivo de la operación completada
            respuesta.addProperty("message", "Resumen del dashboard obtenido correctamente");
            // Adjuntar los datos completos (crudos + calculados) en el campo data
            respuesta.add("data", datosCrudos);
            // Código HTTP 200 para respuesta exitosa
            respuesta.addProperty("status", 200);

        } catch (Exception excepcion) {
            // Registrar el error en consola para diagnóstico
            System.out.println("Error DashboardService.getResumen: " + excepcion.getMessage());
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error genérico para el cliente
            respuesta.addProperty("message", "Error al obtener el resumen del dashboard");
            // Código HTTP 500 para error interno del servidor
            respuesta.addProperty("status", 500);
        }
        // Retornar la respuesta al controller para que la envíe al cliente
        return respuesta;
    }

    /**
     * Obtiene el total de ventas por cada uno de los últimos 7 días.
     * Solo incluye los días que tienen al menos una venta registrada.
     * No requiere lógica de negocio adicional — los datos del DAO son suficientes.
     * Usado para el gráfico de barras "Ventas por Semana".
     *
     * @return JsonObject con { success, message, data: [...], status }
     */
    public static JsonObject getVentasSemanales() {
        // Objeto de respuesta estandarizada que se retornará al controller
        JsonObject respuesta = new JsonObject();
        try {
            // Solicitar al DAO el array de ventas por día de los últimos 7 días
            JsonArray datos = DashboardDAO.getVentasUltimos7Dias();
            // Indicar que la operación fue exitosa
            respuesta.addProperty("success", true);
            // Mensaje descriptivo de la operación completada
            respuesta.addProperty("message", "Ventas de los últimos 7 días obtenidas correctamente");
            // Adjuntar el array de ventas por día directamente en el campo data
            respuesta.add("data", datos);
            // Código HTTP 200 para respuesta exitosa
            respuesta.addProperty("status", 200);
        } catch (Exception excepcion) {
            // Registrar el error en consola para diagnóstico
            System.out.println("Error DashboardService.getVentasSemanales: " + excepcion.getMessage());
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error genérico para el cliente
            respuesta.addProperty("message", "Error al obtener las ventas semanales");
            // Código HTTP 500 para error interno del servidor
            respuesta.addProperty("status", 500);
        }
        // Retornar la respuesta al controller para que la envíe al cliente
        return respuesta;
    }

    /**
     * Obtiene el resumen semanal con ingresos, egresos y ganancia neta por día.
     * Solicita al DAO los datos crudos { fecha, ingresos, egresos } por día y aplica
     * aquí la lógica de negocio: ganancias = ingresos - egresos por cada día.
     * Usado para el gráfico de barras agrupadas "Resumen Semanal".
     *
     * @return JsonObject con { success, message, data: [...], status }
     */
    public static JsonObject getResumenSemanal() {
        // Objeto de respuesta estandarizada que se retornará al controller
        JsonObject respuesta = new JsonObject();
        try {
            // Solicitar al DAO el array de datos crudos (fecha, ingresos, egresos) por día
            JsonArray datosCrudos = DashboardDAO.getResumenSemanal();

            // Array donde se acumularán los objetos diarios con la ganancia calculada
            JsonArray datosConGanancias = new JsonArray();

            // ===== LÓGICA DE NEGOCIO =====
            // Recorrer cada día retornado por el DAO para calcular la ganancia neta
            for (int i = 0; i < datosCrudos.size(); i++) {
                // Obtener el objeto JSON del día actual desde el array del DAO
                JsonObject diaCrudo = datosCrudos.get(i).getAsJsonObject();
                // Leer los ingresos del día desde los datos crudos del DAO
                double ingresos = diaCrudo.get("ingresos").getAsDouble();
                // Leer los egresos del día desde los datos crudos del DAO
                double egresos  = diaCrudo.get("egresos").getAsDouble();

                // Crear el objeto de respuesta del día con todos los campos
                JsonObject dia = new JsonObject();
                // Agregar la fecha del día tal como la retornó la BD
                dia.addProperty("fecha",     diaCrudo.get("fecha").getAsString());
                // Agregar los ingresos del día tal como los retornó la BD
                dia.addProperty("ingresos",  ingresos);
                // Agregar los egresos del día tal como los retornó la BD
                dia.addProperty("egresos",   egresos);
                // Calcular y agregar la ganancia neta del día (ingresos - egresos)
                dia.addProperty("ganancias", ingresos - egresos);
                // Añadir el día procesado al array de respuesta
                datosConGanancias.add(dia);
            }
            // ===== FIN LÓGICA DE NEGOCIO =====

            // Indicar que la operación fue exitosa
            respuesta.addProperty("success", true);
            // Mensaje descriptivo de la operación completada
            respuesta.addProperty("message", "Resumen semanal obtenido correctamente");
            // Adjuntar el array procesado (con ganancias calculadas) en el campo data
            respuesta.add("data", datosConGanancias);
            // Código HTTP 200 para respuesta exitosa
            respuesta.addProperty("status", 200);
        } catch (Exception excepcion) {
            // Registrar el error en consola para diagnóstico
            System.out.println("Error DashboardService.getResumenSemanal: " + excepcion.getMessage());
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error genérico para el cliente
            respuesta.addProperty("message", "Error al obtener el resumen semanal");
            // Código HTTP 500 para error interno del servidor
            respuesta.addProperty("status", 500);
        }
        // Retornar la respuesta al controller para que la envíe al cliente
        return respuesta;
    }

    /**
     * Obtiene el stock total agrupado por categoría para los productos activos.
     * No requiere lógica de negocio adicional — los datos del DAO son suficientes.
     * Usado para el gráfico de dona "Stock por Categoría".
     *
     * @return JsonObject con { success, message, data: [...], status }
     */
    public static JsonObject getStockPorCategoria() {
        // Objeto de respuesta estandarizada que se retornará al controller
        JsonObject respuesta = new JsonObject();
        try {
            // Solicitar al DAO el array de stock agrupado por categoría desde la BD
            JsonArray datos = DashboardDAO.getStockPorCategoria();
            // Indicar que la operación fue exitosa
            respuesta.addProperty("success", true);
            // Mensaje descriptivo de la operación completada
            respuesta.addProperty("message", "Stock por categoría obtenido correctamente");
            // Adjuntar el array de stock por categoría directamente en el campo data
            respuesta.add("data", datos);
            // Código HTTP 200 para respuesta exitosa
            respuesta.addProperty("status", 200);
        } catch (Exception excepcion) {
            // Registrar el error en consola para diagnóstico
            System.out.println("Error DashboardService.getStockPorCategoria: " + excepcion.getMessage());
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error genérico para el cliente
            respuesta.addProperty("message", "Error al obtener el stock por categoría");
            // Código HTTP 500 para error interno del servidor
            respuesta.addProperty("status", 500);
        }
        // Retornar la respuesta al controller para que la envíe al cliente
        return respuesta;
    }

    /**
     * Obtiene los 10 productos más rentables con sus márgenes calculados.
     * Solicita al DAO los datos crudos { nombre, precioVenta, costoPromedio } y aplica
     * aquí la lógica de negocio:
     * - margen = precioVenta - costoPromedio
     * - margenPorcentaje = ((precioVenta - costoPromedio) / precioVenta) * 100
     * Usado para la lista "Productos Más Rentables" del dashboard.
     *
     * @return JsonObject con { success, message, data: [...], status }
     */
    public static JsonObject getProductosRentables() {
        // Objeto de respuesta estandarizada que se retornará al controller
        JsonObject respuesta = new JsonObject();
        try {
            // Solicitar al DAO el array de datos crudos (nombre, precioVenta, costoPromedio)
            JsonArray datosCrudos = DashboardDAO.getProductosRentables();

            // Lista intermedia para poder ordenar los productos antes de armar el JsonArray
            List<JsonObject> listaProductos = new ArrayList<>();

            // ===== LÓGICA DE NEGOCIO =====
            // Recorrer cada producto retornado por el DAO para calcular sus márgenes
            for (int i = 0; i < datosCrudos.size(); i++) {
                // Obtener el objeto JSON del producto actual desde el array del DAO
                JsonObject productoCrudo = datosCrudos.get(i).getAsJsonObject();
                // Leer el precio de venta del producto desde los datos crudos del DAO
                double precioVenta   = productoCrudo.get("precioVenta").getAsDouble();
                // Leer el costo promedio del producto desde los datos crudos del DAO
                double costoPromedio = productoCrudo.get("costoPromedio").getAsDouble();

                // Calcular el margen absoluto (ganancia bruta por unidad vendida)
                double margen = precioVenta - costoPromedio;
                // Calcular el margen porcentual y redondearlo a 2 decimales
                double margenPorcentaje = Math.round(((margen / precioVenta) * 100) * 100.0) / 100.0;

                // Crear el objeto de respuesta del producto con todos los campos
                JsonObject producto = new JsonObject();
                // Agregar el nombre del producto tal como lo retornó la BD
                producto.addProperty("nombre",           productoCrudo.get("nombre").getAsString());
                // Agregar el precio de venta tal como lo retornó la BD
                producto.addProperty("precioVenta",      precioVenta);
                // Agregar el costo promedio tal como lo retornó la BD
                producto.addProperty("costoPromedio",    costoPromedio);
                // Agregar el margen absoluto calculado por el servicio
                producto.addProperty("margen",           margen);
                // Agregar el margen porcentual calculado y redondeado por el servicio
                producto.addProperty("margenPorcentaje", margenPorcentaje);
                // Añadir el producto a la lista intermedia para ordenar después
                listaProductos.add(producto);
            }

            // Ordenar la lista por margen absoluto de mayor a menor (regla de negocio: top más rentables)
            listaProductos.sort(Comparator.comparingDouble(
                    p -> -p.get("margen").getAsDouble()
            ));

            // Tomar solo los 10 primeros (top 10 más rentables) — regla de negocio aplicada en el Service
            JsonArray datosConMargenes = new JsonArray();
            // Limitar al mínimo entre el tamaño real de la lista y 10 para evitar IndexOutOfBounds
            int limite = Math.min(listaProductos.size(), 10);
            // Agregar los primeros 'limite' productos ya ordenados al array de respuesta final
            for (int i = 0; i < limite; i++) {
                datosConMargenes.add(listaProductos.get(i));
            }
            // ===== FIN LÓGICA DE NEGOCIO =====

            // Indicar que la operación fue exitosa
            respuesta.addProperty("success", true);
            // Mensaje descriptivo de la operación completada
            respuesta.addProperty("message", "Productos más rentables obtenidos correctamente");
            // Adjuntar el array procesado (con márgenes calculados) en el campo data
            respuesta.add("data", datosConMargenes);
            // Código HTTP 200 para respuesta exitosa
            respuesta.addProperty("status", 200);
        } catch (Exception excepcion) {
            // Registrar el error en consola para diagnóstico
            System.out.println("Error DashboardService.getProductosRentables: " + excepcion.getMessage());
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error genérico para el cliente
            respuesta.addProperty("message", "Error al obtener los productos más rentables");
            // Código HTTP 500 para error interno del servidor
            respuesta.addProperty("status", 500);
        }
        // Retornar la respuesta al controller para que la envíe al cliente
        return respuesta;
    }
}
