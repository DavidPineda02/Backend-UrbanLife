package com.backend.services; // Paquete de servicios de lógica de negocio

// Para buscar el rol EMPLEADO por nombre y asignarlo a nuevos usuarios
import com.backend.dao.RolDAO; // DAO para operaciones de roles
// Para buscar, crear y vincular usuarios en la base de datos
import com.backend.dao.UsuarioDAO; // DAO para operaciones de usuarios
// Para crear la relacion usuario-rol en la tabla de union
import com.backend.dao.UsuarioRolDAO; // DAO para relación usuario-rol
// Para generar el JWT una vez autenticado el usuario
import com.backend.helpers.JwtHelper; // Helper para generación de tokens JWT
// Entidad del rol del sistema
import com.backend.models.Rol; // Modelo de datos de rol
// Entidad del usuario del sistema
import com.backend.models.Usuario; // Modelo de datos de usuario
// Entidad de la relacion usuario-rol
import com.backend.models.UsuarioRol; // Modelo de datos de relación usuario-rol
// Para deserializar la respuesta JSON de la API de Google
import com.google.gson.Gson; // Biblioteca para manejo de JSON
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonObject; // Clase para objetos JSON
// Para leer GOOGLE_CLIENT_ID del archivo .env
import io.github.cdimascio.dotenv.Dotenv; // Biblioteca para variables de entorno

// Para construir la URL de la API de Google tokeninfo
import java.net.URI; // Clase para URIs
// Cliente HTTP nativo de Java 11+ para hacer la peticion a Google
import java.net.http.HttpClient; // Cliente HTTP
import java.net.http.HttpRequest; // Clase para peticiones HTTP
import java.net.http.HttpResponse; // Clase para respuestas HTTP

/**
 * Servicio que maneja la autenticación con Google OAuth 2.0.
 * Verifica tokens de Google, crea usuarios y genera JWTs.
 * Centraliza la lógica de autenticación con Google Sign-In.
 */
public class GoogleAuthService {

    /** Client ID de la aplicación en Google Cloud Console (para validar el token) */
    private static final String CLIENT_ID = Dotenv.load().get("GOOGLE_CLIENT_ID"); // ID de cliente de Google
    /** Instancia compartida de Gson para deserializar respuestas JSON */
    private static final Gson gson = new Gson(); // Instancia para serialización JSON

    /**
     * Verifica el token de Google, y autentica o crea al usuario en el sistema.
     * Realiza validación completa del token y gestión de usuarios.
     * @param idToken Token JWT de Google Sign-In
     * @return JsonObject con el resultado de la autenticación y JWT si es exitoso
     */
    public static JsonObject loginWithGoogle(String idToken) { // Método principal de login con Google
        JsonObject respuesta = new JsonObject(); // Crear objeto de respuesta

        // Validar que el token de Google no venga nulo o vacio
        if (idToken == null || idToken.isBlank()) { // Validar token vacío
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Token de Google requerido"); // Mensaje de error
            respuesta.addProperty("status", 400); // Código HTTP 400
            return respuesta; // Retornar respuesta de error
        }

        // Verificar el token llamando a la API de Google tokeninfo
        JsonObject datosGoogle = verificarTokenConGoogle(idToken); // Validar token con Google
        if (datosGoogle == null) { // Validar respuesta de Google
            // El token es invalido, fue alterado o ya expiro
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Token de Google inválido o expirado"); // Mensaje de error
            respuesta.addProperty("status", 401); // Código HTTP 401
            return respuesta; // Retornar respuesta de error
        }

        // El campo "aud" del token debe coincidir con el CLIENT_ID de nuestra app
        // Esto evita que tokens de otras apps de Google sean aceptados
        String audience = datosGoogle.has("aud") ? datosGoogle.get("aud").getAsString() : ""; // Extraer audience
        if (!CLIENT_ID.equals(audience)) { // Validar audience
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Token no autorizado para esta aplicación"); // Mensaje de error
            respuesta.addProperty("status", 401); // Código HTTP 401
            return respuesta; // Retornar respuesta de error
        }

        // Verificar que Google haya validado el correo del usuario
        String emailVerified = datosGoogle.has("email_verified") ? datosGoogle.get("email_verified").getAsString() : "false"; // Extraer verificación
        if (!"true".equals(emailVerified)) { // Validar correo verificado
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "El correo de Google no está verificado"); // Mensaje de error
            respuesta.addProperty("status", 401); // Código HTTP 401
            return respuesta; // Retornar respuesta de error
        }

        // Extraer los datos del usuario desde el payload del token verificado
        String googleId = datosGoogle.get("sub").getAsString();    // ID unico de Google (subject)
        String correo = datosGoogle.get("email").getAsString();    // Correo de la cuenta Google
        String nombre = datosGoogle.has("name") ? datosGoogle.get("name").getAsString() : correo; // Nombre o correo como fallback

