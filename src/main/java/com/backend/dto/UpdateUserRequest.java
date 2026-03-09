// Paquete de Data Transfer Objects
package com.backend.dto;

/**
 * DTO que representa los datos opcionales para actualizar un usuario (PUT o PATCH).
 * Todos los campos son opcionales: si vienen null, no se actualizan.
 * Facilita actualizaciones parciales de usuarios.
 */
public class UpdateUserRequest {

    /** Nuevo nombre del usuario (opcional) */
    private String nombre;
    /** Nuevo apellido del usuario (opcional) */
    private String apellido;
    /** Nuevo correo electrónico (opcional, debe ser único) */
    private String correo;
    /** Nuevo estado del usuario como String "true"/"false" (opcional) */
    private String estado;
    /** Nueva contraseña en texto plano (opcional, se hashea antes de guardar) */
    private String contrasena;

    /**
     * Constructor vacío requerido para la deserialización con Gson.
     * Permite que Gson cree instancias sin parámetros.
     */
    public UpdateUserRequest() {}

    /**
     * Valida que al menos uno de los campos actualizables (nombre o correo) esté presente.
     * Requiere que al menos un campo obligatorio venga en la petición.
     * @return true si hay al menos un campo válido para actualizar
     */
    public boolean isValid() {
        // nombre presente y no vacío
        return (nombre != null && !nombre.trim().isEmpty())
                // o correo presente y no vacío
                || (correo != null && !correo.trim().isEmpty());
    }

    /** @return Nombre del usuario */
    public String getNombre() { return nombre; }
    /** @param nombre Nombre a establecer */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return Apellido del usuario */
    public String getApellido() { return apellido; }
    /** @param apellido Apellido a establecer */
    public void setApellido(String apellido) { this.apellido = apellido; }

    /** @return Correo electrónico del usuario */
    public String getCorreo() { return correo; }
    /** @param correo Correo a establecer */
    public void setCorreo(String correo) { this.correo = correo; }

    /** @return Estado del usuario */
    public String getEstado() { return estado; }
    /** @param estado Estado a establecer */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return Contraseña del usuario */
    public String getContrasena() { return contrasena; }
    /** @param contrasena Contraseña a establecer */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
