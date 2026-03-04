package com.backend.dto;

/**
 * DTO que representa los datos opcionales para actualizar un usuario (PUT o PATCH).
 * Todos los campos son opcionales: si vienen null, no se actualizan.
 */
public class UpdateUserRequest {

    /** Nuevo nombre del usuario (opcional) */
    private String nombre;
    /** Nuevo correo electrónico (opcional, debe ser único) */
    private String correo;
    /** Nuevo estado del usuario como String "true"/"false" (opcional) */
    private String estado;
    /** Nueva contraseña en texto plano (opcional, se hashea antes de guardar) */
    private String contrasena;

    /**
     * Constructor vacío requerido para la deserialización con Gson.
     */
    public UpdateUserRequest() {}

    /**
     * Valida que al menos uno de los campos actualizables (nombre o correo) esté presente.
     * @return true si hay al menos un campo válido para actualizar
     */
    public boolean isValid() {
        return (nombre != null && !nombre.trim().isEmpty())  // nombre presente y no vacío
                || (correo != null && !correo.trim().isEmpty()); // o correo presente y no vacío
    }

    /** @return Nombre del usuario */
    public String getNombre() { return nombre; }
    /** @param nombre Nombre a establecer */
    public void setNombre(String nombre) { this.nombre = nombre; }

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
