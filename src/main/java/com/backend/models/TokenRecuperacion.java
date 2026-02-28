package com.backend.models;

import java.time.LocalDateTime;

public class TokenRecuperacion {
    private int idToken;
    private int usuarioId;
    private String token;
    private LocalDateTime fechaExpiracion;
    private boolean usado;
    private LocalDateTime fechaCreacion;

    public TokenRecuperacion(int idToken, int usuarioId, String token,
            LocalDateTime fechaExpiracion, boolean usado, LocalDateTime fechaCreacion) {
        this.idToken = idToken;
        this.usuarioId = usuarioId;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdToken() { return idToken; }
    public void setIdToken(int idToken) { this.idToken = idToken; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
