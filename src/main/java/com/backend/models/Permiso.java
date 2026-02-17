package com.backend.models;

public class Permiso {
    private int idPermisos; 
    private String nombre;
    private String descripcion;

    // public Permiso() {
    // }

    public Permiso(int idPermisos, String nombre, String descripcion) {
        this.idPermisos = idPermisos;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

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
