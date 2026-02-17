package com.backend.models;

public class Perfil {
    private int idPerfil;
    private String nombreEmpresa;
    private String direccion;
    private String ciudad;
    private int usuarioId;

    // public Perfil() {
    // }

    public Perfil(int idPerfil, String nombreEmpresa, String direccion, String ciudad, int usuarioId) {
        this.idPerfil = idPerfil;
        this.nombreEmpresa = nombreEmpresa;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.usuarioId = usuarioId;
    }

    public Perfil(String nombreEmpresa, String direccion, String ciudad, int usuarioId) {
        this.nombreEmpresa = nombreEmpresa;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
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

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
}
