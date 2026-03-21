// Paquete de servicios de lógica de negocio
package com.backend.services;

// Para insertar el correo principal en Correos_Usuario tras crear el usuario
import com.backend.dao.CorreoUsuarioDAO;
// Para buscar el rol EMPLEADO por nombre y asignarlo a nuevos usuarios
import com.backend.dao.RolDAO;
// Para buscar, crear y vincular usuarios en la base de datos
import com.backend.dao.UsuarioDAO;
// Para crear la relacion usuario-rol en la tabla de union
import com.backend.dao.UsuarioRolDAO;
// Para generar el JWT una vez autenticado el usuario
import com.backend.helpers.JwtHelper;
// Modelo que representa un correo electrónico asociado a un usuario
import com.backend.models.CorreoUsuario;
// Entidad del rol del sistema
import com.backend.models.Rol;
// Entidad del usuario del sistema
import com.backend.models.Usuario;
// Entidad de la relacion usuario-rol
import com.backend.models.UsuarioRol;
// Para deserializar la respuesta JSON de la API de Google
import com.google.gson.Gson;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject;
// Para leer GOOGLE_CLIENT_ID del archivo .env
import io.github.cdimascio.dotenv.Dotenv;

// Para construir la URL de la API de Google tokeninfo
import java.net.URI;
// Cliente HTTP nativo de Java 11+ para hacer la peticion a Google
import java.net.http.HttpClient;
// Clase para peticiones HTTP
import java.net.http.HttpRequest;
// Clase para respuestas HTTP
import java.net.http.HttpResponse;

/**
 * Servicio que maneja la autenticación con Google OAuth 2.0.
 * Verifica tokens de Google, crea usuarios y genera JWTs.
 * Centraliza la lógica de autenticación con Google Sign-In.
 */
public class GoogleAuthService {

    /** Client ID de la aplicación en Google Cloud Console (para validar el token) */
    private static final String CLIENT_ID = Dotenv.load().get("GOOGLE_CLIENT_ID");
    /** Instancia compartida de Gson para deserializar respuestas JSON */
    private static final Gson gson = new Gson();

