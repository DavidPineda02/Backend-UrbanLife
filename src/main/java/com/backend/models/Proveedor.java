package com.backend.models;

public class Proveedor {
    private int idProveedor;
    private String nombre;
    private String razonSocial;
    private String nit;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private boolean estado;

    public Proveedor(int idProveedor, String nombre, String razonSocial, String nit, String correo, String telefono, String direccion, String ciudad, boolean estado) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.nit = nit;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = estado;
    }

    public Proveedor(String nombre, String razonSocial, String nit, String correo, String telefono, String direccion, String ciudad) {
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.nit = nit;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = true;
    }

    // Getters y Setters
    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
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
