// Paquete de modelos de datos de la aplicación
package com.backend.models;

/**
 * Modelo que representa un correo electrónico asociado a un usuario.
 * Un usuario puede tener múltiples correos electrónicos registrados en el sistema.
 */
public class CorreoUsuario {

    /** Identificador único del correo en la base de datos */
    private int idCorreo;
    /** Dirección de correo electrónico del usuario */
    private String correo;
    /** ID del usuario al que pertenece este correo */
    private int usuarioId;

    /**
     * Constructor para crear un correo de usuario con ID existente (lectura desde BD).
     * @param idCorreo ID del correo en la base de datos
     * @param correo Dirección de correo electrónico
     * @param usuarioId ID del usuario asociado
     */
    public CorreoUsuario(int idCorreo, String correo, int usuarioId) {
        // Asignar el ID del correo
        this.idCorreo = idCorreo;
        // Asignar la dirección de correo electrónico
        this.correo = correo;
        // Asignar el ID del usuario asociado
        this.usuarioId = usuarioId;
    }

    /**
     * Constructor para crear un nuevo correo de usuario sin ID (inserción en BD).
     * @param correo Dirección de correo electrónico
     * @param usuarioId ID del usuario asociado
     */
    public CorreoUsuario(String correo, int usuarioId) {
        // Asignar la dirección de correo electrónico
        this.correo = correo;
        // Asignar el ID del usuario asociado
        this.usuarioId = usuarioId;
    }

    /**
     * Obtiene el ID del correo.
     * @return ID del correo en la base de datos
     */
    public int getIdCorreo() {
        // Retornar el ID del correo
        return idCorreo;
    }

    /**
     * Establece el ID del correo.
     * @param idCorreo Nuevo ID del correo
     */
    public void setIdCorreo(int idCorreo) {
        // Asignar el nuevo ID del correo
        this.idCorreo = idCorreo;
    }

    /**
     * Obtiene la dirección de correo electrónico.
     * @return Dirección de correo electrónico del usuario
     */
    public String getCorreo() {
        // Retornar la dirección de correo electrónico
        return correo;
    }

    /**
     * Establece la dirección de correo electrónico.
     * @param correo Nueva dirección de correo electrónico
     */
    public void setCorreo(String correo) {
        // Asignar la nueva dirección de correo electrónico
        this.correo = correo;
    }

    /**
     * Obtiene el ID del usuario asociado.
     * @return ID del usuario al que pertenece este correo
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
