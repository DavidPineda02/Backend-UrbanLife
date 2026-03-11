// Paquete de servicios de lógica de negocio
package com.backend.services;

// Constantes de validación centralizadas (FECHA_REGEX, etc.)
import com.backend.helpers.ValidationHelper;
// Para ejecutar la transacción atómica de creación de compra
import com.backend.dao.CompraDAO;
// Para verificar que cada producto existe y está activo
import com.backend.dao.ProductoDAO;
// Para verificar que el proveedor existe y está activo
import com.backend.dao.ProveedorDAO;
// Modelo que representa el encabezado de una compra
import com.backend.models.Compra;
// Modelo que representa un ítem de la compra
import com.backend.models.DetalleCompra;
// Modelo que representa un producto del inventario
import com.backend.models.Producto;
// Modelo que representa un proveedor del negocio
import com.backend.models.Proveedor;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para recorrer cada elemento del array de ítems
import com.google.gson.JsonArray;
// Para verificar el tipo de cada elemento del array
import com.google.gson.JsonElement;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Lista dinámica para construir los detalles de la compra
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * Servicio con la lógica de negocio para el módulo de Compras.
 * Maneja la creación de compras con validaciones de negocio,
 * verificación de productos y cálculo automático de totales.
 * Centraliza toda la lógica de validación antes de delegar al DAO.
 */
public class CompraService {

