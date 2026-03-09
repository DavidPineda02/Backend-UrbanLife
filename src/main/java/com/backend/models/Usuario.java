package com.backend.models;

/**
 * Modelo que representa un usuario en el sistema.
 * Contiene información básica del usuario incluyendo datos de autenticación
 * y soporte para login con Google.
 */
public class Usuario {
    /** Identificador único del usuario en la base de datos */
    private int idUsuario;
    /** Nombre del usuario */
    private String nombre;
    /** Apellido del usuario */
    private String apellido;
    /** Correo electrónico del usuario (usado para login) */
    private String correo;
    /** Contraseña del usuario (almacenada encriptada) */
    private String contrasena;
    /** Estado del usuario (true = activo, false = inactivo) */
    private boolean estado;
    /** ID de Google para autenticación con OAuth2 (opcional) */
    private String googleId;

    /**
     * Constructor por defecto.
     */
    public Usuario() {
    }

    /**
     * Constructor para crear un usuario con ID existente.
     * @param idUsuario ID del usuario
     * @param nombre Nombre del usuario
     * @param correo Correo electrónico
     * @param contrasena Contraseña (debe venir encriptada)
     * @param estado Estado del usuario
     */
    public Usuario(int idUsuario, String nombre, String apellido, String correo, String contrasena, boolean estado) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    /**
     * Constructor para crear un usuario con ID y Google ID.
     */
    public Usuario(int idUsuario, String nombre, String apellido, String correo, String contrasena, boolean estado, String googleId) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = estado;
        this.googleId = googleId;
    }

    /**
     * Constructor para crear un nuevo usuario (sin ID).
     */
    public Usuario(String nombre, String apellido, String correo, String contrasena, boolean estado) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
}
