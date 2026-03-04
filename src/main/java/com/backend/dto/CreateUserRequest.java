package com.backend.dto;

/**
 * DTO (Data Transfer Object) que representa los datos necesarios para crear un nuevo usuario.
 * Gson lo usa para deserializar el JSON del body de POST /api/users.
 */
public class CreateUserRequest {

    /** Nombre completo del nuevo usuario */
    private String nombre;
    /** Correo electrónico (único en el sistema) */
    private String correo;
    /** Contraseña en texto plano (se hashea antes de guardar) */
    private String contrasena;

    /**
     * Constructor vacío requerido por Gson para la deserialización del JSON.
     */
    public CreateUserRequest() {}

    /**
     * Verifica que los tres campos obligatorios estén presentes y no estén vacíos.
     * @return true si todos los campos son válidos, false si alguno es inválido
     */
    public boolean isValid() {
        return nombre != null && !nombre.isBlank()       // nombre no puede ser nulo ni solo espacios
                && correo != null && !correo.isBlank()   // correo no puede ser nulo ni solo espacios
                && contrasena != null && !contrasena.isBlank(); // contraseña no puede ser nula ni vacía
    }

    /** @return Nombre del usuario */
    public String getNombre() { return nombre; }
    /** @param nombre Nombre a establecer */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return Correo electrónico del usuario */
    public String getCorreo() { return correo; }
    /** @param correo Correo a establecer */
    public void setCorreo(String correo) { this.correo = correo; }

    /** @return Contraseña del usuario */
    public String getContrasena() { return contrasena; }
    /** @param contrasena Contraseña a establecer */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
