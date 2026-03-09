package com.backend.models;

public class CorreoUsuario {
    private int idCorreo;
    private String correo;
    private int usuarioId;

    public CorreoUsuario(int idCorreo, String correo, int usuarioId) {
        this.idCorreo = idCorreo;
        this.correo = correo;
        this.usuarioId = usuarioId;
    }

    public CorreoUsuario(String correo, int usuarioId) {
        this.correo = correo;
        this.usuarioId = usuarioId;
    }

    public int getIdCorreo() {
        return idCorreo;
    }

    public void setIdCorreo(int idCorreo) {
        this.idCorreo = idCorreo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
}
