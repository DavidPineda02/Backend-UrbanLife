// Paquete de modelos de datos de la aplicación
package com.backend.models;

/**
 * Modelo que representa la relación entre roles y permisos.
 * Es una tabla intermedia (muchos a muchos) que asigna múltiples permisos a un rol.
 */
public class RolPermiso {

    /** Identificador único de la relación rol-permiso en la base de datos */
    private int idRolPermiso;
    /** ID del rol que recibe los permisos */
    private int rolId;
    /** ID del permiso asignado al rol */
    private int permisosId;

    /**
     * Constructor para crear una relación rol-permiso con ID existente (lectura desde BD).
     * @param idRolPermiso ID de la relación en la base de datos
     * @param rolId ID del rol asociado
     * @param permisosId ID del permiso asociado
     */
    public RolPermiso(int idRolPermiso, int rolId, int permisosId) {
        // Asignar el ID de la relación rol-permiso
        this.idRolPermiso = idRolPermiso;
        // Asignar el ID del rol
        this.rolId = rolId;
        // Asignar el ID del permiso
        this.permisosId = permisosId;
    }

    /**
     * Constructor para crear una nueva relación rol-permiso sin ID (inserción en BD).
     * @param rolId ID del rol asociado
     * @param permisosId ID del permiso asociado
     */
    public RolPermiso(int rolId, int permisosId) {
        // Asignar el ID del rol
        this.rolId = rolId;
        // Asignar el ID del permiso
        this.permisosId = permisosId;
    }

    /**
     * Obtiene el ID de la relación rol-permiso.
     * @return ID de la relación en la base de datos
     */
    public int getIdRolPermiso() {
        // Retornar el ID de la relación
        return idRolPermiso;
    }

    /**
     * Establece el ID de la relación rol-permiso.
     * @param idRolPermiso Nuevo ID de la relación
     */
    public void setIdRolPermiso(int idRolPermiso) {
        // Asignar el nuevo ID de la relación
        this.idRolPermiso = idRolPermiso;
    }

    /**
     * Obtiene el ID del rol asociado.
     * @return ID del rol
     */
    public int getRolId() {
        // Retornar el ID del rol
        return rolId;
    }

    /**
     * Establece el ID del rol asociado.
     * @param rolId Nuevo ID del rol
     */
    public void setRolId(int rolId) {
        // Asignar el nuevo ID del rol
        this.rolId = rolId;
    }

    /**
     * Obtiene el ID del permiso asociado.
     * @return ID del permiso
     */
    public int getPermisosId() {
        // Retornar el ID del permiso
        return permisosId;
    }

    /**
     * Establece el ID del permiso asociado.
     * @param permisosId Nuevo ID del permiso
     */
    public void setPermisosId(int permisosId) {
        // Asignar el nuevo ID del permiso
        this.permisosId = permisosId;
    }
}