    /** Gson compartido para serializar objetos en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todas las compras del sistema.
     * @return JsonObject con la lista de compras y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todas las compras serializada como JSON
        respuesta.add("data", gson.toJsonTree(CompraDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna una compra por su ID, incluyendo sus ítems (detalles).
     * @param id ID de la compra a buscar
     * @return JsonObject con la compra y sus detalles, o error 404
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar la compra en la base de datos por su ID
        Compra compra = CompraDAO.findById(id);
        // Verificar si la compra no existe
        if (compra == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Compra no encontrada");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Serializar la compra como JsonObject para poder agregarle los detalles
        JsonObject compraJson = gson.toJsonTree(compra).getAsJsonObject();
        // Obtener los ítems de la compra desde la BD
        List<DetalleCompra> detalles = CompraDAO.findDetallesByCompraId(id);
        // Agregar los detalles como array dentro del objeto compra
        compraJson.add("detalles", gson.toJsonTree(detalles));

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos completos de la compra (encabezado + detalles)
        respuesta.add("data", compraJson);
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con la compra y sus detalles
        return respuesta;
    }

    /**
     * Valida los datos y crea una nueva compra con transacción atómica.
     * Verifica fecha, método de pago, proveedor y productos.
     * El costoUnitario viene del frontend (precio de compra al proveedor).
     * @param fechaCompra Fecha de la compra en formato "YYYY-MM-DD"
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param proveedorId ID del proveedor al que se le realiza la compra
     * @param usuarioId ID del usuario autenticado que registra la compra
     * @param items Array JSON con los ítems [{productoId, cantidad, costoUnitario}, ...]
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String fechaCompra, String metodoPago,
                                     int proveedorId, int usuarioId, JsonArray items) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validación de fecha de compra -----

        // Verificar que la fecha no sea nula ni esté vacía
        if (fechaCompra == null || fechaCompra.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la fecha es obligatoria
            respuesta.addProperty("message", "La fecha de compra es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la fecha tenga el formato correcto YYYY-MM-DD
        if (!fechaCompra.trim().matches(ValidationHelper.FECHA_REGEX)) {
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

        // ----- Validación del proveedor -----

        // Verificar que el ID del proveedor sea un valor positivo
        if (proveedorId <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el proveedor es obligatorio
            respuesta.addProperty("message", "El proveedor es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Buscar el proveedor en la base de datos para verificar que existe
        Proveedor proveedor = ProveedorDAO.findById(proveedorId);
        // Verificar si el proveedor no existe en la BD
        if (proveedor == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el proveedor no fue encontrado
            respuesta.addProperty("message", "El proveedor no existe");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el proveedor esté activo en el sistema
        if (!proveedor.isEstado()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el proveedor está inactivo
            respuesta.addProperty("message", "El proveedor esta inactivo");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validación de ítems -----

        // Verificar que el array de ítems no sea nulo ni esté vacío
        if (items == null || items.size() == 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que se requiere al menos un ítem
            respuesta.addProperty("message", "La compra debe tener al menos un item");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Procesar cada ítem: validar producto y calcular subtotales -----

        // Lista donde se acumularán los objetos DetalleCompra validados
        List<DetalleCompra> detalles = new ArrayList<>();
        // Acumulador para calcular el total general de la compra
        double totalCompra = 0;

        // Recorrer cada elemento del array de ítems enviado por el frontend
        for (JsonElement itemElement : items) {
            // Convertir el elemento a JsonObject para leer sus propiedades
            JsonObject item = itemElement.getAsJsonObject();

            // Verificar que el ítem tenga el campo productoId
            if (!item.has("productoId")) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que cada ítem debe tener productoId
                respuesta.addProperty("message", "Cada item debe tener un productoId");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
            // Verificar que el ítem tenga el campo cantidad
            if (!item.has("cantidad")) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que cada ítem debe tener cantidad
                respuesta.addProperty("message", "Cada item debe tener una cantidad");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
            // Verificar que el ítem tenga el campo costoUnitario
            if (!item.has("costoUnitario")) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que cada ítem debe tener costoUnitario
                respuesta.addProperty("message", "Cada item debe tener un costoUnitario");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }

            // Extraer el ID del producto del ítem
            int productoId = item.get("productoId").getAsInt();
            // Extraer la cantidad del ítem
            int cantidad = item.get("cantidad").getAsInt();
            // Extraer el costo unitario de compra del ítem
            double costoUnitario = item.get("costoUnitario").getAsDouble();

            // Verificar que el ID del producto sea un valor positivo
            if (productoId <= 0) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el productoId debe ser válido
                respuesta.addProperty("message", "El productoId debe ser un numero positivo");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
            // Verificar que la cantidad sea al menos 1
            if (cantidad <= 0) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que la cantidad debe ser positiva
                respuesta.addProperty("message", "La cantidad debe ser mayor a 0");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }
            // Verificar que el costo unitario sea un valor positivo
            if (costoUnitario <= 0) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el costo unitario debe ser positivo
                respuesta.addProperty("message", "El costoUnitario debe ser mayor a 0");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }

            // Buscar el producto en la base de datos para verificar que existe
            Producto producto = ProductoDAO.findById(productoId);
            // Verificar que el producto exista en la BD
            if (producto == null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando qué producto no fue encontrado
                respuesta.addProperty("message", "El producto con ID " + productoId + " no existe");
                // Código HTTP 404 Not Found
                respuesta.addProperty("status", 404);
                // Retornar respuesta de error
                return respuesta;
            }
            // Verificar que el producto esté activo en el inventario
            if (!producto.isEstado()) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el producto está inactivo
                respuesta.addProperty("message", "El producto '" + producto.getNombre() + "' esta inactivo");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }

            // Calcular el subtotal del ítem multiplicando costo por cantidad
            double subtotal = costoUnitario * cantidad;
            // Acumular el subtotal en el total general de la compra
            totalCompra += subtotal;

            // Construir el objeto DetalleCompra sin ID (se asignará en la transacción)
            DetalleCompra detalle = new DetalleCompra(cantidad, costoUnitario, subtotal, 0, productoId);
            // Agregar el detalle validado a la lista
            detalles.add(detalle);
        }

        // ----- Crear la compra con transacción atómica -----

        // Construir el objeto Compra sin ID con el total calculado
        Compra nuevaCompra = new Compra(fechaCompra.trim(), totalCompra, metodoPago.trim(), usuarioId, proveedorId);
        // Ejecutar la transacción atómica en el DAO (INSERT compra + detalles + stock + movimiento)
        Compra compraCreada = CompraDAO.createConDetalles(nuevaCompra, detalles);

        // Verificar si la transacción falló
        if (compraCreada == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al registrar la compra");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Construir respuesta exitosa con compra y detalles -----

        // Serializar la compra creada como JsonObject para agregarle los detalles
        JsonObject compraJson = gson.toJsonTree(compraCreada).getAsJsonObject();
        // Agregar los detalles como array dentro del objeto compra
        compraJson.add("detalles", gson.toJsonTree(detalles));

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Compra registrada exitosamente");
        // Agregar los datos completos de la compra (encabezado + detalles)
        respuesta.add("data", compraJson);
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con la compra creada
        return respuesta;
    }
}
