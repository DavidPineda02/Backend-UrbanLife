package com.backend.dto; // Paquete de Data Transfer Objects

/**
 * DTO que representa la respuesta del endpoint de login (exitosa o fallida).
 * Se puede serializar a JSON con Gson para enviarlo al cliente.
 * Estandariza las respuestas de autenticación del sistema.
 */
public class LoginResponse {

    /** Indica si el login fue exitoso (true) o falló (false) */
    private boolean success; // Campo para resultado del login
    /** Mensaje descriptivo del resultado (ej: "Login exitoso" o "Credenciales inválidas") */
    private String message; // Campo para mensaje descriptivo
    /** JWT generado para el usuario autenticado (null en caso de error) */
    private String token; // Campo para token JWT
    /** Nombre del usuario autenticado */
    private String nombre; // Campo para nombre del usuario
    /** Correo del usuario autenticado */
    private String correo; // Campo para correo del usuario
    /** Rol del usuario en el sistema (SUPER_ADMIN, ADMIN, EMPLEADO) */
    private String rol; // Campo para rol del usuario

    /**
     * Constructor vacío para instanciación sin argumentos.
     * Permite creación gradual de objetos con setters.
     */
    public LoginResponse() {} // Constructor por defecto

    /**
     * Constructor para respuestas de error (solo necesita success y message).
     * Simplifica la creación de respuestas de error.
     * @param success Resultado del login
     * @param message Mensaje descriptivo
     */
    public LoginResponse(boolean success, String message) { // Constructor para errores
        this.success = success; // Asignar resultado
        this.message = message; // Asignar mensaje
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
    public LoginResponse(boolean success, String message, String token, String nombre, String correo, String rol) { // Constructor completo
        this.success = success; // Asignar resultado
        this.message = message; // Asignar mensaje
        this.token = token; // Asignar token
        this.nombre = nombre; // Asignar nombre
        this.correo = correo; // Asignar correo
        this.rol = rol; // Asignar rol
    }

    /** @return true si el login fue exitoso */
    public boolean isSuccess() { return success; } // Getter para success
    /** @param success Establecer resultado del login */
    public void setSuccess(boolean success) { this.success = success; } // Setter para success

    /** @return Mensaje descriptivo del resultado */
    public String getMessage() { return message; } // Getter para message
    /** @param message Establecer mensaje descriptivo */
    public void setMessage(String message) { this.message = message; } // Setter para message

    /** @return JWT generado para el usuario */
    public String getToken() { return token; } // Getter para token
    /** @param token Establecer JWT */
    public void setToken(String token) { this.token = token; } // Setter para token

    /** @return Nombre del usuario autenticado */
    public String getNombre() { return nombre; } // Getter para nombre
    /** @param nombre Establecer nombre del usuario */
    public void setNombre(String nombre) { this.nombre = nombre; } // Setter para nombre

    /** @return Correo del usuario autenticado */
    public String getCorreo() { return correo; } // Getter para correo
    /** @param correo Establecer correo del usuario */
    public void setCorreo(String correo) { this.correo = correo; } // Setter para correo

    /** @return Rol del usuario en el sistema */
    public String getRol() { return rol; } // Getter para rol
    /** @param rol Establecer rol del usuario */
    public void setRol(String rol) { this.rol = rol; } // Setter para rol
}
