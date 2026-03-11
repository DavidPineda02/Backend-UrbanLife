// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un tipo de gasto (categoría de gasto).
 * Mapea directamente la tabla Tipo_Gasto de la base de datos.
 * Los tipos de gasto se insertan mediante seeders y sirven para clasificar gastos adicionales.
 */
public class TipoGasto {

    // Identificador único del tipo de gasto en la base de datos (PK)
    private int idTipoGasto;
    // Nombre del tipo de gasto (ej: "Servicios Públicos", "Arriendo")
    private String nombre;
    // Descripción detallada del tipo de gasto
    private String descripcion;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idTipoGasto ID del tipo de gasto en la BD
     * @param nombre Nombre del tipo de gasto
     * @param descripcion Descripción del tipo de gasto
     */
    public TipoGasto(int idTipoGasto, String nombre, String descripcion) {
        // Asignar el ID del tipo de gasto
        this.idTipoGasto = idTipoGasto;
        // Asignar el nombre del tipo de gasto
        this.nombre = nombre;
        // Asignar la descripción del tipo de gasto
        this.descripcion = descripcion;
    }

    /**
     * Constructor sin ID (usado al construir un tipo nuevo antes de persistir).
     * @param nombre Nombre del tipo de gasto
     * @param descripcion Descripción del tipo de gasto
     */
    public TipoGasto(String nombre, String descripcion) {
        // Asignar el nombre del tipo de gasto
        this.nombre = nombre;
        // Asignar la descripción del tipo de gasto
        this.descripcion = descripcion;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del tipo de gasto.
     * @return ID del tipo de gasto
     */
    public int getIdTipoGasto() {
        // Retornar el ID del tipo de gasto
        return idTipoGasto;
    }

    /**
     * Establece el ID del tipo de gasto.
     * @param idTipoGasto ID a asignar
     */
    public void setIdTipoGasto(int idTipoGasto) {
        // Asignar el ID del tipo de gasto
        this.idTipoGasto = idTipoGasto;
    }

    /**
     * Retorna el nombre del tipo de gasto.
     * @return Nombre del tipo de gasto
     */
    public String getNombre() {
        // Retornar el nombre del tipo de gasto
        return nombre;
    }

    /**
     * Establece el nombre del tipo de gasto.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre del tipo de gasto
        this.nombre = nombre;
    }

    /**
     * Retorna la descripción del tipo de gasto.
     * @return Descripción del tipo de gasto
     */
    public String getDescripcion() {
        // Retornar la descripción del tipo de gasto
        return descripcion;
    }

    /**
     * Establece la descripción del tipo de gasto.
     * @param descripcion Descripción a asignar
     */
    public void setDescripcion(String descripcion) {
        // Asignar la descripción del tipo de gasto
        this.descripcion = descripcion;
    }
}
