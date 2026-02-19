package com.backend.dto;

public class LoginRequest {

    private String correo;
    private String contrasena;

    public LoginRequest() {}

    public LoginRequest(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public boolean isValid() {
        return correo != null && !correo.trim().isEmpty()
                && contrasena != null && !contrasena.trim().isEmpty();
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
