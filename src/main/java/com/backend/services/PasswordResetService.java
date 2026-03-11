// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para guardar y validar tokens de recuperación en la BD
import com.backend.dao.TokenRecuperacionDAO;
// Para buscar el usuario por correo y actualizar su contraseña
import com.backend.dao.UsuarioDAO;
// Para hashear la nueva contraseña antes de guardarla
import com.backend.helpers.PasswordHelper;
// Para las expresiones regulares y políticas de validación compartidas
import com.backend.helpers.ValidationHelper;
// Entidad que representa al usuario del sistema
import com.backend.models.Usuario;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;

// Para calcular la fecha de expiración del token (1 hora desde ahora)
import java.time.LocalDateTime;

/**
 * Servicio que implementa el flujo completo de recuperación de contraseña en 3 pasos.
 * Paso 1: solicitarRecuperacion — genera y envía el token por correo.
 * Paso 2: validarToken — verifica que el token sea válido y no haya expirado.
 * Paso 3: cambiarContrasena — actualiza la contraseña usando el token.
 */
public class PasswordResetService {

    /**
     * Primer paso del flujo: valida el correo, genera un token UUID y lo envía por correo.
     * Usa mensajes genéricos para no revelar si el correo está o no registrado.
     * @param correo Correo electrónico del usuario que solicita la recuperación
     * @return JsonObject con el resultado de la solicitud
     */
    public static JsonObject solicitarRecuperacion(String correo) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validaciones del campo Correo -----

