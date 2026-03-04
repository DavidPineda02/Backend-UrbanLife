package com.backend.services; // Paquete de servicios de lógica de negocio

// Para consultar usuarios por correo e id
import com.backend.dao.UsuarioDAO; // DAO para operaciones de base de datos de usuarios
// Para generar el JWT una vez autenticado el usuario
import com.backend.helpers.JwtHelper; // Helper para generación de tokens JWT
// Para verificar la contrasena contra el hash BCrypt almacenado
import com.backend.helpers.PasswordHelper; // Helper para validación de contraseñas
// Entidad que representa al usuario encontrado en la BD
import com.backend.models.Usuario; // Modelo de datos de usuario
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject; // Clase para objetos JSON

/**
 * Servicio que contiene la lógica de validación y autenticación del login.
 * Maneja la validación de credenciales y generación de tokens JWT.
 * Centraliza toda la lógica de autenticación del sistema.
 */
public class AuthService {

    /** Expresión regular para validar el formato del correo electrónico */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"; // Regex para email

    /**
     * Valida las credenciales y retorna un JWT si el login es exitoso.
     * Realiza validaciones completas de seguridad y genera token.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     * @return JsonObject con el resultado de la autenticación y JWT si es exitoso
     */
    public static JsonObject validateLogin(String correo, String contrasena) {
        JsonObject respuesta = new JsonObject(); // Crear objeto de respuesta

        // Verificar que correo y contrasena no sean nulos ni vacios
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) { // Validar campos vacíos
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Correo y contraseña son requeridos"); // Mensaje de error
            respuesta.addProperty("status", 400); // Código HTTP 400
            return respuesta; // Retornar respuesta de error
        }

        // Validar el formato del correo con la expresion regular
        if (!correo.matches(EMAIL_REGEX)) { // Validar formato de email
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "El formato del correo no es válido"); // Mensaje de error
            respuesta.addProperty("status", 400); // Código HTTP 400
            return respuesta; // Retornar respuesta de error
        }

        // Buscar el usuario en la BD por correo
        Usuario usuario = UsuarioDAO.findByCorreo(correo); // Buscar usuario por correo
        // Respuesta generica para no revelar si el correo existe o no
        if (usuario == null) { // Validar que exista el usuario
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Credenciales inválidas"); // Mensaje genérico de seguridad
            respuesta.addProperty("status", 401); // Código HTTP 401
            return respuesta; // Retornar respuesta de error
        }

        // Si la contrasena es null, la cuenta fue creada con Google (no tiene contrasena)
        if (usuario.getContrasena() == null) { // Validar cuenta de Google
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesión con Google"); // Mensaje específico
            respuesta.addProperty("status", 401); // Código HTTP 401
            return respuesta; // Retornar respuesta de error
        }

        // Verificar que la contrasena ingresada coincida con el hash BCrypt almacenado
        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) { // Validar contraseña
            // Respuesta generica para no revelar cual de los dos campos es incorrecto
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Credenciales inválidas"); // Mensaje genérico de seguridad
            respuesta.addProperty("status", 401); // Código HTTP 401
            return respuesta; // Retornar respuesta de error
        }

        // Verificar que el usuario este activo en el sistema
        if (!usuario.isEstado()) { // Validar estado del usuario
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador"); // Mensaje de inactividad
            respuesta.addProperty("status", 403); // Código HTTP 403
            return respuesta; // Retornar respuesta de error
        }

        // Obtener el rol del usuario desde la tabla de relacion usuarios_roles
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario()); // Buscar rol del usuario
        // Si por alguna razon no tiene rol asignado, usar "Sin rol" como fallback
        if (rol == null) rol = "Sin rol"; // Asignar rol por defecto

        // Generar el JWT con el id, correo y rol del usuario autenticado
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol); // Generar token

        // Construir la respuesta exitosa con el token y datos del usuario
        respuesta.addProperty("success", true); // Indicar éxito
        respuesta.addProperty("message", "Login exitoso"); // Mensaje de éxito
        respuesta.addProperty("token", token); // Token JWT
        respuesta.addProperty("nombre", usuario.getNombre()); // Nombre del usuario
        respuesta.addProperty("correo", usuario.getCorreo()); // Correo del usuario
        respuesta.addProperty("rol", rol); // Rol del usuario
        respuesta.addProperty("status", 200); // Código HTTP 200

        return respuesta; // Retornar respuesta exitosa
    }
}
