// Paquete de Data Transfer Objects
package com.backend.dto;

/**
 * DTO que representa la respuesta del endpoint de login (exitosa o fallida).
 * Se puede serializar a JSON con Gson para enviarlo al cliente.
 * Estandariza las respuestas de autenticación del sistema.
 */
public class LoginResponse {

    /** Indica si el login fue exitoso (true) o falló (false) */
    private boolean success;
    /** Mensaje descriptivo del resultado (ej: "Login exitoso" o "Credenciales inválidas") */
    private String message;
    /** JWT generado para el usuario autenticado (null en caso de error) */
    private String token;
    /** Nombre del usuario autenticado */
    private String nombre;
    /** Apellido del usuario autenticado */
    private String apellido;
    /** Correo del usuario autenticado */
    private String correo;
    /** Rol del usuario en el sistema (SUPER_ADMIN, ADMIN, EMPLEADO) */
    private String rol;

    /**
     * Constructor vacío para instanciación sin argumentos.
     * Permite creación gradual de objetos con setters.
     */
    public LoginResponse() {}

    /**
     * Constructor para respuestas de error (solo necesita success y message).
     * Simplifica la creación de respuestas de error.
     * @param success Resultado del login
     * @param message Mensaje descriptivo
     */
    public LoginResponse(boolean success, String message) {
        // Asignar resultado
        this.success = success;
        // Asignar mensaje
        this.message = message;
    }

    /**
     * Constructor completo para respuestas exitosas con todos los datos del usuario.
     * Facilita la creación de respuestas de login exitoso.
     * @param success Resultado del login
     * @param message Mensaje descriptivo
     * @param token JWT generado
     * @param nombre Nombre del usuario
     * @param correo Correo del usuario
     * @param rol Rol del usuario
     */
    public LoginResponse(boolean success, String message, String token, String nombre, String apellido, String correo, String rol) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
    }

    /** @return true si el login fue exitoso */
    public boolean isSuccess() { return success; }
    /** @param success Establecer resultado del login */
    public void setSuccess(boolean success) { this.success = success; }

    /** @return Mensaje descriptivo del resultado */
    public String getMessage() { return message; }
    /** @param message Establecer mensaje descriptivo */
    public void setMessage(String message) { this.message = message; }

    /** @return JWT generado para el usuario */
    public String getToken() { return token; }
    /** @param token Establecer JWT */
    public void setToken(String token) { this.token = token; }

    /** @return Nombre del usuario autenticado */
    public String getNombre() { return nombre; }
    /** @param nombre Establecer nombre del usuario */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return Apellido del usuario autenticado */
    public String getApellido() { return apellido; }
    /** @param apellido Establecer apellido del usuario */
    public void setApellido(String apellido) { this.apellido = apellido; }

    /** @return Correo del usuario autenticado */
    public String getCorreo() { return correo; }
    /** @param correo Establecer correo del usuario */
    public void setCorreo(String correo) { this.correo = correo; }

    /** @return Rol del usuario en el sistema */
    public String getRol() { return rol; }
    /** @param rol Establecer rol del usuario */
    public void setRol(String rol) { this.rol = rol; }
}