        // Verificar que el correo no sea nulo ni esté vacío
        if (correo == null || correo.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el correo es obligatorio
            respuesta.addProperty("message", "El correo es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que el correo no supere la longitud máxima permitida
        if (correo.length() > 100) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres
            respuesta.addProperty("message", "El correo no puede superar los 100 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que el correo tenga un formato válido usando la expresión regular compartida
        if (!correo.matches(ValidationHelper.EMAIL_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el formato inválido del correo
            respuesta.addProperty("message", "El formato del correo no es válido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Verificar existencia del usuario -----

        // Buscar si el correo existe en la base de datos
        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        // Si el usuario no existe, responder con mensaje genérico para no revelar que no está registrado
        if (usuario == null) {
            // Indicar éxito aparente (el usuario no sabe si el correo existe o no)
            respuesta.addProperty("success", true);
            // Mensaje genérico de seguridad
            respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
            // Código HTTP 200 OK
            respuesta.addProperty("status", 200);
            // Retornar respuesta genérica sin revelar información
            return respuesta;
        }

        // Si el usuario se registró con Google y no tiene contraseña, no se puede recuperar.
        // Se responde con el mensaje genérico para no revelar que existe cuenta Google con ese correo.
        if (usuario.getContrasena() == null) {
            // Indicar éxito aparente (el usuario no sabe que la cuenta es de Google)
            respuesta.addProperty("success", true);
            // Mensaje genérico de seguridad (igual que cuando no se encuentra el correo)
            respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
            // Código HTTP 200 OK
            respuesta.addProperty("status", 200);
            // Retornar respuesta genérica sin revelar información
            return respuesta;
        }

        // No permitir recuperación para cuentas inactivas (desactivadas por un admin)
        if (!usuario.isEstado()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la cuenta está inactiva
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            // Código HTTP 403 Forbidden
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Generar y guardar el token -----

        // Calcular la fecha de expiración: 1 hora desde el momento actual
        LocalDateTime expiracion = LocalDateTime.now().plusHours(1);
        // Guardar el hash SHA-256 del token en la BD y obtener el token original para enviarlo
        String token = TokenRecuperacionDAO.guardarToken(usuario.getIdUsuario(), expiracion);

        // Verificar si hubo error al guardar el token en la base de datos
        if (token == null) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno al generar el token
            respuesta.addProperty("message", "Error al generar el token");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Enviar el correo con el link de recuperación -----

        // Intentar enviar el correo con el link que contiene el token UUID
        boolean correoEnviado = EmailService.enviarCorreoRecuperacion(correo, token);

        // Si falla el envío del correo, imprimir el link en consola para pruebas locales
        if (!correoEnviado) {
            // Separador visual en consola
            System.out.println("==============================");
            // Encabezado del link de recuperación
            System.out.println("LINK RECUPERACION (fallo email):");
            // Imprimir el link completo con el token para pruebas en desarrollo
            System.out.println("http://localhost:5173/view/nueva-password.html?token=" + token);
            // Separador visual en consola
            System.out.println("==============================");
        }

        // Responder con mensaje genérico independientemente del resultado del correo
        // (no revelar si el correo existe ni si el envío fue exitoso)
        respuesta.addProperty("success", true);
        // Mensaje genérico de seguridad
        respuesta.addProperty("message", "Si el correo está registrado, recibirás un enlace en breve");
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta genérica
        return respuesta;
    }

    /**
     * Segundo paso del flujo: verifica que el token sea válido y no haya expirado.
     * Delega la validación completa al DAO que consulta la base de datos.
     * @param token Token de recuperación recibido desde la URL del correo
     * @return JsonObject con el resultado de la validación
     */
    public static JsonObject validarToken(String token) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Verificar que el token no sea nulo ni esté vacío antes de consultar la BD
        if (token == null || token.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el token es obligatorio
            respuesta.addProperty("message", "El token es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Delegar la validación completa al DAO (verifica en BD, expiración y estado de uso)
        return TokenRecuperacionDAO.validarToken(token);
    }

    /**
     * Tercer paso del flujo: cambia la contraseña usando el token de recuperación válido.
     * Valida el token, hashea la nueva contraseña y la actualiza en la BD.
     * @param token Token de recuperación recibido desde el formulario del frontend
     * @param nuevaContrasena Nueva contraseña en texto plano a establecer
     * @return JsonObject con el resultado del cambio de contraseña
     */
    public static JsonObject cambiarContrasena(String token, String nuevaContrasena) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // ----- Validaciones de los campos requeridos -----

        // Verificar que el token no sea nulo ni esté vacío
        if (token == null || token.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que el token es obligatorio
            respuesta.addProperty("message", "El token de recuperación es requerido");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que la nueva contraseña no sea nula ni esté vacía
        if (nuevaContrasena == null || nuevaContrasena.isBlank()) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando que la contraseña es obligatoria
            respuesta.addProperty("message", "La contraseña es requerida");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que la contraseña no supere la longitud máxima permitida por la política del sistema
        if (nuevaContrasena.length() > 20) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje indicando el máximo de caracteres permitidos
            respuesta.addProperty("message", "La contraseña no puede superar los 20 caracteres");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que la nueva contraseña cumpla la política: entre 8 y 20 chars sin espacios, mayúscula, minúscula y número
        if (!nuevaContrasena.matches(ValidationHelper.PASSWORD_REGEX)) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje explicando los requisitos de la contraseña
            respuesta.addProperty("message", "La contraseña debe tener entre 8 y 20 caracteres, sin espacios, una mayúscula, una minúscula y un número");
            // Código HTTP 400 Bad Request
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // ----- Validar el token en la base de datos -----

        // Consultar en la BD si el token es válido, no ha expirado y no fue usado
        JsonObject validacion = TokenRecuperacionDAO.validarToken(token);
        // Si el token no es válido, retornar directamente la respuesta de error del DAO
        if (!validacion.get("success").getAsBoolean()) {
            // Retornar la respuesta de error con el motivo específico del DAO
            return validacion;
        }

        // ----- Actualizar la contraseña en la base de datos -----

        // Extraer el ID del usuario desde la respuesta de validación del token
        int usuarioId = validacion.get("usuarioId").getAsInt();
        // Extraer el ID del token para marcarlo como usado después
        int idToken   = validacion.get("idToken").getAsInt();

        // Hashear la nueva contraseña con BCrypt antes de guardarla (nunca texto plano)
        String hashNuevaContrasena = PasswordHelper.hashPassword(nuevaContrasena);
        // Actualizar la contraseña hasheada del usuario en la base de datos
        boolean actualizado = UsuarioDAO.updatePassword(usuarioId, hashNuevaContrasena);

        // Verificar si hubo error al actualizar la contraseña en la base de datos
        if (!actualizado) {
            // Indicar que la operación falló
            respuesta.addProperty("success", false);
            // Mensaje de error interno al actualizar la contraseña
            respuesta.addProperty("message", "Error al actualizar la contrasena");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
            // Retornar respuesta de error
            return respuesta;
        }

        // Marcar el token como usado para que no pueda reutilizarse en futuros intentos
        TokenRecuperacionDAO.marcarTokenUsado(idToken);

        // Indicar que la operación fue exitosa
        respuesta.addProperty("success", true);
        // Mensaje confirmando el cambio de contraseña exitoso
        respuesta.addProperty("message", "Contrasena actualizada correctamente");
        // Código HTTP 200 OK
        respuesta.addProperty("status", 200);
        // Retornar la respuesta exitosa
        return respuesta;
    }
}
