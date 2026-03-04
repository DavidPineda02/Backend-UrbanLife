package com.backend.dto;

// DTO que representa los datos opcionales para actualizar un usuario (PUT o PATCH)
// Todos los campos son opcionales: si vienen null, no se actualizan
public class UpdateUserRequest {

    // Nuevo nombre del usuario (opcional)
    private String nombre;
    // Nuevo correo electronico (opcional, debe ser unico)
    private String correo;
    // Nuevo estado del usuario como String "true"/"false" (opcional)
    private String estado;
    // Nueva contrasena en texto plano (opcional, se hashea antes de guardar)
    private String contrasena;

    // Constructor vacio requerido para la deserializacion con Gson
    public UpdateUserRequest() {}

    // Valida que al menos uno de los campos actualizables (nombre o correo) este presente
    public boolean isValid() {
        return (nombre != null && !nombre.trim().isEmpty())  // nombre presente y no vacio
                || (correo != null && !correo.trim().isEmpty()); // o correo presente y no vacio
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
