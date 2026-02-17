package com.backend.models;

public class CorreoPerfil {
    private int idCorreos;
    private String correo;
    private int perfilId;

    // public CorreoPerfil() {
    // }

    public CorreoPerfil(int idCorreos, String correo, int perfilId) {
        this.idCorreos = idCorreos;
        this.correo = correo;
        this.perfilId = perfilId;
    }

    public CorreoPerfil(String correo, int perfilId) {
        this.correo = correo;
        this.perfilId = perfilId;
    }

    // Getters y Setters
    public int getIdCorreos() {
        return idCorreos;
    }

    public void setIdCorreos(int idCorreos) {
        this.idCorreos = idCorreos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getPerfilId() {
        return perfilId;
    }

    public void setPerfilId(int perfilId) {
        this.perfilId = perfilId;
    }
}
