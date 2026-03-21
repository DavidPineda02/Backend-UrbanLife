// Paquete de modelos de datos de la aplicación
package com.backend.models;

/**
 * Modelo que representa un teléfono asociado a un usuario.
 * Un usuario puede tener múltiples teléfonos registrados en el sistema.
 * El campo esPrincipal (Boolean) usa el truco de NULL para el UNIQUE constraint:
 * TRUE = teléfono principal (solo uno por usuario), NULL = teléfono secundario (ilimitados).
 */
public class TelefonoUsuario {

    // Identificador único del teléfono en la base de datos (PK)
    private int idTelefono;
    // Número de teléfono del usuario
    private String telefono;
    // Indica si es el teléfono principal del usuario (TRUE=principal, NULL=secundario)
    private Boolean esPrincipal;
    // ID del usuario al que pertenece este teléfono (FK a Usuarios)
    private int usuarioId;

    /**
     * Constructor para crear un teléfono de usuario con ID existente (lectura desde BD).
     * @param idTelefono ID del teléfono en la base de datos
     * @param telefono Número de teléfono
     * @param esPrincipal TRUE si es principal, NULL si es secundario
     * @param usuarioId ID del usuario asociado
     */
    public TelefonoUsuario(int idTelefono, String telefono, Boolean esPrincipal, int usuarioId) {
        // Asignar el ID del teléfono
        this.idTelefono = idTelefono;
        // Asignar el número de teléfono
        this.telefono = telefono;
        // Asignar si es teléfono principal o secundario
        this.esPrincipal = esPrincipal;
        // Asignar el ID del usuario asociado
        this.usuarioId = usuarioId;
    }

    /**
     * Constructor para crear un nuevo teléfono de usuario sin ID (inserción en BD).
     * @param telefono Número de teléfono
     * @param esPrincipal TRUE si es principal, NULL si es secundario
     * @param usuarioId ID del usuario asociado
     */
    public TelefonoUsuario(String telefono, Boolean esPrincipal, int usuarioId) {
        // Asignar el número de teléfono
        this.telefono = telefono;
        // Asignar si es teléfono principal o secundario
        this.esPrincipal = esPrincipal;
        // Asignar el ID del usuario asociado
        this.usuarioId = usuarioId;
    }

    /**
     * Obtiene el ID del teléfono.
     * @return ID del teléfono en la base de datos
     */
    public int getIdTelefono() {
        // Retornar el ID del teléfono
        return idTelefono;
    }

    /**
     * Establece el ID del teléfono.
     * @param idTelefono Nuevo ID del teléfono
     */
    public void setIdTelefono(int idTelefono) {
        // Asignar el nuevo ID del teléfono
        this.idTelefono = idTelefono;
    }

    /**
     * Obtiene el número de teléfono.
     * @return Número de teléfono del usuario
     */
    public String getTelefono() {
        // Retornar el número de teléfono
        return telefono;
    }

    /**
     * Establece el número de teléfono.
     * @param telefono Nuevo número de teléfono
     */
    public void setTelefono(String telefono) {
        // Asignar el nuevo número de teléfono
        this.telefono = telefono;
    }

    /**
     * Obtiene si este teléfono es el principal del usuario.
     * @return TRUE si es principal, NULL si es secundario
     */
    public Boolean getEsPrincipal() {
        // Retornar si es teléfono principal
        return esPrincipal;
    }

    /**
     * Establece si este teléfono es el principal del usuario.
     * @param esPrincipal TRUE para principal, NULL para secundario
     */
    public void setEsPrincipal(Boolean esPrincipal) {
        // Asignar si es teléfono principal o secundario
        this.esPrincipal = esPrincipal;
    }

    /**
     * Obtiene el ID del usuario asociado.
     * @return ID del usuario al que pertenece este teléfono
     */
    public int getUsuarioId() {
        // Retornar el ID del usuario
        return usuarioId;
    }

    /**
     * Establece el ID del usuario asociado.
     * @param usuarioId Nuevo ID del usuario
     */
    public void setUsuarioId(int usuarioId) {
        // Asignar el nuevo ID del usuario
        this.usuarioId = usuarioId;
    }
}
