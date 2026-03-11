// Paquete de servicios de lógica de negocio
package com.backend.services;

// Constantes de validación centralizadas (FECHA_REGEX, etc.)
import com.backend.helpers.ValidationHelper;
// Para verificar que el tipo de gasto existe en la BD
import com.backend.dao.TipoGastoDAO;
// Para verificar que la compra asociada existe (si aplica)
import com.backend.dao.CompraDAO;
// Para ejecutar la transacción atómica de creación de gasto
import com.backend.dao.GastoAdicionalDAO;
// Modelo que representa un gasto adicional del negocio
import com.backend.models.GastoAdicional;
// Modelo que representa un tipo de gasto (categoría)
import com.backend.models.TipoGasto;
// Modelo que representa una compra del negocio
import com.backend.models.Compra;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el módulo de Gastos Adicionales.
 * Maneja la creación de gastos con validaciones de negocio,
 * verificación de tipo de gasto y compra asociada opcional.
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
     * Retorna todos los tipos de gasto disponibles para el dropdown del frontend.
     * @return JsonObject con la lista de tipos de gasto y código 200
     */
    public static JsonObject findAllTiposGasto() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todos los tipos de gasto serializada como JSON
        respuesta.add("data", gson.toJsonTree(TipoGastoDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Valida los datos y crea un nuevo gasto adicional con transacción atómica.
     * Verifica monto, descripción, fecha, método de pago, tipo de gasto y compra opcional.
     * @param monto Monto del gasto adicional
     * @param descripcion Descripción del concepto del gasto
     * @param fechaRegistro Fecha de registro en formato "YYYY-MM-DD"
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param compraId ID de la compra asociada (null si no aplica)
     * @param tipoGastoId ID del tipo de gasto
     * @param usuarioId ID del usuario autenticado que registra el gasto
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(double monto, String descripcion, String fechaRegistro,
                                     String metodoPago, Integer compraId, int tipoGastoId, int usuarioId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validación de monto -----

        // Verificar que el monto sea un valor positivo
        if (monto <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el monto debe ser positivo
            respuesta.addProperty("message", "El monto debe ser mayor a 0");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validación de descripción -----

        // Verificar que la descripción no sea nula ni esté vacía
        if (descripcion == null || descripcion.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la descripción es obligatoria
            respuesta.addProperty("message", "La descripcion es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validación de fecha de registro -----

        // Verificar que la fecha no sea nula ni esté vacía
        if (fechaRegistro == null || fechaRegistro.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la fecha es obligatoria
            respuesta.addProperty("message", "La fecha de registro es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la fecha tenga el formato correcto YYYY-MM-DD
        if (!fechaRegistro.trim().matches(ValidationHelper.FECHA_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato requerido
            respuesta.addProperty("message", "La fecha debe tener el formato YYYY-MM-DD");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validación de método de pago -----

        // Verificar que el método de pago no sea nulo ni esté vacío
        if (metodoPago == null || metodoPago.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el método de pago es obligatorio
            respuesta.addProperty("message", "El metodo de pago es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el método de pago sea uno de los valores permitidos por el ENUM
        if (!metodoPago.trim().equals("Transferencia") && !metodoPago.trim().equals("Efectivo")) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando los valores permitidos
            respuesta.addProperty("message", "El metodo de pago debe ser 'Transferencia' o 'Efectivo'");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validación del tipo de gasto -----

        // Verificar que el ID del tipo de gasto sea un valor positivo
        if (tipoGastoId <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el tipo de gasto es obligatorio
            respuesta.addProperty("message", "El tipo de gasto es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Buscar el tipo de gasto en la base de datos para verificar que existe
        TipoGasto tipoGasto = TipoGastoDAO.findById(tipoGastoId);
        // Verificar si el tipo de gasto no existe en la BD
        if (tipoGasto == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el tipo de gasto no fue encontrado
            respuesta.addProperty("message", "El tipo de gasto no existe");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validación de compra asociada (opcional) -----

        // Verificar si se proporcionó un ID de compra asociada
        if (compraId != null && compraId > 0) {
            // Buscar la compra en la base de datos para verificar que existe
            Compra compra = CompraDAO.findById(compraId);
            // Verificar si la compra no existe en la BD
            if (compra == null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que la compra asociada no fue encontrada
                respuesta.addProperty("message", "La compra asociada no existe");
                // Código HTTP 404 Not Found
                respuesta.addProperty("status", 404);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Crear el gasto con transacción atómica -----

        // Construir el objeto GastoAdicional sin ID con los datos validados
        GastoAdicional nuevoGasto = new GastoAdicional(monto, descripcion.trim(),
                fechaRegistro.trim(), metodoPago.trim(), compraId, tipoGastoId, usuarioId);
        // Ejecutar la transacción atómica en el DAO (INSERT gasto + INSERT movimiento financiero)
        GastoAdicional gastoCreado = GastoAdicionalDAO.createConMovimiento(nuevoGasto);

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
