package com.backend.dto;

public class LoginResponse {

    private boolean success;
    private String message;
    private String token;
    private String nombre;
    private String correo;
    private String rol;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, String token, String nombre, String correo, String rol) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
