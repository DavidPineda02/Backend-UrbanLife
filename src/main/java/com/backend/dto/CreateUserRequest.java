// Paquete de Data Transfer Objects
package com.backend.dto;

/**
 * DTO (Data Transfer Object) que representa los datos necesarios para crear un nuevo usuario.
 * Gson lo usa para deserializar el JSON del body de POST /api/users.
 * Facilita la transferencia de datos para creación de usuarios.
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
     * Permite que Gson cree instancias sin parámetros.
     */
    public CreateUserRequest() {}

    /**
     * Verifica que los tres campos obligatorios estén presentes y no estén vacíos.
     * Realiza validación básica de los campos requeridos.
     * @return true si todos los campos son válidos, false si alguno es inválido
     */
    public boolean isValid() {
        // nombre no puede ser nulo ni solo espacios
        return nombre != null && !nombre.isBlank()
                // correo no puede ser nulo ni solo espacios
                && correo != null && !correo.isBlank()
                // contraseña no puede ser nula ni vacía
                && contrasena != null && !contrasena.isBlank();
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
