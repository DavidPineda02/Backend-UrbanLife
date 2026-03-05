package com.backend.models;

/**
 * Modelo que representa un rol en el sistema de autenticación.
 * Los roles definen los niveles de acceso y permisos que tienen los usuarios.
 */
public class Rol {
    /** Identificador único del rol en la base de datos */
    private int idRoles;
    /** Nombre del rol (ej: ADMIN, EMPLEADO, SUPER_ADMIN) */
    private String nombre;
    /** Descripción detallada del rol y sus responsabilidades */
    private String descripcion;

    /**
     * Constructor para crear un rol con ID existente.
     * @param idRoles ID del rol
     * @param nombre Nombre del rol
     * @param descripcion Descripción del rol
     */
    public Rol(int idRoles, String nombre, String descripcion) {
        this.idRoles = idRoles;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Constructor para crear un nuevo rol (sin ID).
     * @param nombre Nombre del rol
     * @param descripcion Descripción del rol
     */
    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdRoles() {
        return idRoles;
    }

    public void setIdRoles(int idRoles) {
        this.idRoles = idRoles;
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
