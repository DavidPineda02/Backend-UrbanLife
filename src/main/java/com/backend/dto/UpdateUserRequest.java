package com.backend.dto;

public class UpdateUserRequest {

    private String nombre;
    private String correo;
    private String estado;
    private String contrasena;

    public UpdateUserRequest() {}

    public boolean isValid() {
        return (nombre != null && !nombre.trim().isEmpty())
                || (correo != null && !correo.trim().isEmpty());
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
