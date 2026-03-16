// Paquete de servicios de lógica de negocio
package com.backend.services;

// Constantes de validación centralizadas (FECHA_REGEX, etc.)
import com.backend.helpers.ValidationHelper;
// Para verificar que el cliente existe en la BD
import com.backend.dao.ClienteDAO;
// Para verificar que cada producto existe, está activo y tiene stock suficiente
import com.backend.dao.ProductoDAO;
// Para ejecutar la transacción atómica de creación de venta
import com.backend.dao.VentaDAO;
// Modelo que representa un ítem de la venta
import com.backend.models.DetalleVenta;
// Modelo que representa el cliente asociado a la venta
import com.backend.models.Cliente;
// Modelo que representa un producto del inventario
import com.backend.models.Producto;
// Modelo que representa el encabezado de una venta
import com.backend.models.Venta;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para recorrer cada elemento del array de ítems
import com.google.gson.JsonArray;
// Para verificar el tipo de cada elemento del array
import com.google.gson.JsonElement;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Lista dinámica para construir los detalles de la venta
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * Servicio con la lógica de negocio para el módulo de Ventas.
 * Maneja la creación de ventas con validaciones de negocio,
 * verificación de stock y cálculo automático de totales.
 * Centraliza toda la lógica de validación antes de delegar al DAO.
 */
public class VentaService {

    /** Gson compartido para serializar objetos en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todas las ventas del sistema.
     * @return JsonObject con la lista de ventas y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todas las ventas serializada como JSON
        respuesta.add("data", gson.toJsonTree(VentaDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna una venta por su ID, incluyendo sus ítems (detalles).
     * @param id ID de la venta a buscar
     * @return JsonObject con la venta y sus detalles, o error 404
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar la venta en la base de datos por su ID
        Venta venta = VentaDAO.findById(id);
        // Verificar si la venta no existe
        if (venta == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Venta no encontrada");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Serializar la venta como JsonObject para poder agregarle los detalles
        JsonObject ventaJson = gson.toJsonTree(venta).getAsJsonObject();
        // Obtener los ítems de la venta desde la BD
        List<DetalleVenta> detalles = VentaDAO.findDetallesByVentaId(id);
        // Agregar los detalles como array dentro del objeto venta
        ventaJson.add("detalles", gson.toJsonTree(detalles));

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos completos de la venta (encabezado + detalles)
        respuesta.add("data", ventaJson);
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con la venta y sus detalles
        return respuesta;
    }

    /**
     * Valida los datos y crea una nueva venta con transacción atómica.
     * Verifica fecha, método de pago, cliente, productos, stock y calcula totales.
     * El precio unitario se toma del producto en la BD para evitar manipulación.
     * @param fechaVenta Fecha de la venta en formato "YYYY-MM-DD"
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param clienteId ID del cliente al que se le realiza la venta
     * @param usuarioId ID del usuario autenticado que registra la venta
     * @param items Array JSON con los ítems [{productoId, cantidad}, ...]
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String fechaVenta, String metodoPago,
                                     int clienteId, int usuarioId, JsonArray items) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validación de fecha de venta -----

        // Verificar que la fecha no sea nula ni esté vacía
        if (fechaVenta == null || fechaVenta.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la fecha es obligatoria
            respuesta.addProperty("message", "La fecha de venta es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la fecha tenga el formato correcto YYYY-MM-DD
        if (!fechaVenta.trim().matches(ValidationHelper.FECHA_REGEX)) {
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

        // ----- Validación del cliente -----

        // Verificar que el ID del cliente sea un valor positivo
        if (clienteId <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el cliente es obligatorio
            respuesta.addProperty("message", "El cliente es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Buscar el cliente en la base de datos para verificar que existe
        Cliente cliente = ClienteDAO.findById(clienteId);
        // Verificar si el cliente no existe en la BD
        if (cliente == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el cliente no fue encontrado
            respuesta.addProperty("message", "El cliente no existe");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el cliente esté activo en el sistema
        if (!cliente.isEstado()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el cliente está inactivo
            respuesta.addProperty("message", "El cliente esta inactivo");
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
            respuesta.addProperty("message", "La venta debe tener al menos un item");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Procesar cada ítem: validar producto, stock y calcular subtotales -----

        // Lista donde se acumularán los objetos DetalleVenta validados
        List<DetalleVenta> detalles = new ArrayList<>();
        // Acumulador para calcular el total general de la venta
        double totalVenta = 0;

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
            // Verificar que el ítem tenga el campo precioUnitario
            if (!item.has("precioUnitario")) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que cada ítem debe tener precioUnitario
                respuesta.addProperty("message", "Cada item debe tener un precioUnitario");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }

            // Extraer el ID del producto del ítem
            int productoId = item.get("productoId").getAsInt();
            // Extraer la cantidad del ítem
            int cantidad = item.get("cantidad").getAsInt();
            // Extraer el precio unitario ingresado por el usuario
            double precioUnitario = item.get("precioUnitario").getAsDouble();

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

            // Buscar el producto en la base de datos para obtener precio y stock actual
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
            // Verificar que el stock del producto sea suficiente para la cantidad solicitada
            if (producto.getStock() < cantidad) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando el stock insuficiente con la disponibilidad actual
                respuesta.addProperty("message", "Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }

            // Validar que el precio unitario ingresado sea mayor a 0
            if (precioUnitario <= 0) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el precio debe ser positivo
                respuesta.addProperty("message", "El precio unitario debe ser mayor a 0");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
                // Retornar respuesta de error
                return respuesta;
            }

            // Calcular el subtotal del ítem multiplicando precio por cantidad
            double subtotal = precioUnitario * cantidad;
            // Acumular el subtotal en el total general de la venta
            totalVenta += subtotal;

            // Construir el objeto DetalleVenta sin ID (se asignará en la transacción)
            DetalleVenta detalle = new DetalleVenta(cantidad, precioUnitario, subtotal, 0, productoId);
            // Agregar el detalle validado a la lista
            detalles.add(detalle);
        }

        // ----- Crear la venta con transacción atómica -----

        // Construir el objeto Venta sin ID con el total calculado
        Venta nuevaVenta = new Venta(fechaVenta.trim(), totalVenta, metodoPago.trim(), usuarioId, clienteId);
        // Ejecutar la transacción atómica en el DAO (INSERT venta + detalles + stock + movimiento)
        Venta ventaCreada = VentaDAO.createConDetalles(nuevaVenta, detalles);

        // Verificar si la transacción falló
        if (ventaCreada == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al registrar la venta");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Construir respuesta exitosa con venta y detalles -----

        // Serializar la venta creada como JsonObject para agregarle los detalles
        JsonObject ventaJson = gson.toJsonTree(ventaCreada).getAsJsonObject();
        // Agregar los detalles como array dentro del objeto venta
        ventaJson.add("detalles", gson.toJsonTree(detalles));

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Venta registrada exitosamente");
        // Agregar los datos completos de la venta (encabezado + detalles)
        respuesta.add("data", ventaJson);
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con la venta creada
        return respuesta;
    }
}
