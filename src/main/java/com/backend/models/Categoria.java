// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa una categoría de productos en el sistema.
 * Mapea directamente la tabla Categoria de la base de datos.
 * Las categorías pueden ser activadas o desactivadas sin eliminarse.
 */
public class Categoria {

    // Identificador único de la categoría (PK en la base de datos)
    private int idCategoria;
    // Nombre de la categoría (obligatorio, único)
    private String nombre;
    // Descripción opcional de la categoría
    private String descripcion;
    // Estado activo (true) o inactivo (false) de la categoría
    private boolean estado;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idCategoria ID de la categoría
     * @param nombre Nombre de la categoría
     * @param descripcion Descripción de la categoría
     * @param estado Estado activo/inactivo
     */
    public Categoria(int idCategoria, String nombre, String descripcion, boolean estado) {
        // Asignar el ID de la categoría
        this.idCategoria = idCategoria;
        // Asignar el nombre de la categoría
        this.nombre = nombre;
        // Asignar la descripción de la categoría
        this.descripcion = descripcion;
        // Asignar el estado de la categoría
        this.estado = estado;
    }

    /**
     * Constructor sin ID (usado al crear una nueva categoría antes de persistir).
     * @param nombre Nombre de la categoría
     * @param descripcion Descripción de la categoría
     * @param estado Estado activo/inactivo
     */
    public Categoria(String nombre, String descripcion, boolean estado) {
        // Asignar el nombre de la categoría
        this.nombre = nombre;
        // Asignar la descripción de la categoría
        this.descripcion = descripcion;
        // Asignar el estado de la categoría
        this.estado = estado;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID de la categoría.
     * @return ID de la categoría
     */
    public int getIdCategoria() {
        // Retornar el ID de la categoría
        return idCategoria;
    }

    /**
     * Establece el ID de la categoría (usado al recuperar la clave generada tras INSERT).
     * @param idCategoria ID a asignar
     */
    public void setIdCategoria(int idCategoria) {
        // Asignar el ID de la categoría
        this.idCategoria = idCategoria;
    }

    /**
     * Retorna el nombre de la categoría.
     * @return Nombre de la categoría
     */
    public String getNombre() {
        // Retornar el nombre de la categoría
        return nombre;
    }

    /**
     * Establece el nombre de la categoría.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre de la categoría
        this.nombre = nombre;
    }

    /**
     * Retorna la descripción de la categoría.
     * @return Descripción de la categoría
     */
    public String getDescripcion() {
        // Retornar la descripción de la categoría
        return descripcion;
    }

    /**
     * Establece la descripción de la categoría.
     * @param descripcion Descripción a asignar
     */
    public void setDescripcion(String descripcion) {
        // Asignar la descripción de la categoría
        this.descripcion = descripcion;
    }

    /**
     * Retorna el estado activo/inactivo de la categoría.
     * @return true si está activa, false si está inactiva
     */
    public boolean isEstado() {
        // Retornar el estado de la categoría
        return estado;
    }

    /**
     * Establece el estado activo/inactivo de la categoría.
     * @param estado true para activar, false para desactivar
     */
    public void setEstado(boolean estado) {
        // Asignar el estado de la categoría
        this.estado = estado;
    }
}
