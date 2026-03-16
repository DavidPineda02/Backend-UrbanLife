// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para ejecutar la transacción atómica de creación de gasto
import com.backend.dao.GastoAdicionalDAO;
// Modelo que representa un gasto adicional del negocio
import com.backend.models.GastoAdicional;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el módulo de Gastos Adicionales.
 * Maneja la creación de gastos con validaciones de negocio.
 * Centraliza toda la lógica de validación antes de delegar al DAO.
 */
public class GastoAdicionalService {

    /** Gson compartido para serializar objetos en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los gastos adicionales del sistema.
     * @return JsonObject con la lista de gastos y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todos los gastos serializada como JSON
        respuesta.add("data", gson.toJsonTree(GastoAdicionalDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna un gasto adicional por su ID.
     * @param id ID del gasto a buscar
     * @return JsonObject con el gasto encontrado, o error 404
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el gasto en la base de datos por su ID
        GastoAdicional gasto = GastoAdicionalDAO.findById(id);
        // Verificar si el gasto no existe
        if (gasto == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Gasto adicional no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos del gasto serializado como JSON
        respuesta.add("data", gson.toJsonTree(gasto));
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el gasto encontrado
        return respuesta;
    }

    /**
     * Valida los datos y crea un nuevo gasto adicional con transacción atómica.
     * Verifica monto, descripción, fecha y método de pago.
     * @param monto Monto del gasto adicional
     * @param descripcion Descripción del concepto del gasto
     * @param fechaRegistro Fecha de registro en formato "YYYY-MM-DD"
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param usuarioId ID del usuario autenticado que registra el gasto
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(double monto, String descripcion, String fechaRegistro,
                                     String metodoPago, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validación de monto -----

        if (monto <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el monto debe ser positivo
            respuesta.addProperty("message", "El monto debe ser mayor a 0");
            // Agregar el código HTTP 400 para que el controller lo extraiga
            respuesta.addProperty("status", 400);
            // Retornar la respuesta de error
            return respuesta;
        }

        // ----- Validación de descripción -----
        if (descripcion == null || descripcion.trim().isEmpty()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la descripción es requerida
            respuesta.addProperty("message", "La descripción es obligatoria");
            // Agregar el código HTTP 400 para que el controller lo extraiga
            respuesta.addProperty("status", 400);
            // Retornar la respuesta de error
            return respuesta;
        }

        // ----- Validación de fecha -----
        if (fechaRegistro == null || fechaRegistro.trim().isEmpty()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la fecha es requerida
            respuesta.addProperty("message", "La fecha de registro es obligatoria");
            // Agregar el código HTTP 400 para que el controller lo extraiga
            respuesta.addProperty("status", 400);
            // Retornar la respuesta de error
            return respuesta;
        }

        // ----- Validación de método de pago -----
        if (!metodoPago.equals("Transferencia") && !metodoPago.equals("Efectivo")) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el método de pago es inválido
            respuesta.addProperty("message", "El método de pago debe ser 'Transferencia' o 'Efectivo'");
            // Agregar el código HTTP 400 para que el controller lo extraiga
            respuesta.addProperty("status", 400);
            // Retornar la respuesta de error
            return respuesta;
        }

        // ----- Creación del objeto GastoAdicional -----
        GastoAdicional nuevoGasto = new GastoAdicional(monto, descripcion, fechaRegistro, metodoPago);

        // ----- Crear el gasto con transacción atómica -----

        // Ejecutar la transacción atómica en el DAO (INSERT gasto + INSERT movimiento financiero)
        GastoAdicional gastoCreado = GastoAdicionalDAO.create(nuevoGasto, usuarioId);

        // Verificar si la transacción falló
        if (gastoCreado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al registrar el gasto adicional");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Construir respuesta exitosa -----

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Gasto adicional registrado exitosamente");
        // Agregar los datos del gasto creado serializado como JSON
        respuesta.add("data", gson.toJsonTree(gastoCreado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el gasto creado
        return respuesta;
    }
}
