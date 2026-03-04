package com.backend.dto;

// DTO que representa la respuesta del endpoint de login (exitosa o fallida)
// Se puede serializar a JSON con Gson para enviarlo al cliente
public class LoginResponse {

    // Indica si el login fue exitoso (true) o fallo (false)
    private boolean success;
    // Mensaje descriptivo del resultado (ej: "Login exitoso" o "Credenciales invalidas")
    private String message;
    // JWT generado para el usuario autenticado (null en caso de error)
    private String token;
    // Nombre del usuario autenticado
    private String nombre;
    // Correo del usuario autenticado
    private String correo;
    // Rol del usuario en el sistema (SUPER_ADMIN, ADMIN, EMPLEADO)
    private String rol;

    // Constructor vacio para instanciacion sin argumentos
    public LoginResponse() {}

    // Constructor para respuestas de error (solo necesita success y message)
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Constructor completo para respuestas exitosas con todos los datos del usuario
    public LoginResponse(boolean success, String message, String token, String nombre, String correo, String rol) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
