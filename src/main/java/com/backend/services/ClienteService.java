// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para operaciones CRUD de clientes en la base de datos
import com.backend.dao.ClienteDAO;
// Constantes de validación centralizadas (EMAIL_REGEX, etc.)
import com.backend.helpers.ValidationHelper;
// Entidad del cliente del sistema
import com.backend.models.Cliente;
// Para serializar objetos Java a JSON
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

/**
 * Servicio con la lógica de negocio para el CRUD de clientes.
 * Maneja la creación, actualización y gestión del directorio de clientes.
 * Centraliza todas las validaciones y operaciones de gestión de clientes.
 */
public class ClienteService {

    /** Gson compartido para serializar objetos Cliente en la respuesta */
    private static final Gson gson = new Gson();

    /**
     * Retorna todos los clientes del sistema.
     * @return JsonObject con la lista de clientes y código 200
     */
    public static JsonObject findAll() {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar la lista de todos los clientes serializada como JSON
        respuesta.add("data", gson.toJsonTree(ClienteDAO.findAll()));
        // Agregar el código HTTP 200 para que el controller lo extraiga
        respuesta.addProperty("status", 200);
        // Retornar la respuesta armada
        return respuesta;
    }

    /**
     * Retorna un cliente por su ID.
     * @param id ID del cliente a buscar
     * @return JsonObject con el cliente o error 404 si no existe
     */
    public static JsonObject findById(int id) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el cliente en la base de datos por su ID
        Cliente cliente = ClienteDAO.findById(id);
        // Verificar si el cliente no existe
        if (cliente == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Cliente no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar los datos del cliente encontrado serializado como JSON
        respuesta.add("data", gson.toJsonTree(cliente));
        // Agregar el código HTTP 200
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el cliente
        return respuesta;
    }

