package com.backend.dto;

// DTO que representa los datos del body para el endpoint POST /api/auth/login
// Gson lo deserializa desde el JSON recibido
public class LoginRequest {

    // Correo electronico del usuario que intenta iniciar sesion
    private String correo;
    // Contrasena en texto plano (se compara con el hash almacenado en BD)
    private String contrasena;

    // Constructor vacio requerido por Gson para la deserializacion
    public LoginRequest() {}

    // Constructor con ambos campos para crear instancias de forma directa
    public LoginRequest(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    // Verifica que correo y contrasena no sean nulos ni contengan solo espacios
    public boolean isValid() {
        return correo != null && !correo.trim().isEmpty()
                && contrasena != null && !contrasena.trim().isEmpty();
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
