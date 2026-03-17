// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para verificar que la categoría asociada exista antes de crear/actualizar
import com.backend.dao.CategoriaDAO;
// Para operaciones CRUD de productos en la base de datos
import com.backend.dao.ProductoDAO;
// Entidad del producto del sistema
import com.backend.models.Producto;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el CRUD de productos.
 * Maneja la creación, actualización y gestión de productos del inventario.
 * Centraliza todas las validaciones y operaciones de gestión de productos.
 */
public class ProductoService {

    /** Gson compartido para serializar objetos Producto en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los productos del sistema.
     * @return JsonObject con la lista de productos y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todos los productos serializada como JSON
        respuesta.add("data", gson.toJsonTree(ProductoDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna un producto por su ID.
     * @param id ID del producto a buscar
     * @return JsonObject con el producto o error 404 si no existe
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el producto en la base de datos por su ID
        Producto producto = ProductoDAO.findById(id);
        // Verificar si el producto no existe
        if (producto == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Producto no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos del producto encontrado serializado como JSON
        respuesta.add("data", gson.toJsonTree(producto));
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el producto
        return respuesta;
    }

    /**
     * Valida y crea un nuevo producto en el inventario.
     * El nombre es obligatorio (2-150 caracteres), el precio de venta debe ser mayor a 0,
     * el stock debe ser mayor o igual a 0, y la categoría debe existir en la BD.
     * @param nombre Nombre del producto (obligatorio)
     * @param descripcion Descripción del producto (opcional)
     * @param precioVenta Precio de venta al público (obligatorio, mayor a 0)
     * @param stock Cantidad inicial en inventario (obligatorio, mayor o igual a 0)
     * @param categoriaId ID de la categoría asociada (obligatorio, debe existir)
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String nombre, String descripcion, double precioVenta,
                                    int stock, int categoriaId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validaciones del campo Nombre -----

        // Verificar que el nombre no sea nulo ni esté vacío
        if (nombre == null || nombre.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el nombre es obligatorio
            respuesta.addProperty("message", "El nombre es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre tenga al menos 2 caracteres
        if (nombre.trim().length() < 2) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el mínimo de caracteres requeridos
            respuesta.addProperty("message", "El nombre debe tener al menos 2 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre no supere los 150 caracteres (límite de la columna en BD)
        if (nombre.trim().length() > 150) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "El nombre no puede superar los 150 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Descripción -----

        // Verificar que la descripción (si viene) no supere los 255 caracteres
        if (descripcion != null && descripcion.length() > 255) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La descripción no puede superar los 255 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Precio de Venta -----

        // Verificar que el precio de venta sea mayor a cero
        if (precioVenta <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el precio debe ser positivo
            respuesta.addProperty("message", "El precio de venta debe ser mayor a 0");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Stock -----

        // Verificar que el stock no sea negativo
        if (stock < 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el stock no puede ser negativo
            respuesta.addProperty("message", "El stock no puede ser negativo");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo CategoriaId -----

        // Verificar que el ID de categoría sea válido (mayor a 0)
        if (categoriaId <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la categoría es obligatoria
            respuesta.addProperty("message", "El id de categoría es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la categoría exista en la base de datos
        if (CategoriaDAO.findById(categoriaId) == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la categoría no fue encontrada
            respuesta.addProperty("message", "La categoría especificada no existe");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir el producto -----

        // Construir el objeto Producto con estado activo y costo promedio en 0 (se calcula automáticamente en CompraDAO)
        Producto nuevo = new Producto(nombre.trim(), descripcion, precioVenta, 0, stock, true, categoriaId);
        // Persistir el nuevo producto en la base de datos
        Producto creado = ProductoDAO.create(nuevo);

        // Verificar si hubo un error al insertar en la base de datos
        if (creado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al crear el producto");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Producto creado exitosamente");
        // Agregar los datos del producto creado (ya con ID asignado) serializado como JSON
        respuesta.add("data", gson.toJsonTree(creado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el producto creado
        return respuesta;
    }

    /**
     * Valida y actualiza un producto existente (PUT completo).
     * Verifica que el producto exista y que la categoría sea válida.
     * @param id ID del producto a actualizar
     * @param nombre Nuevo nombre (obligatorio)
     * @param descripcion Nueva descripción (opcional)
     * @param precioVenta Nuevo precio de venta (obligatorio, mayor a 0)
     * @param stock Nuevo stock (obligatorio, mayor o igual a 0)
     * @param estado Nuevo estado activo/inactivo
     * @param categoriaId Nuevo ID de categoría (obligatorio, debe existir)
     * @return JsonObject con el resultado de la actualización
     */
    public static JsonObject update(int id, String nombre, String descripcion, double precioVenta,
                                    int stock, boolean estado, int categoriaId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el producto existe -----

        // Buscar el producto en la base de datos por su ID
        Producto producto = ProductoDAO.findById(id);
        // Verificar si el producto no existe
        if (producto == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Producto no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Nombre -----

        // Verificar que el nombre no sea nulo ni esté vacío
        if (nombre == null || nombre.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el nombre es obligatorio
            respuesta.addProperty("message", "El nombre es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre tenga al menos 2 caracteres
        if (nombre.trim().length() < 2) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el mínimo de caracteres requeridos
            respuesta.addProperty("message", "El nombre debe tener al menos 2 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el nombre no supere los 150 caracteres (límite de la columna en BD)
        if (nombre.trim().length() > 150) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "El nombre no puede superar los 150 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Descripción -----

        // Verificar que la descripción (si viene) no supere los 255 caracteres
        if (descripcion != null && descripcion.length() > 255) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La descripción no puede superar los 255 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Precio de Venta -----

        // Verificar que el precio de venta sea mayor a cero
        if (precioVenta <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el precio debe ser positivo
            respuesta.addProperty("message", "El precio de venta debe ser mayor a 0");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Stock -----

        // Verificar que el stock no sea negativo
        if (stock < 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el stock no puede ser negativo
            respuesta.addProperty("message", "El stock no puede ser negativo");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo CategoriaId -----

        // Verificar que el ID de categoría sea válido (mayor a 0)
        if (categoriaId <= 0) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la categoría es obligatoria
            respuesta.addProperty("message", "El id de categoría es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la categoría exista en la base de datos
        if (CategoriaDAO.findById(categoriaId) == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la categoría no fue encontrada
            respuesta.addProperty("message", "La categoría especificada no existe");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Aplicar cambios y persistir -----

        // Actualizar el nombre en el objeto producto
        producto.setNombre(nombre.trim());
        // Actualizar la descripción en el objeto producto
        producto.setDescripcion(descripcion);
        // Actualizar el precio de venta en el objeto producto
        producto.setPrecioVenta(precioVenta);
        // El costo promedio NO se modifica aquí, se preserva el valor calculado automáticamente por CompraDAO
        // Actualizar el stock en el objeto producto
        producto.setStock(stock);
        // Actualizar el estado en el objeto producto
        producto.setEstado(estado);
        // Actualizar el ID de categoría en el objeto producto
        producto.setCategoriaId(categoriaId);

        // Persistir los cambios en la base de datos
        if (!ProductoDAO.update(producto)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el producto");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la actualización exitosa
        respuesta.addProperty("message", "Producto actualizado exitosamente");
        // Agregar los datos actualizados consultando el producto desde la BD
        respuesta.add("data", gson.toJsonTree(ProductoDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el producto actualizado
        return respuesta;
    }

    /**
     * Cambia el estado activo/inactivo de un producto (PATCH).
     * Permite activar o desactivar un producto sin modificar sus otros datos.
     * @param id ID del producto
     * @param estado Nuevo estado (true = activo, false = inactivo)
     * @return JsonObject con el resultado del cambio de estado
     */
    public static JsonObject updateEstado(int id, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el producto en la base de datos por su ID
        Producto producto = ProductoDAO.findById(id);
        // Verificar si el producto no existe
        if (producto == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Producto no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Actualizar el estado en el objeto producto con el nuevo valor recibido
        producto.setEstado(estado);

        // Persistir el cambio de estado en la base de datos
        if (!ProductoDAO.update(producto)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el estado del producto");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Construir el mensaje según el nuevo estado (activado o desactivado)
        String mensaje = estado ? "Producto activado exitosamente" : "Producto desactivado exitosamente";
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar el mensaje descriptivo del resultado
        respuesta.addProperty("message", mensaje);
        // Agregar los datos actualizados consultando el producto desde la BD
        respuesta.add("data", gson.toJsonTree(ProductoDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el producto actualizado
        return respuesta;
    }
}
