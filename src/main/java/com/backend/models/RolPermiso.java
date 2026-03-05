package com.backend.models;

/**
 * Modelo que representa la relación entre roles y permisos.
 * Es una tabla intermedia que asigna múltiples permisos a un rol.
 */
public class RolPermiso {
    /** Identificador único de la relación rol-permiso */
    private int idRolPermiso;
    /** ID del rol que recibe los permisos */
    private int rolId;
    /** ID del permiso asignado al rol */
    private int permisosId;

    /**
     * Constructor para crear una relación rol-permiso con ID existente.
     * @param idRolPermiso ID de la relación
     * @param rolId ID del rol
     * @param permisosId ID del permiso
     */
    public RolPermiso(int idRolPermiso, int rolId, int permisosId) {
        this.idRolPermiso = idRolPermiso;
        this.rolId = rolId;
        this.permisosId = permisosId;
    }

    /**
     * Constructor para crear una nueva relación rol-permiso (sin ID).
     * @param rolId ID del rol
     * @param permisosId ID del permiso
     */
    public RolPermiso(int rolId, int permisosId) {
        this.rolId = rolId;
        this.permisosId = permisosId;
    }

    // Getters y Setters
    public int getIdRolPermiso() {
        return idRolPermiso;
    }

    public void setIdRolPermiso(int idRolPermiso) {
        this.idRolPermiso = idRolPermiso;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public int getPermisosId() {
        return permisosId;
    }

    public void setPermisosId(int permisosId) {
        this.permisosId = permisosId;
    }
}
