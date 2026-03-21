// Paquete de servicios de la aplicación
package com.backend.services;

// DAO para obtener los movimientos financieros de la base de datos (ya incluye JOIN con Tipos_Movimientos)
import com.backend.dao.MovimientoFinancieroDAO;
// Modelo de movimiento financiero (incluye tipoMovimiento y naturaleza del JOIN)
import com.backend.models.MovimientoFinanciero;

// Para construir y manipular objetos JSON de respuesta
import com.google.gson.JsonObject;
// Para construir arrays JSON de datos
import com.google.gson.JsonArray;

// Para manejar listas de movimientos
import java.util.List;

/**
 * Servicio del módulo de Movimientos Financieros.
 * Capa de lógica de negocio entre MovimientoFinancieroController y MovimientoFinancieroDAO.
 * Responsabilidades:
 * - Solicitar movimientos al DAO (que ya trae tipo y naturaleza via JOIN)
 * - Construir respuestas estandarizadas { success, message, data, status }
 * Los movimientos son de solo lectura: se crean automáticamente en transacciones de Ventas, Compras y Gastos.
 */
public class MovimientoFinancieroService {

    /**
     * Obtiene todos los movimientos financieros con tipo y naturaleza (via JOIN en el DAO).
     * Retorna el JSON con campos: idMovimiento, fecha, tipoMovimiento, naturaleza, concepto, monto.
     *
     * @return JsonObject con { success, message, data: [ movimientos enriquecidos ], status }
     */
    public static JsonObject findAll() {
        // Objeto de respuesta estandarizada que se retornará al controller
        JsonObject respuesta = new JsonObject();

        try {
            // Obtener todos los movimientos financieros (ya enriquecidos con tipo y naturaleza via JOIN)
            List<MovimientoFinanciero> movimientos = MovimientoFinancieroDAO.findAll();
            // Array JSON donde se acumularán los movimientos formateados
            JsonArray arrayMovimientos = new JsonArray();

            // Recorrer cada movimiento y construir el JSON para el frontend
            for (MovimientoFinanciero mov : movimientos) {
                // Crear objeto JSON para este movimiento
                JsonObject movJson = new JsonObject();
                // Agregar el ID del movimiento con el nombre que espera el frontend
                movJson.addProperty("idMovimiento", mov.getIdMovsFinancieros());
                // Agregar la fecha del movimiento
                movJson.addProperty("fecha", mov.getFecha());
                // Agregar el concepto descriptivo del movimiento
                movJson.addProperty("concepto", mov.getConcepto());
                // Agregar el monto del movimiento
                movJson.addProperty("monto", mov.getMonto());
                // Agregar el nombre del tipo de movimiento obtenido del JOIN (Venta, Compra, Gasto Adicional)
                movJson.addProperty("tipoMovimiento", mov.getTipoMovimiento() != null ? mov.getTipoMovimiento() : "Desconocido");
                // Agregar la naturaleza del movimiento obtenida del JOIN (Ingreso o Egreso)
                movJson.addProperty("naturaleza", mov.getNaturaleza() != null ? mov.getNaturaleza() : "Desconocido");

                // Agregar el movimiento formateado al array
                arrayMovimientos.add(movJson);
            }

            // Construir respuesta exitosa con los movimientos
            respuesta.addProperty("success", true);
            // Mensaje descriptivo de la operación
            respuesta.addProperty("message", "Movimientos financieros obtenidos correctamente");
            // Agregar el array de movimientos como data
            respuesta.add("data", arrayMovimientos);
            // Código HTTP 200 (OK)
            respuesta.addProperty("status", 200);

        // Capturar cualquier error durante el proceso
        } catch (Exception excepcion) {
            // Log de error en consola para depuración
            System.err.println("Error MovimientoFinancieroService.findAll: " + excepcion.getMessage());
            // Construir respuesta de error interno
            respuesta.addProperty("success", false);
            // Mensaje de error para el cliente
            respuesta.addProperty("message", "Error al obtener los movimientos financieros");
            // Código HTTP 500 (Internal Server Error)
            respuesta.addProperty("status", 500);
        }

        // Retornar la respuesta estandarizada al controller
        return respuesta;
    }
}
