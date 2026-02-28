package com.backend.models;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String documento;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private boolean estado;

    public Cliente(int idCliente, String nombre, String documento, String correo, String telefono, String direccion, String ciudad, boolean estado) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = estado;
    }

    public Cliente(String nombre, String documento, String correo, String telefono, String direccion, String ciudad) {
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = true;
    }

    // Getters y Setters
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
