// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

// Para representar la fecha y hora de expiración y creación del token
import java.time.LocalDateTime;

/**
 * Entidad que representa un token de recuperación de contraseña.
 * Mapea directamente la tabla Token_Recuperacion de la base de datos.
 * El token se almacena hasheado (SHA-256) para mayor seguridad.
 */
public class TokenRecuperacion {

    // Identificador único del token en la base de datos (PK)
    private int idToken;
    // ID del usuario que solicitó la recuperación de contraseña
    private int usuarioId;
    // Hash SHA-256 del token UUID (nunca se almacena el token en texto plano)
    private String token;
    // Fecha y hora límite hasta la cual el token es válido (1 hora desde su creación)
    private LocalDateTime fechaExpiracion;
    // Indica si el token ya fue utilizado (true = usado, false = disponible)
    private boolean usado;
    // Fecha y hora en que fue generado el token
    private LocalDateTime fechaCreacion;

    /**
     * Constructor completo con todos los campos del token.
     * Se usa al mapear un registro de la base de datos al objeto.
     * @param idToken ID del token en la BD
     * @param usuarioId ID del usuario propietario del token
     * @param token Hash SHA-256 del token UUID
     * @param fechaExpiracion Fecha de expiración del token
     * @param usado true si ya fue usado, false si sigue vigente
     * @param fechaCreacion Fecha en que se generó el token
     */
    public TokenRecuperacion(int idToken, int usuarioId, String token,
            LocalDateTime fechaExpiracion, boolean usado, LocalDateTime fechaCreacion) {
        // Asignar el ID del token
        this.idToken = idToken;
        // Asignar el ID del usuario propietario
        this.usuarioId = usuarioId;
        // Asignar el hash SHA-256 del token
        this.token = token;
        // Asignar la fecha de expiración
        this.fechaExpiracion = fechaExpiracion;
        // Asignar el estado de uso del token
        this.usado = usado;
        // Asignar la fecha de creación
        this.fechaCreacion = fechaCreacion;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del token.
     * @return ID del token
     */
    public int getIdToken() {
        // Retornar el ID del token
        return idToken;
    }

    /**
     * Establece el ID del token.
     * @param idToken ID a asignar
     */
    public void setIdToken(int idToken) {
        // Asignar el ID del token
        this.idToken = idToken;
    }

    /**
     * Retorna el ID del usuario propietario del token.
     * @return ID del usuario
     */
    public int getUsuarioId() {
        // Retornar el ID del usuario
        return usuarioId;
    }

    /**
     * Establece el ID del usuario propietario.
     * @param usuarioId ID a asignar
     */
    public void setUsuarioId(int usuarioId) {
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    /**
     * Retorna el hash SHA-256 del token almacenado en la BD.
     * @return Hash del token
     */
    public String getToken() {
        // Retornar el hash del token
        return token;
    }

    /**
     * Establece el hash del token.
     * @param token Hash a asignar
     */
    public void setToken(String token) {
        // Asignar el hash del token
        this.token = token;
    }

    /**
     * Retorna la fecha y hora de expiración del token.
     * @return Fecha de expiración
     */
    public LocalDateTime getFechaExpiracion() {
        // Retornar la fecha de expiración del token
        return fechaExpiracion;
    }

    /**
     * Establece la fecha de expiración del token.
     * @param fechaExpiracion Fecha a asignar
     */
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        // Asignar la fecha de expiración del token
        this.fechaExpiracion = fechaExpiracion;
    }

    /**
     * Retorna si el token ya fue utilizado.
     * @return true si fue usado, false si está disponible
     */
    public boolean isUsado() {
        // Retornar el estado de uso del token
        return usado;
    }

    /**
     * Establece el estado de uso del token.
     * @param usado true para marcarlo como usado, false si está disponible
     */
    public void setUsado(boolean usado) {
        // Asignar el estado de uso del token
        this.usado = usado;
    }

    /**
     * Retorna la fecha y hora de creación del token.
     * @return Fecha de creación
     */
    public LocalDateTime getFechaCreacion() {
        // Retornar la fecha de creación del token
        return fechaCreacion;
    }

    /**
     * Establece la fecha de creación del token.
     * @param fechaCreacion Fecha a asignar
     */
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        // Asignar la fecha de creación del token
        this.fechaCreacion = fechaCreacion;
    }
}