        // Intentar encontrar el usuario por su google_id (logins previos con Google)
        Usuario usuario = UsuarioDAO.findByGoogleId(googleId); // Buscar por Google ID

        if (usuario == null) { // Validar si existe usuario
            // No encontrado por google_id: buscar por correo (podria tener cuenta manual)
            usuario = UsuarioDAO.findByCorreo(correo); // Buscar por correo

            if (usuario != null) { // Validar si existe usuario con ese correo
                // El correo ya existe con cuenta manual: vincular el google_id a esa cuenta
                UsuarioDAO.linkGoogleId(usuario.getIdUsuario(), googleId); // Vincular Google ID
                usuario.setGoogleId(googleId); // Actualizar el objeto en memoria tambien
            } else { // Usuario no existe
                // El correo no existe: crear una cuenta nueva vinculada a Google
                usuario = UsuarioDAO.createWithGoogle(googleId, nombre, correo); // Crear usuario con Google
                if (usuario == null) { // Validar creación exitosa
                    respuesta.addProperty("success", false); // Indicar fallo
                    respuesta.addProperty("message", "Error al crear el usuario"); // Mensaje de error
                    respuesta.addProperty("status", 500); // Código HTTP 500
                    return respuesta; // Retornar respuesta de error
                }

                // Asignar rol EMPLEADO por defecto al nuevo usuario de Google
                Rol rolEmpleado = RolDAO.findByNombre("EMPLEADO"); // Buscar rol EMPLEADO
                if (rolEmpleado != null) { // Validar que exista el rol
                    // Crear el registro en la tabla de relacion usuario-rol
                    UsuarioRolDAO.create(new UsuarioRol(usuario.getIdUsuario(), rolEmpleado.getIdRoles())); // Crear relación
                } else { // Rol no encontrado
                    System.out.println("Advertencia: no se encontro el rol EMPLEADO para asignar al nuevo usuario Google"); // Log de advertencia
                }
            }
        }

        // Verificar que el usuario (nuevo o existente) este activo en el sistema
        if (!usuario.isEstado()) { // Validar estado del usuario
            respuesta.addProperty("success", false); // Indicar fallo
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador"); // Mensaje de inactividad
            respuesta.addProperty("status", 403); // Código HTTP 403
            return respuesta; // Retornar respuesta de error
        }

        // Obtener el rol del usuario para incluirlo en el JWT
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario()); // Buscar rol del usuario
        if (rol == null) rol = "Sin rol"; // Fallback si por alguna razon no tiene rol

        // Generar el JWT con los datos de identidad del usuario autenticado
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol); // Generar token JWT

        // Construir la respuesta exitosa con el token y los datos del usuario
        respuesta.addProperty("success", true); // Indicar éxito
        respuesta.addProperty("message", "Login con Google exitoso"); // Mensaje de éxito
        respuesta.addProperty("token", token); // Token JWT
        respuesta.addProperty("nombre", usuario.getNombre()); // Nombre del usuario
        respuesta.addProperty("correo", usuario.getCorreo()); // Correo del usuario
        respuesta.addProperty("rol", rol); // Rol del usuario
        respuesta.addProperty("status", 200); // Código HTTP 200

        return respuesta; // Retornar respuesta exitosa
    }

    /**
     * Llama a la API de Google tokeninfo para verificar la validez del id_token.
     * Realiza petición HTTP a Google para validar el token.
     * @param idToken Token JWT de Google Sign-In a verificar
     * @return JsonObject con el payload del token si es válido, o null si falla la verificación
     */
    private static JsonObject verificarTokenConGoogle(String idToken) { // Método para verificar token
        try { // Bloque try para manejar excepciones
            // Crear un cliente HTTP nativo de Java para hacer la peticion GET
            HttpClient cliente = HttpClient.newHttpClient(); // Crear cliente HTTP
            // Construir la peticion GET al endpoint tokeninfo de Google con el token
            HttpRequest peticion = HttpRequest.newBuilder() // Crear petición HTTP
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)) // URL con token
                    .GET() // Método GET
                    .build(); // Construir petición

            // Ejecutar la peticion y obtener la respuesta como String
            HttpResponse<String> respuestaGoogle = cliente.send(peticion, HttpResponse.BodyHandlers.ofString()); // Enviar petición

            // Si Google responde con algo distinto a 200, el token es invalido o expiro
            if (respuestaGoogle.statusCode() != 200) { // Validar código de respuesta
                System.out.println("Error GoogleAuthService: tokeninfo respondio " + respuestaGoogle.statusCode()); // Log de error
                return null; // Retornar null
            }

            // Deserializar el cuerpo de la respuesta de Google como JsonObject y retornarlo
            return gson.fromJson(respuestaGoogle.body(), JsonObject.class); // Parsear respuesta JSON

        } catch (Exception excepcion) { // Capturar errores generales
            // Error de red o al parsear la respuesta
            System.out.println("Error GoogleAuthService.verificarTokenConGoogle: " + excepcion.getMessage()); // Log de error
            return null;
        }
    }
}
