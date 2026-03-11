// Paquete de modelos de datos de la aplicación
package com.backend.models;

/**
 * Modelo que representa un permiso en el sistema de autenticación.
 * Los permisos definen acciones específicas que pueden realizar los usuarios
 * según el rol que tengan asignado (ej: READ_USERS, CREATE_PRODUCTS).
 */
public class Permiso {

    /** Identificador único del permiso en la base de datos */
    private int idPermisos;
    /** Nombre del permiso (ej: READ_USERS, CREATE_PRODUCTS, DELETE_SALES) */
    private String nombre;
    /** Descripción detallada del permiso y su propósito */
    private String descripcion;

    /**
     * Constructor para crear un permiso con ID existente (lectura desde BD).
     * @param idPermisos ID del permiso en la base de datos
     * @param nombre Nombre identificador del permiso
     * @param descripcion Descripción detallada del permiso
     */
    public Permiso(int idPermisos, String nombre, String descripcion) {
        // Asignar el ID del permiso
        this.idPermisos = idPermisos;
        // Asignar el nombre del permiso
        this.nombre = nombre;
        // Asignar la descripción del permiso
        this.descripcion = descripcion;
    }

    /**
     * Constructor para crear un nuevo permiso sin ID (inserción en BD).
     * @param nombre Nombre identificador del permiso
     * @param descripcion Descripción detallada del permiso
     */
    public Permiso(String nombre, String descripcion) {
        // Asignar el nombre del permiso
        this.nombre = nombre;
        // Asignar la descripción del permiso
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el ID del permiso.
     * @return ID del permiso en la base de datos
     */
    public int getIdPermisos() {
        // Retornar el ID del permiso
        return idPermisos;
    }

    /**
     * Establece el ID del permiso.
     * @param idPermisos Nuevo ID del permiso
     */
    public void setIdPermisos(int idPermisos) {
        // Asignar el nuevo ID del permiso
        this.idPermisos = idPermisos;
    }

    /**
     * Obtiene el nombre del permiso.
     * @return Nombre identificador del permiso
     */
    public String getNombre() {
        // Retornar el nombre del permiso
        return nombre;
    }

    /**
     * Establece el nombre del permiso.
     * @param nombre Nuevo nombre del permiso
     */
    public void setNombre(String nombre) {
        // Asignar el nuevo nombre del permiso
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripción del permiso.
     * @return Descripción detallada del permiso
     */
    public String getDescripcion() {
        // Retornar la descripción del permiso
        return descripcion;
    }

    /**
     * Establece la descripción del permiso.
     * @param descripcion Nueva descripción del permiso
     */
    public void setDescripcion(String descripcion) {
        // Asignar la nueva descripción del permiso
        this.descripcion = descripcion;
    }
}
