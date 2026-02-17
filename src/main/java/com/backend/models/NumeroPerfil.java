package com.backend.models;

public class NumeroPerfil {
    private int idNumeros;
    private String numero;
    private int perfilId;

    // public NumeroPerfil() {
    // }

    public NumeroPerfil(int idNumeros, String numero, int perfilId) {
        this.idNumeros = idNumeros;
        this.numero = numero;
        this.perfilId = perfilId;
    }

    public NumeroPerfil(String numero, int perfilId) {
        this.numero = numero;
        this.perfilId = perfilId;
    }

    // Getters y Setters
    public int getIdNumeros() {
        return idNumeros;
    }

    public void setIdNumeros(int idNumeros) {
        this.idNumeros = idNumeros;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getPerfilId() {
        return perfilId;
    }

    public void setPerfilId(int perfilId) {
        this.perfilId = perfilId;
    }
}
