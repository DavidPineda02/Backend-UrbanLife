package com.backend.models;

import java.time.LocalDateTime;

/**
 * Modelo que representa un token de recuperación de contraseña.
 * Almacena tokens temporales para que los usuarios restablezcan sus contraseñas.
 */
public class TokenRecuperacion {
    /** Identificador único del token en la base de datos */
    private int idToken;
    /** ID del usuario que solicitó la recuperación */
    private int usuarioId;
    /** Token único generado para la recuperación */
    private String token;
    /** Fecha y hora de expiración del token */
    private LocalDateTime fechaExpiracion;
    /** Estado del token (true = usado, false = válido) */
    private boolean usado;
    /** Fecha y hora de creación del token */
    private LocalDateTime fechaCreacion;

    /**
     * Constructor para crear un token de recuperación con todos sus datos.
     * @param idToken ID del token
     * @param usuarioId ID del usuario
     * @param token Token único generado
     * @param fechaExpiracion Fecha de expiración del token
     * @param usado Estado del token
     * @param fechaCreacion Fecha de creación del token
     */
    public TokenRecuperacion(int idToken, int usuarioId, String token,
            LocalDateTime fechaExpiracion, boolean usado, LocalDateTime fechaCreacion) {
        this.idToken = idToken;
        this.usuarioId = usuarioId;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
        this.fechaCreacion = fechaCreacion;
    }

    /** @return ID del token */
    public int getIdToken() { return idToken; }
    /** @param idToken ID del token a establecer */
    public void setIdToken(int idToken) { this.idToken = idToken; }

    /** @return ID del usuario */
    public int getUsuarioId() { return usuarioId; }
    /** @param usuarioId ID del usuario a establecer */
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    /** @return Token de recuperación */
    public String getToken() { return token; }
    /** @param token Token a establecer */
    public void setToken(String token) { this.token = token; }

    /** @return Fecha de expiración del token */
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    /** @param fechaExpiracion Fecha de expiración a establecer */
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    /** @return true si el token ha sido usado, false si está vigente */
    public boolean isUsado() { return usado; }
    /** @param usado Estado del token a establecer */
    public void setUsado(boolean usado) { this.usado = usado; }

    /** @return Fecha de creación del token */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    /** @param fechaCreacion Fecha de creación a establecer */
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
