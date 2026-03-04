package com.backend.dto;

// DTO (Data Transfer Object) que representa los datos necesarios para crear un nuevo usuario
// Gson lo usa para deserializar el JSON del body de POST /api/users
public class CreateUserRequest {

    // Nombre completo del nuevo usuario
    private String nombre;
    // Correo electronico (unico en el sistema)
    private String correo;
    // Contrasena en texto plano (se hashea antes de guardar)
    private String contrasena;

    // Constructor vacio requerido por Gson para la deserializacion del JSON
    public CreateUserRequest() {}

    // Verifica que los tres campos obligatorios esten presentes y no esten vacios
    public boolean isValid() {
        return nombre != null && !nombre.isBlank()       // nombre no puede ser nulo ni solo espacios
                && correo != null && !correo.isBlank()   // correo no puede ser nulo ni solo espacios
                && contrasena != null && !contrasena.isBlank(); // contrasena no puede ser nula ni vacia
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
