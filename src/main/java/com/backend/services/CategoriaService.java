// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para operaciones CRUD de categorías en la base de datos
import com.backend.dao.CategoriaDAO;
// Entidad de la categoría del sistema
import com.backend.models.Categoria;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el CRUD de categorías.
 * Maneja la creación, actualización y gestión de categorías de productos.
 * Centraliza todas las operaciones de gestión de categorías.
 */
public class CategoriaService {

    /** Gson compartido para serializar objetos Categoria en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todas las categorías del sistema.
     * @return JsonObject con la lista de categorías y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todas las categorías serializada como JSON
        respuesta.add("data", gson.toJsonTree(CategoriaDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna una categoría por su ID.
     * @param id ID de la categoría a buscar
     * @return JsonObject con la categoría o error 404 si no existe
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar la categoría en la base de datos por su ID
        Categoria categoria = CategoriaDAO.findById(id);
        // Verificar si la categoría no existe
        if (categoria == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Categoría no encontrada");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos de la categoría encontrada serializada como JSON
        respuesta.add("data", gson.toJsonTree(categoria));
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con la categoría
        return respuesta;
    }

    /**
     * Valida y crea una nueva categoría.
     * El nombre es obligatorio (2-100 caracteres) y debe ser único.
     * La descripción es opcional con máximo 255 caracteres.
     * @param nombre Nombre de la categoría (obligatorio)
     * @param descripcion Descripción de la categoría (opcional)
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String nombre, String descripcion) {
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
        // Verificar que el nombre no supere los 100 caracteres (límite de la columna en BD)
        if (nombre.trim().length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "El nombre no puede superar los 100 caracteres");
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

        // ----- Verificar unicidad del nombre -----

        // Verificar que no exista otra categoría con el mismo nombre en la BD
        if (CategoriaDAO.findByNombre(nombre.trim()) != null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el nombre ya está en uso
            respuesta.addProperty("message", "Ya existe una categoría con ese nombre");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir la categoría -----

        // Construir el objeto Categoria con estado activo por defecto
        Categoria nueva = new Categoria(nombre.trim(), descripcion, true);
        // Persistir la nueva categoría en la base de datos
        Categoria creada = CategoriaDAO.create(nueva);

        // Verificar si hubo un error al insertar en la base de datos
        if (creada == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al crear la categoría");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Categoría creada exitosamente");
        // Agregar los datos de la categoría creada (ya con ID asignado) serializada como JSON
        respuesta.add("data", gson.toJsonTree(creada));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con la categoría creada
        return respuesta;
    }

    /**
     * Valida y actualiza una categoría existente (PUT completo).
     * Verifica que la categoría exista y que el nuevo nombre sea único (si cambió).
     * @param id ID de la categoría a actualizar
     * @param nombre Nuevo nombre (obligatorio)
     * @param descripcion Nueva descripción (opcional)
     * @param estado Nuevo estado activo/inactivo
     * @return JsonObject con el resultado de la actualización
     */
    public static JsonObject update(int id, String nombre, String descripcion, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que la categoría existe -----

        // Buscar la categoría en la base de datos por su ID
        Categoria categoria = CategoriaDAO.findById(id);
        // Verificar si la categoría no existe
        if (categoria == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Categoría no encontrada");
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
        // Verificar que el nombre no supere los 100 caracteres (límite de la columna en BD)
        if (nombre.trim().length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "El nombre no puede superar los 100 caracteres");
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

        // ----- Verificar unicidad del nombre (solo si cambió) -----

        // Comparar el nuevo nombre con el actual ignorando mayúsculas/minúsculas
        if (!nombre.trim().equalsIgnoreCase(categoria.getNombre())) {
            // Solo verificar unicidad si el nombre cambió (para no bloquear actualizar el mismo nombre)
            if (CategoriaDAO.findByNombre(nombre.trim()) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el nombre ya está en uso por otra categoría
                respuesta.addProperty("message", "Ya existe una categoría con ese nombre");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Aplicar cambios y persistir -----

        // Actualizar el nombre en el objeto categoría
        categoria.setNombre(nombre.trim());
        // Actualizar la descripción en el objeto categoría
        categoria.setDescripcion(descripcion);
        // Actualizar el estado en el objeto categoría
        categoria.setEstado(estado);

        // Persistir los cambios en la base de datos
        if (!CategoriaDAO.update(categoria)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar la categoría");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la actualización exitosa
        respuesta.addProperty("message", "Categoría actualizada exitosamente");
        // Agregar los datos actualizados consultando la categoría desde la BD
        respuesta.add("data", gson.toJsonTree(CategoriaDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con la categoría actualizada
        return respuesta;
    }

    /**
     * Cambia el estado activo/inactivo de una categoría (PATCH).
     * Permite activar o desactivar una categoría sin modificar sus otros datos.
     * @param id ID de la categoría
     * @param estado Nuevo estado (true = activa, false = inactiva)
     * @return JsonObject con el resultado del cambio de estado
     */
    public static JsonObject updateEstado(int id, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar la categoría en la base de datos por su ID
        Categoria categoria = CategoriaDAO.findById(id);
        // Verificar si la categoría no existe
        if (categoria == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Categoría no encontrada");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Actualizar el estado en el objeto categoría con el nuevo valor recibido
        categoria.setEstado(estado);

        // Persistir el cambio de estado en la base de datos
        if (!CategoriaDAO.update(categoria)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el estado");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Construir el mensaje según el nuevo estado (activada o desactivada)
        String mensaje = estado ? "Categoría activada exitosamente" : "Categoría desactivada exitosamente";
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar el mensaje descriptivo del resultado
        respuesta.addProperty("message", mensaje);
        // Agregar los datos actualizados consultando la categoría desde la BD
        respuesta.add("data", gson.toJsonTree(CategoriaDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con la categoría actualizada
        return respuesta;
    }
}
