// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para operaciones CRUD de proveedores en la base de datos
import com.backend.dao.ProveedorDAO;
// Constantes de validación centralizadas (EMAIL_REGEX, etc.)
import com.backend.helpers.ValidationHelper;
// Entidad del proveedor del sistema
import com.backend.models.Proveedor;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el CRUD de proveedores.
 * Maneja la creación, actualización y gestión del directorio de proveedores.
 * Centraliza todas las validaciones y operaciones de gestión de proveedores.
 */
public class ProveedorService {

    /** Gson compartido para serializar objetos Proveedor en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los proveedores del sistema.
     * @return JsonObject con la lista de proveedores y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todos los proveedores serializada como JSON
        respuesta.add("data", gson.toJsonTree(ProveedorDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna un proveedor por su ID.
     * @param id ID del proveedor a buscar
     * @return JsonObject con el proveedor o error 404 si no existe
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el proveedor en la base de datos por su ID
        Proveedor proveedor = ProveedorDAO.findById(id);
        // Verificar si el proveedor no existe
        if (proveedor == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Proveedor no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos del proveedor encontrado serializado como JSON
        respuesta.add("data", gson.toJsonTree(proveedor));
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el proveedor
        return respuesta;
    }

    /**
     * Valida y crea un nuevo proveedor en el directorio.
     * El nombre y el NIT son obligatorios. El NIT debe ser único.
     * Razón social, correo, teléfono, dirección y ciudad son opcionales.
     * @param nombre Nombre comercial del proveedor (obligatorio, 2-100 caracteres)
     * @param razonSocial Razón social legal (obligatorio, máximo 150 caracteres)
     * @param nit Número de identificación tributaria (obligatorio, único, 5-20 caracteres)
     * @param correo Correo electrónico (obligatorio, formato válido)
     * @param telefono Número de teléfono (obligatorio, 7-10 dígitos)
     * @param direccion Dirección física (obligatorio, máximo 200 caracteres)
     * @param ciudad Ciudad donde opera (obligatorio, máximo 100 caracteres)
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String nombre, String razonSocial, String nit,
                                    String correo, String telefono, String direccion, String ciudad) {
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
        // Verificar que el nombre no supere los 100 caracteres
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

        // ----- Validaciones del campo Razón Social (obligatorio) -----

        // Verificar que la razón social no sea nula ni esté vacía
        if (razonSocial == null || razonSocial.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la razón social es obligatoria
            respuesta.addProperty("message", "La razón social es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la razón social no supere los 150 caracteres
        if (razonSocial.trim().length() > 150) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La razón social no puede superar los 150 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo NIT -----

        // Verificar que el NIT no sea nulo ni esté vacío
        if (nit == null || nit.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el NIT es obligatorio
            respuesta.addProperty("message", "El NIT es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el NIT tenga formato colombiano: 9 dígitos, guión opcional, 1 dígito verificador (ej: 900123456-1)
        if (!nit.trim().matches(ValidationHelper.NIT_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato colombiano del NIT
            respuesta.addProperty("message", "El NIT debe tener el formato colombiano: 9 dígitos y dígito verificador (ej: 900123456-1)");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Correo (obligatorio) -----

        // Verificar que el correo no sea nulo ni esté vacío
        if (correo == null || correo.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el correo es obligatorio
            respuesta.addProperty("message", "El correo electrónico es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el correo tenga formato válido usando el regex centralizado
        if (!correo.trim().matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido
            respuesta.addProperty("message", "El correo no tiene un formato válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Teléfono (obligatorio) -----

        // Verificar que el teléfono no sea nulo ni esté vacío
        if (telefono == null || telefono.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el teléfono es obligatorio
            respuesta.addProperty("message", "El teléfono es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el teléfono sea solo dígitos y tenga entre 7 y 10 caracteres (estándar colombiano)
        if (!telefono.trim().matches(ValidationHelper.TELEFONO_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato colombiano requerido
            respuesta.addProperty("message", "El teléfono debe contener entre 7 y 10 dígitos numéricos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Dirección (obligatorio) -----

        // Verificar que la dirección no sea nula ni esté vacía
        if (direccion == null || direccion.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la dirección es obligatoria
            respuesta.addProperty("message", "La dirección es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la dirección no supere los 200 caracteres
        if (direccion.trim().length() > 200) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La dirección no puede superar los 200 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Ciudad (obligatorio) -----

        // Verificar que la ciudad no sea nula ni esté vacía
        if (ciudad == null || ciudad.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la ciudad es obligatoria
            respuesta.addProperty("message", "La ciudad es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la ciudad no supere los 100 caracteres
        if (ciudad.trim().length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La ciudad no puede superar los 100 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del NIT -----

        // Verificar que no exista otro proveedor con el mismo NIT
        if (ProveedorDAO.findByNit(nit.trim()) != null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el NIT ya está registrado
            respuesta.addProperty("message", "Ya existe un proveedor con ese NIT");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del correo -----

        // Verificar que no exista otro proveedor con el mismo correo electrónico
        if (ProveedorDAO.findByCorreo(correo.trim()) != null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el correo ya está registrado por otro proveedor
            respuesta.addProperty("message", "Ya existe un proveedor con ese correo electrónico");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del teléfono -----

        // Verificar que no exista otro proveedor con el mismo número de teléfono
        if (ProveedorDAO.findByTelefono(telefono.trim()) != null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el teléfono ya está registrado por otro proveedor
            respuesta.addProperty("message", "Ya existe un proveedor con ese número de teléfono");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir el proveedor -----

        // Construir el objeto Proveedor con todos los campos obligatorios y estado activo por defecto
        Proveedor nuevo = new Proveedor(nombre.trim(), razonSocial.trim(), nit.trim(), correo.trim(), telefono.trim(), direccion.trim(), ciudad.trim());
        // Persistir el nuevo proveedor en la base de datos
        Proveedor creado = ProveedorDAO.create(nuevo);

        // Verificar si hubo un error al insertar en la base de datos
        if (creado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al crear el proveedor");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Proveedor creado exitosamente");
        // Agregar los datos del proveedor creado (ya con ID asignado) serializado como JSON
        respuesta.add("data", gson.toJsonTree(creado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el proveedor creado
        return respuesta;
    }

    /**
     * Valida y actualiza un proveedor existente (PUT completo).
     * Verifica que el proveedor exista y que el NIT sea único (si cambió).
     * @param id ID del proveedor a actualizar
     * @param nombre Nuevo nombre comercial (obligatorio)
     * @param razonSocial Nueva razón social (obligatorio)
     * @param nit Nuevo NIT (obligatorio, único si cambió)
     * @param correo Nuevo correo (obligatorio, único si cambió)
     * @param telefono Nuevo teléfono (obligatorio, único si cambió)
     * @param direccion Nueva dirección (obligatorio)
     * @param ciudad Nueva ciudad (obligatorio)
     * @param estado Nuevo estado activo/inactivo
     * @return JsonObject con el resultado de la actualización
     */
    public static JsonObject update(int id, String nombre, String razonSocial, String nit,
                                    String correo, String telefono, String direccion, String ciudad, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el proveedor existe -----

        // Buscar el proveedor en la base de datos por su ID
        Proveedor proveedor = ProveedorDAO.findById(id);
        // Verificar si el proveedor no existe
        if (proveedor == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Proveedor no encontrado");
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
        // Verificar que el nombre no supere los 100 caracteres
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

        // ----- Validaciones del campo Razón Social (obligatorio) -----

        // Verificar que la razón social no sea nula ni esté vacía
        if (razonSocial == null || razonSocial.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la razón social es obligatoria
            respuesta.addProperty("message", "La razón social es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la razón social no supere los 150 caracteres
        if (razonSocial.trim().length() > 150) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La razón social no puede superar los 150 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo NIT -----

        // Verificar que el NIT no sea nulo ni esté vacío
        if (nit == null || nit.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el NIT es obligatorio
            respuesta.addProperty("message", "El NIT es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el NIT tenga formato colombiano: 9 dígitos, guión opcional, 1 dígito verificador (ej: 900123456-1)
        if (!nit.trim().matches(ValidationHelper.NIT_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato colombiano del NIT
            respuesta.addProperty("message", "El NIT debe tener el formato colombiano: 9 dígitos y dígito verificador (ej: 900123456-1)");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Correo (obligatorio) -----

        // Verificar que el correo no sea nulo ni esté vacío
        if (correo == null || correo.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el correo es obligatorio
            respuesta.addProperty("message", "El correo electrónico es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el correo tenga formato válido usando el regex centralizado
        if (!correo.trim().matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido
            respuesta.addProperty("message", "El correo no tiene un formato válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Teléfono (obligatorio) -----

        // Verificar que el teléfono no sea nulo ni esté vacío
        if (telefono == null || telefono.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el teléfono es obligatorio
            respuesta.addProperty("message", "El teléfono es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que el teléfono sea solo dígitos y tenga entre 7 y 10 caracteres (estándar colombiano)
        if (!telefono.trim().matches(ValidationHelper.TELEFONO_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato colombiano requerido
            respuesta.addProperty("message", "El teléfono debe contener entre 7 y 10 dígitos numéricos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Dirección (obligatorio) -----

        // Verificar que la dirección no sea nula ni esté vacía
        if (direccion == null || direccion.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la dirección es obligatoria
            respuesta.addProperty("message", "La dirección es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la dirección no supere los 200 caracteres
        if (direccion.trim().length() > 200) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La dirección no puede superar los 200 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Ciudad (obligatorio) -----

        // Verificar que la ciudad no sea nula ni esté vacía
        if (ciudad == null || ciudad.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la ciudad es obligatoria
            respuesta.addProperty("message", "La ciudad es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la ciudad no supere los 100 caracteres
        if (ciudad.trim().length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La ciudad no puede superar los 100 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del NIT (solo si cambió) -----

        // Comparar el nuevo NIT con el actual del proveedor
        if (!nit.trim().equals(proveedor.getNit())) {
            // Solo verificar unicidad si el NIT cambió para no bloquear el mismo valor
            if (ProveedorDAO.findByNit(nit.trim()) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el NIT ya está registrado por otro proveedor
                respuesta.addProperty("message", "Ya existe un proveedor con ese NIT");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Verificar unicidad del correo (solo si cambió) -----

        // Comparar el nuevo correo con el actual del proveedor
        if (!correo.trim().equals(proveedor.getCorreo())) {
            // Solo verificar unicidad si el correo cambió para no bloquear el mismo valor
            if (ProveedorDAO.findByCorreo(correo.trim()) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el correo ya está registrado por otro proveedor
                respuesta.addProperty("message", "Ya existe un proveedor con ese correo electrónico");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Verificar unicidad del teléfono (solo si cambió) -----

        // Comparar el nuevo teléfono con el actual del proveedor
        if (!telefono.trim().equals(proveedor.getTelefono())) {
            // Solo verificar unicidad si el teléfono cambió para no bloquear el mismo valor
            if (ProveedorDAO.findByTelefono(telefono.trim()) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el teléfono ya está registrado por otro proveedor
                respuesta.addProperty("message", "Ya existe un proveedor con ese número de teléfono");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Aplicar cambios y persistir -----

        // Actualizar el nombre en el objeto proveedor
        proveedor.setNombre(nombre.trim());
        // Actualizar la razón social en el objeto proveedor
        proveedor.setRazonSocial(razonSocial.trim());
        // Actualizar el NIT en el objeto proveedor
        proveedor.setNit(nit.trim());
        // Actualizar el correo en el objeto proveedor
        proveedor.setCorreo(correo.trim());
        // Actualizar el teléfono en el objeto proveedor
        proveedor.setTelefono(telefono.trim());
        // Actualizar la dirección en el objeto proveedor
        proveedor.setDireccion(direccion.trim());
        // Actualizar la ciudad en el objeto proveedor
        proveedor.setCiudad(ciudad.trim());
        // Actualizar el estado en el objeto proveedor
        proveedor.setEstado(estado);

        // Persistir los cambios en la base de datos
        if (!ProveedorDAO.update(proveedor)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el proveedor");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la actualización exitosa
        respuesta.addProperty("message", "Proveedor actualizado exitosamente");
        // Agregar los datos actualizados consultando el proveedor desde la BD
        respuesta.add("data", gson.toJsonTree(ProveedorDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el proveedor actualizado
        return respuesta;
    }

    /**
     * Cambia el estado activo/inactivo de un proveedor (PATCH).
     * Permite activar o desactivar un proveedor sin modificar sus otros datos.
     * @param id ID del proveedor
     * @param estado Nuevo estado (true = activo, false = inactivo)
     * @return JsonObject con el resultado del cambio de estado
     */
    public static JsonObject updateEstado(int id, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el proveedor en la base de datos por su ID
        Proveedor proveedor = ProveedorDAO.findById(id);
        // Verificar si el proveedor no existe
        if (proveedor == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Proveedor no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Actualizar el estado en el objeto proveedor con el nuevo valor recibido
        proveedor.setEstado(estado);

        // Persistir el cambio de estado en la base de datos
        if (!ProveedorDAO.update(proveedor)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el estado del proveedor");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Construir el mensaje según el nuevo estado (activado o desactivado)
        String mensaje = estado ? "Proveedor activado exitosamente" : "Proveedor desactivado exitosamente";
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar el mensaje descriptivo del resultado
        respuesta.addProperty("message", mensaje);
        // Agregar los datos actualizados consultando el proveedor desde la BD
        respuesta.add("data", gson.toJsonTree(ProveedorDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el proveedor actualizado
        return respuesta;
    }
}
