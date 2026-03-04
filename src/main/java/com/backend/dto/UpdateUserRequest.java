package com.backend.dto; // Paquete de Data Transfer Objects

/**
 * DTO que representa los datos opcionales para actualizar un usuario (PUT o PATCH).
 * Todos los campos son opcionales: si vienen null, no se actualizan.
 * Facilita actualizaciones parciales de usuarios.
 */
public class UpdateUserRequest {

    /** Nuevo nombre del usuario (opcional) */
    private String nombre; // Campo para nombre del usuario
    /** Nuevo correo electrónico (opcional, debe ser único) */
    private String correo; // Campo para correo del usuario
    /** Nuevo estado del usuario como String "true"/"false" (opcional) */
    private String estado; // Campo para estado del usuario
    /** Nueva contraseña en texto plano (opcional, se hashea antes de guardar) */
    private String contrasena; // Campo para contraseña del usuario

    /**
     * Constructor vacío requerido para la deserialización con Gson.
     * Permite que Gson cree instancias sin parámetros.
     */
    public UpdateUserRequest() {} // Constructor por defecto para Gson

    /**
     * Valida que al menos uno de los campos actualizables (nombre o correo) esté presente.
     * Requiere que al menos un campo obligatorio venga en la petición.
     * @return true si hay al menos un campo válido para actualizar
     */
    public boolean isValid() { // Método de validación
        return (nombre != null && !nombre.trim().isEmpty())  // nombre presente y no vacío
                || (correo != null && !correo.trim().isEmpty()); // o correo presente y no vacío
    }

    /** @return Nombre del usuario */
    public String getNombre() { return nombre; } // Getter para nombre
    /** @param nombre Nombre a establecer */
    public void setNombre(String nombre) { this.nombre = nombre; } // Setter para nombre

    /** @return Correo electrónico del usuario */
    public String getCorreo() { return correo; } // Getter para correo
    /** @param correo Correo a establecer */
    public void setCorreo(String correo) { this.correo = correo; } // Setter para correo

    /** @return Estado del usuario */
    public String getEstado() { return estado; } // Getter para estado
    /** @param estado Estado a establecer */
    public void setEstado(String estado) { this.estado = estado; } // Setter para estado

    /** @return Contraseña del usuario */
    public String getContrasena() { return contrasena; } // Getter para contraseña
    /** @param contrasena Contraseña a establecer */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; } // Setter para contraseña
}
