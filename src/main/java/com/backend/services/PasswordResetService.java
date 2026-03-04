package com.backend.services;

// Para guardar y validar tokens de recuperacion en la BD
import com.backend.dao.TokenRecuperacionDAO;
// Para buscar el usuario por correo
import com.backend.dao.UsuarioDAO;
// Para hashear la nueva contrasena antes de guardarla
import com.backend.helpers.PasswordHelper;
// Entidad del usuario del sistema
import com.backend.models.Usuario;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Para calcular la fecha de expiracion del token (1 hora desde ahora)
import java.time.LocalDateTime;

/**
 * Servicio que implementa el flujo completo de recuperación de contraseña.
 * Genera tokens, valida y cambia contraseñas de forma segura.
 */
public class PasswordResetService {

    /**
     * Primer paso del flujo: genera un token UUID, lo guarda y envía el correo con el link.
     * @param correo Correo electrónico del usuario que solicita la recuperación
     * @return JsonObject con el resultado de la solicitud
     */
    public static JsonObject solicitarRecuperacion(String correo) {
        JsonObject respuesta = new JsonObject();

        // Validar que el correo no venga vacio
        if (correo == null || correo.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo es requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Buscar si el correo existe en la base de datos
        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        if (usuario == null) {
            // Respuesta generica para no revelar si el correo esta registrado (seguridad)
            respuesta.addProperty("success", true);
            respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
            respuesta.addProperty("status", 200);
            return respuesta;
        }

        // Las cuentas de Google no tienen contrasena, no pueden usar este flujo
        if (usuario.getContrasena() == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Esta cuenta usa inicio de sesion con Google");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // No permitir recuperacion para cuentas inactivas
        if (!usuario.isEstado()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            respuesta.addProperty("status", 403);
            return respuesta;
        }

        // Calcular la fecha de expiracion del token: 1 hora desde ahora
        LocalDateTime expiracion = LocalDateTime.now().plusHours(1);
        // Guardar el token UUID en la BD y retornarlo para enviarlo por correo
        String token = TokenRecuperacionDAO.guardarToken(usuario.getIdUsuario(), expiracion);

        if (token == null) {
            // Error al insertar el token en la base de datos
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al generar el token");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // Intentar enviar el correo con el link de recuperacion
        boolean correoEnviado = EmailService.enviarCorreoRecuperacion(correo, token);

        if (!correoEnviado) {
            // Si falla el correo, mostrar el link en consola para pruebas locales
            System.out.println("==============================");
            System.out.println("LINK RECUPERACION (fallo email):");
            System.out.println("http://localhost:5500/reset-password.html?token=" + token);
            System.out.println("==============================");
        }

        // Respuesta generica independientemente del resultado (no revelar existencia del correo)
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
        respuesta.addProperty("status", 200);
        return respuesta;
    }

    /**
     * Segundo paso del flujo: verifica que el token sea válido y no haya expirado.
     * @param token Token de recuperación a validar
     * @return JsonObject con el resultado de la validación
     */
    public static JsonObject validarToken(String token) {
        JsonObject respuesta = new JsonObject();

        // Validar que el token no venga vacio antes de consultar la BD
        if (token == null || token.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El token es requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Delegar la validacion completa al DAO (busca en BD, verifica expiracion y estado)
        return TokenRecuperacionDAO.validarToken(token);
    }

    /**
     * Tercer paso del flujo: cambia la contraseña usando el token de recuperación.
     * @param token Token de recuperación válido
     * @param nuevaContrasena Nueva contraseña a establecer
     * @return JsonObject con el resultado del cambio de contraseña
     */
    public static JsonObject cambiarContrasena(String token, String nuevaContrasena) {
        JsonObject respuesta = new JsonObject();

        // Validar que ambos campos vengan con valor
        if (token == null || token.isBlank() || nuevaContrasena == null || nuevaContrasena.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token y nueva contrasena son requeridos");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar que la nueva contrasena cumpla la politica: min 8 chars, mayuscula, minuscula y numero
        if (!nuevaContrasena.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar el token en la BD (no expirado, no usado)
        JsonObject validacion = TokenRecuperacionDAO.validarToken(token);
        if (!validacion.get("success").getAsBoolean()) {
            // Si el token es invalido, retornar directamente la respuesta de error del DAO
            return validacion;
        }

        // Extraer el id del usuario y del token desde la respuesta del DAO
        int usuarioId = validacion.get("usuarioId").getAsInt(); // Para actualizar su contrasena
        int idToken   = validacion.get("idToken").getAsInt();   // Para marcarlo como usado

        // Hashear la nueva contrasena con BCrypt antes de guardarla
        String hashNuevaContrasena = PasswordHelper.hashPassword(nuevaContrasena);
        // Actualizar la contrasena del usuario en la base de datos
        boolean actualizado = TokenRecuperacionDAO.actualizarContrasena(usuarioId, hashNuevaContrasena);

        if (!actualizado) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al actualizar la contrasena");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // Marcar el token como usado para que no pueda reutilizarse
        TokenRecuperacionDAO.marcarTokenUsado(idToken);

        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Contrasena actualizada correctamente");
        respuesta.addProperty("status", 200);
        return respuesta;
    }
}
