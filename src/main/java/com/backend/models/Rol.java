package com.backend.models;

public class Rol {
    private int idRoles;
    private String nombre;
    private String descripcion;

    // public Rol() {
    // }

    public Rol(int idRoles, String nombre, String descripcion) {
        this.idRoles = idRoles;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

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
