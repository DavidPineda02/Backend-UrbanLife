package com.backend.models;

/**
 * Modelo que representa un permiso en el sistema de autenticación.
 * Los permisos definen acciones específicas que pueden realizar los usuarios.
 */
public class Permiso {
    /** Identificador único del permiso en la base de datos */
    private int idPermisos; 
    /** Nombre del permiso (ej: READ_USERS, CREATE_PRODUCTS, DELETE_SALES) */
    private String nombre;
    /** Descripción detallada del permiso y su propósito */
    private String descripcion;

    /**
     * Constructor para crear un permiso con ID existente.
     * @param idPermisos ID del permiso
     * @param nombre Nombre del permiso
     * @param descripcion Descripción del permiso
     */
    public Permiso(int idPermisos, String nombre, String descripcion) {
        this.idPermisos = idPermisos;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Constructor para crear un nuevo permiso (sin ID).
     * @param nombre Nombre del permiso
     * @param descripcion Descripción del permiso
     */
    public Permiso(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdPermisos() {
        return idPermisos;
    }

    public void setIdPermisos(int idPermisos) {
        this.idPermisos = idPermisos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
