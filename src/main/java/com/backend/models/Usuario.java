// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un usuario del sistema.
 * Mapea directamente la tabla Usuarios de la base de datos.
 * Soporta autenticación con correo/contraseña y con Google OAuth2.
 */
public class Usuario {

    // Identificador único del usuario (PK en la base de datos)
    private int idUsuario;
    // Nombre del usuario
    private String nombre;
    // Apellido del usuario
    private String apellido;
    // Correo electrónico del usuario (usado para login, único en la BD)
    private String correo;
    // Contraseña del usuario almacenada como hash BCrypt (null si usa Google)
    private String contrasena;
    // Estado activo (true) o inactivo (false) del usuario
    private boolean estado;
    // ID de Google para autenticación OAuth2 (null si usa correo/contraseña)
    private String googleId;

    /**
     * Constructor por defecto requerido para construir el objeto manualmente.
     * Se usa en createWithGoogle donde se asignan los campos uno a uno.
     */
    public Usuario() {
    }

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idUsuario ID del usuario en la BD
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param correo Correo electrónico del usuario
     * @param contrasena Hash BCrypt de la contraseña (puede ser null para cuentas Google)
     * @param estado Estado activo/inactivo del usuario
     */
    public Usuario(int idUsuario, String nombre, String apellido, String correo, String contrasena, boolean estado) {
        // Asignar el ID del usuario
        this.idUsuario = idUsuario;
        // Asignar el nombre del usuario
        this.nombre = nombre;
        // Asignar el apellido del usuario
        this.apellido = apellido;
        // Asignar el correo del usuario
        this.correo = correo;
        // Asignar la contraseña hasheada del usuario
        this.contrasena = contrasena;
        // Asignar el estado del usuario
        this.estado = estado;
    }

    /**
     * Constructor completo con ID y Google ID.
     * Se usa cuando el usuario tiene tanto contraseña como cuenta Google vinculada.
     * @param idUsuario ID del usuario en la BD
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param correo Correo electrónico del usuario
     * @param contrasena Hash BCrypt de la contraseña
     * @param estado Estado activo/inactivo del usuario
     * @param googleId ID de Google vinculado a la cuenta
     */
    public Usuario(int idUsuario, String nombre, String apellido, String correo, String contrasena, boolean estado, String googleId) {
        // Asignar el ID del usuario
        this.idUsuario = idUsuario;
        // Asignar el nombre del usuario
        this.nombre = nombre;
        // Asignar el apellido del usuario
        this.apellido = apellido;
        // Asignar el correo del usuario
        this.correo = correo;
        // Asignar la contraseña hasheada del usuario
        this.contrasena = contrasena;
        // Asignar el estado del usuario
        this.estado = estado;
        // Asignar el ID de Google vinculado
        this.googleId = googleId;
    }

    /**
     * Constructor sin ID (usado al crear un nuevo usuario antes de persistir).
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param correo Correo electrónico del usuario
     * @param contrasena Hash BCrypt de la contraseña
     * @param estado Estado activo/inactivo del usuario
     */
    public Usuario(String nombre, String apellido, String correo, String contrasena, boolean estado) {
        // Asignar el nombre del usuario
        this.nombre = nombre;
        // Asignar el apellido del usuario
        this.apellido = apellido;
        // Asignar el correo del usuario
        this.correo = correo;
        // Asignar la contraseña hasheada del usuario
        this.contrasena = contrasena;
        // Asignar el estado del usuario
        this.estado = estado;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del usuario.
     * @return ID del usuario
     */
    public int getIdUsuario() {
        // Retornar el ID del usuario
        return idUsuario;
    }

    /**
     * Establece el ID del usuario (usado tras recuperar la clave generada en INSERT).
     * @param idUsuario ID a asignar
     */
    public void setIdUsuario(int idUsuario) {
        // Asignar el ID del usuario
        this.idUsuario = idUsuario;
    }

    /**
     * Retorna el nombre del usuario.
     * @return Nombre del usuario
     */
    public String getNombre() {
        // Retornar el nombre del usuario
        return nombre;
    }

    /**
     * Establece el nombre del usuario.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre del usuario
        this.nombre = nombre;
    }

    /**
     * Retorna el apellido del usuario.
     * @return Apellido del usuario
     */
    public String getApellido() {
        // Retornar el apellido del usuario
        return apellido;
    }

    /**
     * Establece el apellido del usuario.
     * @param apellido Apellido a asignar
     */
    public void setApellido(String apellido) {
        // Asignar el apellido del usuario
        this.apellido = apellido;
    }

    /**
     * Retorna el correo electrónico del usuario.
     * @return Correo del usuario
     */
    public String getCorreo() {
        // Retornar el correo del usuario
        return correo;
    }

    /**
     * Establece el correo electrónico del usuario.
     * @param correo Correo a asignar
     */
    public void setCorreo(String correo) {
        // Asignar el correo del usuario
        this.correo = correo;
    }

    /**
     * Retorna el hash BCrypt de la contraseña del usuario.
     * @return Hash de la contraseña (puede ser null si es cuenta Google)
     */
    public String getContrasena() {
        // Retornar la contraseña hasheada del usuario
        return contrasena;
    }

    /**
     * Establece la contraseña del usuario.
     * Se asigna null antes de enviar al cliente para no exponer el hash.
     * @param contrasena Hash BCrypt o null para ocultarla
     */
    public void setContrasena(String contrasena) {
        // Asignar la contraseña del usuario
        this.contrasena = contrasena;
    }

    /**
     * Retorna el estado activo/inactivo del usuario.
     * @return true si está activo, false si está inactivo
     */
    public boolean isEstado() {
        // Retornar el estado del usuario
        return estado;
    }

    /**
     * Establece el estado activo/inactivo del usuario.
     * @param estado true para activar, false para desactivar
     */
    public void setEstado(boolean estado) {
        // Asignar el estado del usuario
        this.estado = estado;
    }

    /**
     * Retorna el ID de Google vinculado al usuario.
     * @return Google ID o null si no tiene cuenta Google vinculada
     */
    public String getGoogleId() {
        // Retornar el ID de Google del usuario
        return googleId;
    }

    /**
     * Establece el ID de Google del usuario.
     * @param googleId ID de Google a asignar
     */
    public void setGoogleId(String googleId) {
        // Asignar el ID de Google del usuario
        this.googleId = googleId;
    }
}
