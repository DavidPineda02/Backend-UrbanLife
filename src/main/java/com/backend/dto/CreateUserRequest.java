package com.backend.dto;

public class CreateUserRequest {

    private String nombre;
    private String correo;
    private String contrasena;

    public CreateUserRequest() {}

    public boolean isValid() {
        return nombre != null && !nombre.isBlank()
                && correo != null && !correo.isBlank()
                && contrasena != null && !contrasena.isBlank();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
