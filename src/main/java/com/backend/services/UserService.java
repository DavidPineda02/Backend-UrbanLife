package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class UserService {

    private static final Gson gson = new Gson();

    public static JsonObject validateAndCreate(String nombre, String correo, String contrasena, boolean estado) {
        JsonObject response = new JsonObject();

        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            response.addProperty("success", false);
            response.addProperty("message", "Nombre, correo y contraseña son requeridos");
            response.addProperty("status", 400);
            return response;
        }

        if (UsuarioDAO.findByCorreo(correo) != null) {
            response.addProperty("success", false);
            response.addProperty("message", "El correo ya está registrado");
            response.addProperty("status", 409);
            return response;
        }

        Usuario nuevo = new Usuario(nombre, correo,
                PasswordHelper.hashPassword(contrasena), estado);

        Usuario creado = UsuarioDAO.create(nuevo);

        if (creado != null) {
            creado.setContrasena(null);
            response.addProperty("success", true);
            response.addProperty("message", "Usuario creado exitosamente");
            response.add("data", gson.toJsonTree(creado));
            response.addProperty("status", 201);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Error al crear el usuario");
            response.addProperty("status", 500);
        }

        return response;
    }

    public static JsonObject validateAndUpdate(int id, String nombre, String correo,
                                               String contrasena, Boolean estado) {
        JsonObject response = new JsonObject();

        Usuario usuario = UsuarioDAO.findById(id);
        if (usuario == null) {
            response.addProperty("success", false);
            response.addProperty("message", "Usuario no encontrado");
            response.addProperty("status", 404);
            return response;
        }

        if ((nombre == null || nombre.isBlank()) && (correo == null || correo.isBlank())) {
            response.addProperty("success", false);
            response.addProperty("message", "Al menos nombre o correo deben ser proporcionados");
            response.addProperty("status", 400);
            return response;
        }

        if (correo != null && !correo.isBlank() && !correo.equals(usuario.getCorreo())) {
            if (UsuarioDAO.findByCorreo(correo) != null) {
                response.addProperty("success", false);
                response.addProperty("message", "El correo ya está en uso por otro usuario");
                response.addProperty("status", 409);
                return response;
            }
        }

        if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);
        if (correo != null && !correo.isBlank()) usuario.setCorreo(correo);
        if (estado != null) usuario.setEstado(estado);

        if (!UsuarioDAO.update(usuario)) {
            response.addProperty("success", false);
            response.addProperty("message", "Error al actualizar el usuario");
            response.addProperty("status", 500);
            return response;
        }

        if (contrasena != null && !contrasena.isBlank()) {
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        Usuario actualizado = UsuarioDAO.findById(id);
        actualizado.setContrasena(null);
        response.addProperty("success", true);
        response.addProperty("message", "Usuario actualizado exitosamente");
        response.add("data", gson.toJsonTree(actualizado));
        response.addProperty("status", 200);

        return response;
    }

    public static JsonObject deleteUser(int id) {
        JsonObject response = new JsonObject();

        if (UsuarioDAO.findById(id) == null) {
            response.addProperty("success", false);
            response.addProperty("message", "Usuario no encontrado");
            response.addProperty("status", 404);
            return response;
        }

        if (UsuarioDAO.delete(id)) {
            response.addProperty("success", true);
            response.addProperty("message", "Usuario eliminado exitosamente");
            response.addProperty("status", 200);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Error al eliminar el usuario");
            response.addProperty("status", 500);
        }

        return response;
    }
}
