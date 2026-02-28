package com.backend.services;

import com.backend.dao.RolDAO;
import com.backend.dao.UsuarioDAO;
import com.backend.dao.UsuarioRolDAO;
import com.backend.helpers.JwtHelper;
import com.backend.models.Rol;
import com.backend.models.Usuario;
import com.backend.models.UsuarioRol;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GoogleAuthService {

    private static final String CLIENT_ID = Dotenv.load().get("GOOGLE_CLIENT_ID");
    private static final Gson gson = new Gson();

    public static JsonObject loginWithGoogle(String idToken) {
        JsonObject respuesta = new JsonObject();

        if (idToken == null || idToken.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token de Google requerido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Verificar el token con Google
        JsonObject datosGoogle = verificarTokenConGoogle(idToken);
        if (datosGoogle == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token de Google inválido o expirado");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Validar que el token fue emitido para nuestra app
        String audience = datosGoogle.has("aud") ? datosGoogle.get("aud").getAsString() : "";
        if (!CLIENT_ID.equals(audience)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Token no autorizado para esta aplicación");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        // Validar que el correo esté verificado por Google
        String emailVerified = datosGoogle.has("email_verified") ? datosGoogle.get("email_verified").getAsString() : "false";
        if (!"true".equals(emailVerified)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo de Google no está verificado");
            respuesta.addProperty("status", 401);
            return respuesta;
        }

        String googleId = datosGoogle.get("sub").getAsString();
        String correo = datosGoogle.get("email").getAsString();
        String nombre = datosGoogle.has("name") ? datosGoogle.get("name").getAsString() : correo;

        // Buscar usuario por google_id
        Usuario usuario = UsuarioDAO.findByGoogleId(googleId);

        if (usuario == null) {
            // Buscar si ya existe un usuario con ese correo (cuenta manual)
            usuario = UsuarioDAO.findByCorreo(correo);

            if (usuario != null) {
                // Vincular google_id a la cuenta existente
                UsuarioDAO.linkGoogleId(usuario.getIdUsuario(), googleId);
                usuario.setGoogleId(googleId);
            } else {
                // Crear nuevo usuario con Google
                usuario = UsuarioDAO.createWithGoogle(googleId, nombre, correo);
                if (usuario == null) {
                    respuesta.addProperty("success", false);
                    respuesta.addProperty("message", "Error al crear el usuario");
                    respuesta.addProperty("status", 500);
                    return respuesta;
                }

                // Asignar rol EMPLEADO por defecto
                Rol rolEmpleado = RolDAO.findByNombre("EMPLEADO");
                if (rolEmpleado != null) {
                    UsuarioRolDAO.create(new UsuarioRol(usuario.getIdUsuario(), rolEmpleado.getIdRoles()));
                } else {
                    System.out.println("Advertencia: no se encontro el rol EMPLEADO para asignar al nuevo usuario Google");
                }
            }
        }

        if (!usuario.isEstado()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario inactivo. Contacte al administrador");
            respuesta.addProperty("status", 403);
            return respuesta;
        }

        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        if (rol == null) rol = "Sin rol";

        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Login con Google exitoso");
        respuesta.addProperty("token", token);
        respuesta.addProperty("nombre", usuario.getNombre());
        respuesta.addProperty("correo", usuario.getCorreo());
        respuesta.addProperty("rol", rol);
        respuesta.addProperty("status", 200);

        return respuesta;
    }

    private static JsonObject verificarTokenConGoogle(String idToken) {
        try {
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .GET()
                    .build();

            HttpResponse<String> respuestaGoogle = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

            if (respuestaGoogle.statusCode() != 200) {
                System.out.println("Error GoogleAuthService: tokeninfo respondio " + respuestaGoogle.statusCode());
                return null;
            }

            return gson.fromJson(respuestaGoogle.body(), JsonObject.class);

        } catch (Exception excepcion) {
            System.out.println("Error GoogleAuthService.verificarTokenConGoogle: " + excepcion.getMessage());
            return null;
        }
    }
}
