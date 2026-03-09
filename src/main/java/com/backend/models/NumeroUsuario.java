package com.backend.models;

public class NumeroUsuario {
    private int idNumero;
    private String numero;
    private int usuarioId;

    public NumeroUsuario(int idNumero, String numero, int usuarioId) {
        this.idNumero = idNumero;
        this.numero = numero;
        this.usuarioId = usuarioId;
    }

    public NumeroUsuario(String numero, int usuarioId) {
        this.numero = numero;
        this.usuarioId = usuarioId;
    }

    public int getIdNumero() {
        return idNumero;
    }

    public void setIdNumero(int idNumero) {
        this.idNumero = idNumero;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
}
