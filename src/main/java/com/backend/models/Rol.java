// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un rol del sistema de autorización.
 * Mapea directamente la tabla Roles de la base de datos.
 * Los roles definen los niveles de acceso: SUPER_ADMIN, ADMIN, EMPLEADO.
 */
public class Rol {

    // Identificador único del rol en la base de datos (PK)
    private int idRoles;
    // Nombre del rol (ej: SUPER_ADMIN, ADMIN, EMPLEADO)
    private String nombre;
    // Descripción detallada del rol y sus responsabilidades
    private String descripcion;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idRoles ID del rol en la BD
     * @param nombre Nombre del rol
     * @param descripcion Descripción del rol
     */
    public Rol(int idRoles, String nombre, String descripcion) {
        // Asignar el ID del rol
        this.idRoles = idRoles;
        // Asignar el nombre del rol
        this.nombre = nombre;
        // Asignar la descripción del rol
        this.descripcion = descripcion;
    }

    /**
     * Constructor sin ID (usado al crear un nuevo rol antes de persistir).
     * @param nombre Nombre del rol
     * @param descripcion Descripción del rol
     */
    public Rol(String nombre, String descripcion) {
        // Asignar el nombre del rol
        this.nombre = nombre;
        // Asignar la descripción del rol
        this.descripcion = descripcion;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del rol.
     * @return ID del rol
     */
    public int getIdRoles() {
        // Retornar el ID del rol
        return idRoles;
    }

    /**
     * Establece el ID del rol (usado tras recuperar la clave generada en INSERT).
     * @param idRoles ID a asignar
     */
    public void setIdRoles(int idRoles) {
        // Asignar el ID del rol
        this.idRoles = idRoles;
    }

    /**
     * Retorna el nombre del rol.
     * @return Nombre del rol
     */
    public String getNombre() {
        // Retornar el nombre del rol
        return nombre;
    }

    /**
     * Establece el nombre del rol.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre del rol
        this.nombre = nombre;
    }

    /**
     * Retorna la descripción del rol.
     * @return Descripción del rol
     */
    public String getDescripcion() {
        // Retornar la descripción del rol
        return descripcion;
    }

    /**
     * Establece la descripción del rol.
     * @param descripcion Descripción a asignar
     */
    public void setDescripcion(String descripcion) {
        // Asignar la descripción del rol
        this.descripcion = descripcion;
    }
}
