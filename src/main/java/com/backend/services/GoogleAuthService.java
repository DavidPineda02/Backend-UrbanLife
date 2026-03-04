package com.backend.services;

// Para buscar el rol EMPLEADO por nombre y asignarlo a nuevos usuarios
import com.backend.dao.RolDAO;
// Para buscar, crear y vincular usuarios en la base de datos
import com.backend.dao.UsuarioDAO;
// Para crear la relacion usuario-rol en la tabla de union
import com.backend.dao.UsuarioRolDAO;
// Para generar el JWT una vez autenticado el usuario
import com.backend.helpers.JwtHelper;
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Servicio que maneja la autenticacion con Google OAuth 2.0
public class GoogleAuthService {

    // Client ID de la aplicacion en Google Cloud Console (para validar el token)
    private static final String CLIENT_ID = Dotenv.load().get("GOOGLE_CLIENT_ID");
    // Instancia compartida de Gson para deserializar respuestas JSON
    private static final Gson gson = new Gson();

    // Verifica el token de Google, y autentica o crea al usuario en el sistema
    public static JsonObject loginWithGoogle(String idToken) {
        JsonObject respuesta = new JsonObject();

        // Validar que el token de Google no venga nulo o vacio
        if (idToken == null || idToken.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token de Google requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Verificar el token llamando a la API de Google tokeninfo
        JsonObject datosGoogle = verificarTokenConGoogle(idToken);
        if (datosGoogle == null) {
            // El token es invalido, fue alterado o ya expiro
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token de Google inválido o expirado");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // El campo "aud" del token debe coincidir con el CLIENT_ID de nuestra app
        // Esto evita que tokens de otras apps de Google sean aceptados
        String audience = datosGoogle.has("aud") ? datosGoogle.get("aud").getAsString() : "";
        if (!CLIENT_ID.equals(audience)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token no autorizado para esta aplicación");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Verificar que Google haya validado el correo del usuario
        String emailVerified = datosGoogle.has("email_verified") ? datosGoogle.get("email_verified").getAsString() : "false";
        if (!"true".equals(emailVerified)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo de Google no está verificado");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Extraer los datos del usuario desde el payload del token verificado
        String googleId = datosGoogle.get("sub").getAsString();    // ID unico de Google (subject)
        String correo = datosGoogle.get("email").getAsString();    // Correo de la cuenta Google
        String nombre = datosGoogle.has("name") ? datosGoogle.get("name").getAsString() : correo; // Nombre o correo como fallback

        // Intentar encontrar el usuario por su google_id (logins previos con Google)
        Usuario usuario = UsuarioDAO.findByGoogleId(googleId);

        if (usuario == null) {
            // No encontrado por google_id: buscar por correo (podria tener cuenta manual)
            usuario = UsuarioDAO.findByCorreo(correo);

            if (usuario != null) {
                // El correo ya existe con cuenta manual: vincular el google_id a esa cuenta
                UsuarioDAO.linkGoogleId(usuario.getIdUsuario(), googleId);
                usuario.setGoogleId(googleId); // Actualizar el objeto en memoria tambien
            } else {
                // El correo no existe: crear una cuenta nueva vinculada a Google
                usuario = UsuarioDAO.createWithGoogle(googleId, nombre, correo);
                if (usuario == null) {
                    respuesta.addProperty("success", false);
                    respuesta.addProperty("message", "Error al crear el usuario");
                    respuesta.addProperty("status", 500);
                    return respuesta;
                }

                // Asignar rol EMPLEADO por defecto al nuevo usuario de Google
                Rol rolEmpleado = RolDAO.findByNombre("EMPLEADO");
                if (rolEmpleado != null) {
                    // Crear el registro en la tabla de relacion usuario-rol
                    UsuarioRolDAO.create(new UsuarioRol(usuario.getIdUsuario(), rolEmpleado.getIdRoles()));
                } else {
                    System.out.println("Advertencia: no se encontro el rol EMPLEADO para asignar al nuevo usuario Google");
                }
            }
        }

        // Verificar que el usuario (nuevo o existente) este activo en el sistema
        if (!usuario.isEstado()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            respuesta.addProperty("status", 403);
            return respuesta;
        }

        // Obtener el rol del usuario para incluirlo en el JWT
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        if (rol == null) rol = "Sin rol"; // Fallback si por alguna razon no tiene rol

        // Generar el JWT con los datos de identidad del usuario autenticado
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        // Construir la respuesta exitosa con el token y los datos del usuario
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Login con Google exitoso");
        respuesta.addProperty("token", token);
        respuesta.addProperty("nombre", usuario.getNombre());
        respuesta.addProperty("correo", usuario.getCorreo());
        respuesta.addProperty("rol", rol);
        respuesta.addProperty("status", 200);

        return respuesta;
    }

    // Llama a la API de Google tokeninfo para verificar la validez del id_token
    // Retorna el payload JSON del token si es valido, o null si falla la verificacion
    private static JsonObject verificarTokenConGoogle(String idToken) {
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
                System.out.println("Error GoogleAuthService: tokeninfo respondio " + respuestaGoogle.statusCode());
                return null;
            }

            // Deserializar el cuerpo de la respuesta de Google como JsonObject y retornarlo
            return gson.fromJson(respuestaGoogle.body(), JsonObject.class);

        } catch (Exception excepcion) {
            // Error de red o al parsear la respuesta
            System.out.println("Error GoogleAuthService.verificarTokenConGoogle: " + excepcion.getMessage());
            return null;
        }
    }
}