    /**
     * Valida y crea un nuevo cliente en el directorio.
     * El nombre y documento son obligatorios. El documento debe ser único.
     * Correo, teléfono, dirección y ciudad son opcionales.
     * @param nombre Nombre completo del cliente (obligatorio, 2-100 caracteres)
     * @param documento Número de documento de identidad (obligatorio, único, mínimo 5 dígitos)
     * @param correo Correo electrónico (opcional, formato válido si viene)
     * @param telefono Número de teléfono (opcional, 7-15 caracteres si viene)
     * @param direccion Dirección física (opcional, máximo 200 caracteres)
     * @param ciudad Ciudad de residencia (opcional, máximo 100 caracteres)
     * @return JsonObject con el resultado de la creación
     */
    public static JsonObject create(String nombre, Long documento, String correo,
                                    String telefono, String direccion, String ciudad) {
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

        // ----- Validaciones del campo Documento -----

        // Verificar que el documento no sea nulo
        if (documento == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el documento es obligatorio
            respuesta.addProperty("message", "El documento es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la cédula colombiana tenga al menos 6 dígitos (mínimo 1.000.000)
        if (documento < 1_000_000L) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el mínimo de dígitos requeridos para una cédula colombiana
            respuesta.addProperty("message", "La cédula colombiana debe tener al menos 6 dígitos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la cédula colombiana no supere los 10 dígitos (máximo 9.999.999.999)
        if (documento > 9_999_999_999L) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de dígitos permitidos para una cédula colombiana
            respuesta.addProperty("message", "La cédula colombiana no puede superar los 10 dígitos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Correo (opcional) -----

        // Verificar que el correo (si viene) tenga formato válido usando el regex centralizado
        if (correo != null && !correo.isBlank() && !correo.matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido
            respuesta.addProperty("message", "El correo no tiene un formato válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Teléfono (opcional) -----

        // Verificar que el teléfono (si viene) sea solo dígitos y tenga entre 7 y 10 caracteres (estándar colombiano)
        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("^\\d{7,10}$")) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato colombiano requerido
            respuesta.addProperty("message", "El teléfono debe contener entre 7 y 10 dígitos numéricos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Dirección (opcional) -----

        // Verificar que la dirección (si viene) no supere los 200 caracteres
        if (direccion != null && direccion.length() > 200) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La dirección no puede superar los 200 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Ciudad (opcional) -----

        // Verificar que la ciudad (si viene) no supere los 100 caracteres
        if (ciudad != null && ciudad.length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La ciudad no puede superar los 100 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del documento -----

        // Verificar que no exista otro cliente con el mismo número de documento
        if (ClienteDAO.findByDocumento(documento) != null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el documento ya está registrado
            respuesta.addProperty("message", "Ya existe un cliente con ese número de documento");
            // Código HTTP 409 Conflict
            respuesta.addProperty("status", 409);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Crear y persistir el cliente -----

        // Normalizar correo a null si viene vacío para evitar strings vacíos en BD
        String correoFinal = (correo != null && !correo.isBlank()) ? correo.trim() : null;
        // Normalizar teléfono a null si viene vacío
        String telefonoFinal = (telefono != null && !telefono.isBlank()) ? telefono.trim() : null;
        // Normalizar dirección a null si viene vacía
        String direccionFinal = (direccion != null && !direccion.isBlank()) ? direccion.trim() : null;
        // Normalizar ciudad a null si viene vacía
        String ciudadFinal = (ciudad != null && !ciudad.isBlank()) ? ciudad.trim() : null;

        // Construir el objeto Cliente con estado activo por defecto
        Cliente nuevo = new Cliente(nombre.trim(), documento, correoFinal, telefonoFinal, direccionFinal, ciudadFinal);
        // Persistir el nuevo cliente en la base de datos
        Cliente creado = ClienteDAO.create(nuevo);

        // Verificar si hubo un error al insertar en la base de datos
        if (creado == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al crear el cliente");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la creación exitosa
        respuesta.addProperty("message", "Cliente creado exitosamente");
        // Agregar los datos del cliente creado (ya con ID asignado) serializado como JSON
        respuesta.add("data", gson.toJsonTree(creado));
        // Código HTTP 201 Created
        respuesta.addProperty("status", 201);
        // Retornar la respuesta con el cliente creado
        return respuesta;
    }

    /**
     * Valida y actualiza un cliente existente (PUT completo).
     * Verifica que el cliente exista y que el documento sea único (si cambió).
     * @param id ID del cliente a actualizar
     * @param nombre Nuevo nombre completo (obligatorio)
     * @param documento Nuevo número de documento (obligatorio, único si cambió)
     * @param correo Nuevo correo (opcional)
     * @param telefono Nuevo teléfono (opcional)
     * @param direccion Nueva dirección (opcional)
     * @param ciudad Nueva ciudad (opcional)
     * @param estado Nuevo estado activo/inactivo
     * @return JsonObject con el resultado de la actualización
     */
    public static JsonObject update(int id, String nombre, Long documento, String correo,
                                    String telefono, String direccion, String ciudad, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Verificar que el cliente existe -----

        // Buscar el cliente en la base de datos por su ID
        Cliente cliente = ClienteDAO.findById(id);
        // Verificar si el cliente no existe
        if (cliente == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Cliente no encontrado");
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

        // ----- Validaciones del campo Documento -----

        // Verificar que el documento no sea nulo
        if (documento == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el documento es obligatorio
            respuesta.addProperty("message", "El documento es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la cédula colombiana tenga al menos 6 dígitos (mínimo 1.000.000)
        if (documento < 1_000_000L) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el mínimo de dígitos requeridos para una cédula colombiana
            respuesta.addProperty("message", "La cédula colombiana debe tener al menos 6 dígitos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }
        // Verificar que la cédula colombiana no supere los 10 dígitos (máximo 9.999.999.999)
        if (documento > 9_999_999_999L) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de dígitos permitidos para una cédula colombiana
            respuesta.addProperty("message", "La cédula colombiana no puede superar los 10 dígitos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Correo (opcional) -----

        // Verificar que el correo (si viene) tenga formato válido usando el regex centralizado
        if (correo != null && !correo.isBlank() && !correo.matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido
            respuesta.addProperty("message", "El correo no tiene un formato válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Teléfono (opcional) -----

        // Verificar que el teléfono (si viene) sea solo dígitos y tenga entre 7 y 10 caracteres (estándar colombiano)
        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("^\\d{7,10}$")) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato colombiano requerido
            respuesta.addProperty("message", "El teléfono debe contener entre 7 y 10 dígitos numéricos");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Dirección (opcional) -----

        // Verificar que la dirección (si viene) no supere los 200 caracteres
        if (direccion != null && direccion.length() > 200) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La dirección no puede superar los 200 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validaciones del campo Ciudad (opcional) -----

        // Verificar que la ciudad (si viene) no supere los 100 caracteres
        if (ciudad != null && ciudad.length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La ciudad no puede superar los 100 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar unicidad del documento (solo si cambió) -----

        // Comparar el nuevo documento con el actual del cliente
        if (!documento.equals(cliente.getDocumento())) {
            // Solo verificar unicidad si el documento cambió para no bloquear el mismo valor
            if (ClienteDAO.findByDocumento(documento) != null) {
                // Indicar que la operación falló
                respuesta.addProperty("success", false);
                // Mensaje indicando que el documento ya está registrado por otro cliente
                respuesta.addProperty("message", "Ya existe un cliente con ese número de documento");
                // Código HTTP 409 Conflict
                respuesta.addProperty("status", 409);
                // Retornar respuesta de error
                return respuesta;
            }
        }

        // ----- Aplicar cambios y persistir -----

        // Actualizar el nombre en el objeto cliente
        cliente.setNombre(nombre.trim());
        // Actualizar el documento numérico en el objeto cliente
        cliente.setDocumento(documento);
        // Normalizar correo a null si viene vacío y actualizar
        cliente.setCorreo((correo != null && !correo.isBlank()) ? correo.trim() : null);
        // Normalizar teléfono a null si viene vacío y actualizar
        cliente.setTelefono((telefono != null && !telefono.isBlank()) ? telefono.trim() : null);
        // Normalizar dirección a null si viene vacía y actualizar
        cliente.setDireccion((direccion != null && !direccion.isBlank()) ? direccion.trim() : null);
        // Normalizar ciudad a null si viene vacía y actualizar
        cliente.setCiudad((ciudad != null && !ciudad.isBlank()) ? ciudad.trim() : null);
        // Actualizar el estado en el objeto cliente
        cliente.setEstado(estado);

        // Persistir los cambios en la base de datos
        if (!ClienteDAO.update(cliente)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el cliente");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando la actualización exitosa
        respuesta.addProperty("message", "Cliente actualizado exitosamente");
        // Agregar los datos actualizados consultando el cliente desde la BD
        respuesta.add("data", gson.toJsonTree(ClienteDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el cliente actualizado
        return respuesta;
    }

    /**
     * Cambia el estado activo/inactivo de un cliente (PATCH).
     * Permite activar o desactivar un cliente sin modificar sus otros datos.
     * @param id ID del cliente
     * @param estado Nuevo estado (true = activo, false = inactivo)
     * @return JsonObject con el resultado del cambio de estado
     */
    public static JsonObject updateEstado(int id, boolean estado) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Buscar el cliente en la base de datos por su ID
        Cliente cliente = ClienteDAO.findById(id);
        // Verificar si el cliente no existe
        if (cliente == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje descriptivo del error
            respuesta.addProperty("message", "Cliente no encontrado");
            // Código HTTP 404 Not Found
            respuesta.addProperty("status", 404);
            // Retornar respuesta de error
            return respuesta;
        }

        // Actualizar el estado en el objeto cliente con el nuevo valor recibido
        cliente.setEstado(estado);

        // Persistir el cambio de estado en la base de datos
        if (!ClienteDAO.update(cliente)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno del servidor
            respuesta.addProperty("message", "Error al actualizar el estado del cliente");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Construir el mensaje según el nuevo estado (activado o desactivado)
        String mensaje = estado ? "Cliente activado exitosamente" : "Cliente desactivado exitosamente";
        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Agregar el mensaje descriptivo del resultado
        respuesta.addProperty("message", mensaje);
        // Agregar los datos actualizados consultando el cliente desde la BD
        respuesta.add("data", gson.toJsonTree(ClienteDAO.findById(id)));
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta con el cliente actualizado
        return respuesta;
    }
}