    /**
     * Verifica el token de Google, y autentica o crea al usuario en el sistema.
     * Realiza validación completa del token y gestión de usuarios.
     * @param idToken Token JWT de Google Sign-In
     * @return JsonObject con el resultado de la autenticación y JWT si es exitoso
     */
    public static JsonObject loginWithGoogle(String idToken) {
        // Crear objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Validar que el token de Google no venga nulo o vacio
        if (idToken == null || idToken.isBlank()) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de error
            respuesta.addProperty("message", "Token de Google requerido");
            // Código HTTP 400
            respuesta.addProperty("status", 400);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar el token llamando a la API de Google tokeninfo
        JsonObject datosGoogle = verificarTokenConGoogle(idToken);
        // Validar respuesta de Google
        if (datosGoogle == null) {
            // El token es invalido, fue alterado o ya expiro
            respuesta.addProperty("success", false);
            // Mensaje de error
            respuesta.addProperty("message", "Token de Google inválido o expirado");
            // Código HTTP 401
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // El campo "aud" del token debe coincidir con el CLIENT_ID de nuestra app
        // Esto evita que tokens de otras apps de Google sean aceptados
        String audience = datosGoogle.has("aud") ? datosGoogle.get("aud").getAsString() : "";
        // Validar audience
        if (!CLIENT_ID.equals(audience)) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de error
            respuesta.addProperty("message", "Token no autorizado para esta aplicación");
            // Código HTTP 401
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Verificar que Google haya validado el correo del usuario
        String emailVerified = datosGoogle.has("email_verified") ? datosGoogle.get("email_verified").getAsString() : "false";
        // Validar correo verificado
        if (!"true".equals(emailVerified)) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de error
            respuesta.addProperty("message", "El correo de Google no está verificado");
            // Código HTTP 401
            respuesta.addProperty("status", 401);
            // Retornar respuesta de error
            return respuesta;
        }

        // Extraer los datos del usuario desde el payload del token verificado
        String googleId = datosGoogle.get("sub").getAsString();
        // Correo de la cuenta Google
        String correo = datosGoogle.get("email").getAsString();
        // Nombre (given_name) o correo como fallback
        String nombre = datosGoogle.has("given_name") ? datosGoogle.get("given_name").getAsString() : correo;
        // Apellido (family_name) o vacío como fallback
        String apellido = datosGoogle.has("family_name") ? datosGoogle.get("family_name").getAsString() : "";

        // Intentar encontrar el usuario por su google_id (logins previos con Google)
        Usuario usuario = UsuarioDAO.findByGoogleId(googleId);

        // Validar si existe usuario
        if (usuario == null) {
            // No encontrado por google_id: buscar por correo (podria tener cuenta manual)
            usuario = UsuarioDAO.findByCorreo(correo);

            // Validar si existe usuario con ese correo
            if (usuario != null) {
                // El correo ya existe con cuenta manual: vincular el google_id a esa cuenta
                UsuarioDAO.linkGoogleId(usuario.getIdUsuario(), googleId);
                // Actualizar el objeto en memoria tambien
                usuario.setGoogleId(googleId);
            // Usuario no existe
            } else {
                // El correo no existe: crear una cuenta nueva vinculada a Google
                usuario = UsuarioDAO.createWithGoogle(googleId, nombre, apellido, correo);
                // Validar creación exitosa
                if (usuario == null) {
                    // Indicar fallo
                    respuesta.addProperty("success", false);
                    // Mensaje de error
                    respuesta.addProperty("message", "Error al crear el usuario");
                    // Código HTTP 500
                    respuesta.addProperty("status", 500);
                    // Retornar respuesta de error
                    return respuesta;
                }

                // Insertar el correo principal en la tabla Correos_Usuario con ES_PRINCIPAL=TRUE
                CorreoUsuarioDAO.create(new CorreoUsuario(correo, true, usuario.getIdUsuario()));

                // Asignar rol EMPLEADO por defecto al nuevo usuario de Google
                Rol rolEmpleado = RolDAO.findByNombre("EMPLEADO");
                // Validar que exista el rol
                if (rolEmpleado != null) {
                    // Crear el registro en la tabla de relacion usuario-rol
                    UsuarioRolDAO.create(new UsuarioRol(usuario.getIdUsuario(), rolEmpleado.getIdRoles()));
                // Rol no encontrado
                } else {
                    // Log de advertencia
                    System.out.println("Advertencia: no se encontro el rol EMPLEADO para asignar al nuevo usuario Google");
                }
            }
        }

        // Verificar que el usuario (nuevo o existente) este activo en el sistema
        if (!usuario.isEstado()) {
            // Indicar fallo
            respuesta.addProperty("success", false);
            // Mensaje de inactividad
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            // Código HTTP 403
            respuesta.addProperty("status", 403);
            // Retornar respuesta de error
            return respuesta;
        }

        // Obtener el rol del usuario para incluirlo en el JWT
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        // Fallback si por alguna razon no tiene rol
        if (rol == null) rol = "Sin rol";

        // Generar el JWT con los datos de identidad del usuario autenticado
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        // Construir la respuesta exitosa con el token y los datos del usuario
        respuesta.addProperty("success", true);
        // Mensaje de éxito
        respuesta.addProperty("message", "Login con Google exitoso");
        // Token JWT
        respuesta.addProperty("token", token);
        // Nombre del usuario
        respuesta.addProperty("nombre", usuario.getNombre());
        // Apellido del usuario
        respuesta.addProperty("apellido", usuario.getApellido());
        // Correo del usuario
        respuesta.addProperty("correo", usuario.getCorreo());
        // Rol del usuario
        respuesta.addProperty("rol", rol);
        // Código HTTP 200
        respuesta.addProperty("status", 200);

        // Retornar respuesta exitosa
        return respuesta;
    }

    /**
     * Llama a la API de Google tokeninfo para verificar la validez del id_token.
     * Realiza petición HTTP a Google para validar el token.
     * @param idToken Token JWT de Google Sign-In a verificar
     * @return JsonObject con el payload del token si es válido, o null si falla la verificación
     */
    private static JsonObject verificarTokenConGoogle(String idToken) {
        // Bloque try para manejar excepciones
        try {
            // Crear un cliente HTTP nativo de Java para hacer la peticion GET
            HttpClient cliente = HttpClient.newHttpClient();
            // Construir la peticion GET al endpoint tokeninfo de Google con el token
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .GET()
                    .build();

            // Ejecutar la peticion y obtener la respuesta como String
            HttpResponse<String> respuestaGoogle = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

            // Si Google responde con algo distinto a 200, el token es invalido o expiro
            if (respuestaGoogle.statusCode() != 200) {
                // Log de error
                System.out.println("Error GoogleAuthService: tokeninfo respondio " + respuestaGoogle.statusCode());
                // Retornar null
                return null;
            }

            // Deserializar el cuerpo de la respuesta de Google como JsonObject y retornarlo
            return gson.fromJson(respuestaGoogle.body(), JsonObject.class);

        // Capturar errores generales
        } catch (Exception excepcion) {
            // Error de red o al parsear la respuesta
            System.out.println("Error GoogleAuthService.verificarTokenConGoogle: " + excepcion.getMessage());
            return null;
        }
    }
}
