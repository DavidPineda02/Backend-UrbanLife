package com.backend.models;

/**
 * Modelo que representa la relación entre usuarios y roles.
 * Es una tabla intermedia que asigna múltiples roles a un usuario.
 */
public class UsuarioRol {
    /** Identificador único de la relación usuario-rol */
    private int idUsuarioRol;
    /** ID del usuario que recibe el rol */
    private int usuarioId;
    /** ID del rol asignado al usuario */
    private int rolId;

    /**
     * Constructor para crear una relación usuario-rol con ID existente.
     * @param idUsuarioRol ID de la relación
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     */
    public UsuarioRol(int idUsuarioRol, int usuarioId, int rolId) {
        this.idUsuarioRol = idUsuarioRol;
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    /**
     * Constructor para crear una nueva relación usuario-rol (sin ID).
     * @param usuarioId ID del usuario
     * @param rolId ID del rol
     */
    public UsuarioRol(int usuarioId, int rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    // Getters y Setters
    public int getIdUsuarioRol() {
        return idUsuarioRol;
    }

    public void setIdUsuarioRol(int idUsuarioRol) {
        this.idUsuarioRol = idUsuarioRol;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }
}
