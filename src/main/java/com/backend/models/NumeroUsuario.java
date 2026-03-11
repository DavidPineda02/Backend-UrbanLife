// Paquete de modelos de datos de la aplicación
package com.backend.models;

/**
 * Modelo que representa un número telefónico asociado a un usuario.
 * Un usuario puede tener múltiples números de teléfono registrados en el sistema.
 */
public class NumeroUsuario {

    /** Identificador único del número telefónico en la base de datos */
    private int idNumero;
    /** Número de teléfono del usuario */
    private String numero;
    /** ID del usuario al que pertenece este número */
    private int usuarioId;

    /**
     * Constructor para crear un número de usuario con ID existente (lectura desde BD).
     * @param idNumero ID del número en la base de datos
     * @param numero Número de teléfono
     * @param usuarioId ID del usuario asociado
     */
    public NumeroUsuario(int idNumero, String numero, int usuarioId) {
        // Asignar el ID del número
        this.idNumero = idNumero;
        // Asignar el número de teléfono
        this.numero = numero;
        // Asignar el ID del usuario asociado
        this.usuarioId = usuarioId;
    }

    /**
     * Constructor para crear un nuevo número de usuario sin ID (inserción en BD).
     * @param numero Número de teléfono
     * @param usuarioId ID del usuario asociado
     */
    public NumeroUsuario(String numero, int usuarioId) {
        // Asignar el número de teléfono
        this.numero = numero;
        // Asignar el ID del usuario asociado
        this.usuarioId = usuarioId;
    }

    /**
     * Obtiene el ID del número telefónico.
     * @return ID del número en la base de datos
     */
    public int getIdNumero() {
        // Retornar el ID del número
        return idNumero;
    }

    /**
     * Establece el ID del número telefónico.
     * @param idNumero Nuevo ID del número
     */
    public void setIdNumero(int idNumero) {
        // Asignar el nuevo ID del número
        this.idNumero = idNumero;
    }

    /**
     * Obtiene el número de teléfono.
     * @return Número de teléfono del usuario
     */
    public String getNumero() {
        // Retornar el número de teléfono
        return numero;
    }

    /**
     * Establece el número de teléfono.
     * @param numero Nuevo número de teléfono
     */
    public void setNumero(String numero) {
        // Asignar el nuevo número de teléfono
        this.numero = numero;
    }

    /**
     * Obtiene el ID del usuario asociado.
     * @return ID del usuario al que pertenece este número
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
