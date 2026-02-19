package com.backend.dto;

public class CreateUserRequest {

    private String nombre;
    private String correo;
    private String contrasena;
    private String estado;

    public CreateUserRequest() {}

    public boolean isValid() {
        return nombre != null && !nombre.trim().isEmpty()
                && correo != null && !correo.trim().isEmpty()
                && contrasena != null && !contrasena.trim().isEmpty();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
