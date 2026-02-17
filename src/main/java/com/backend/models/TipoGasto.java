package com.backend.models;

public class TipoGasto {
    private int idTipoGasto;
    private String nombre;
    private String descripcion;

    // public TipoGasto() {
    // }

    public TipoGasto(int idTipoGasto, String nombre, String descripcion) {
        this.idTipoGasto = idTipoGasto;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public TipoGasto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdTipoGasto() {
        return idTipoGasto;
    }

    public void setIdTipoGasto(int idTipoGasto) {
        this.idTipoGasto = idTipoGasto;
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
