package com.backend.dto; // Paquete de Data Transfer Objects

/**
 * DTO (Data Transfer Object) que representa los datos necesarios para crear un nuevo usuario.
 * Gson lo usa para deserializar el JSON del body de POST /api/users.
 * Facilita la transferencia de datos para creación de usuarios.
 */
public class CreateUserRequest {

    /** Nombre completo del nuevo usuario */
    private String nombre; // Campo para nombre del usuario
    /** Correo electrónico (único en el sistema) */
    private String correo; // Campo para correo del usuario
    /** Contraseña en texto plano (se hashea antes de guardar) */
    private String contrasena; // Campo para contraseña del usuario

    /**
     * Constructor vacío requerido por Gson para la deserialización del JSON.
     * Permite que Gson cree instancias sin parámetros.
     */
    public CreateUserRequest() {} // Constructor por defecto para Gson

    /**
     * Verifica que los tres campos obligatorios estén presentes y no estén vacíos.
     * Realiza validación básica de los campos requeridos.
     * @return true si todos los campos son válidos, false si alguno es inválido
     */
    public boolean isValid() { // Método de validación
        return nombre != null && !nombre.isBlank()       // nombre no puede ser nulo ni solo espacios
                && correo != null && !correo.isBlank()   // correo no puede ser nulo ni solo espacios
                && contrasena != null && !contrasena.isBlank(); // contraseña no puede ser nula ni vacía
    }

    /** @return Nombre del usuario */
    public String getNombre() { return nombre; } // Getter para nombre
    /** @param nombre Nombre a establecer */
    public void setNombre(String nombre) { this.nombre = nombre; } // Setter para nombre

    /** @return Correo electrónico del usuario */
    public String getCorreo() { return correo; } // Getter para correo
    /** @param correo Correo a establecer */
    public void setCorreo(String correo) { this.correo = correo; } // Setter para correo

    /** @return Contraseña del usuario */
    public String getContrasena() { return contrasena; } // Getter para contraseña
    /** @param contrasena Contraseña a establecer */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; } // Setter para contraseña
}
