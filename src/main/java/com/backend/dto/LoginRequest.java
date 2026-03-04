// Paquete de Data Transfer Objects
package com.backend.dto;

/**
 * DTO que representa los datos del body para el endpoint POST /api/auth/login.
 * Gson lo deserializa desde el JSON recibido.
 * Facilita la transferencia de datos entre cliente y servidor.
 */
public class LoginRequest {

    /** Correo electrónico del usuario que intenta iniciar sesión */
    private String correo;
    /** Contraseña en texto plano (se compara con el hash almacenado en BD) */
    private String contrasena;

    /**
     * Constructor vacío requerido por Gson para la deserialización.
     * Permite que Gson cree instancias sin parámetros.
     */
    public LoginRequest() {}

    /**
     * Constructor con ambos campos para crear instancias de forma directa.
     * Útil para pruebas o creación programática.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     */
    public LoginRequest(String correo, String contrasena) {
        // Asignar correo
        this.correo = correo;
        // Asignar contraseña
        this.contrasena = contrasena;
    }

    /**
     * Verifica que correo y contraseña no sean nulos ni contengan solo espacios.
     * Realiza validación básica de los campos requeridos.
     * @return true si ambos campos son válidos, false si alguno es inválido
     */
    public boolean isValid() {
        // Validar correo no vacío
        return correo != null && !correo.trim().isEmpty()
                // Validar contraseña no vacía
                && contrasena != null && !contrasena.trim().isEmpty();
    }

    /** @return Correo electrónico del usuario */
    public String getCorreo() { return correo; }
    /** @param correo Correo electrónico a establecer */
    public void setCorreo(String correo) { this.correo = correo; }

    /** @return Contraseña del usuario */
    public String getContrasena() { return contrasena; }
    /** @param contrasena Contraseña a establecer */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
