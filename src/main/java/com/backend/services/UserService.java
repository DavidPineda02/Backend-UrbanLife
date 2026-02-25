package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class UserService {

    private static final Gson gson = new Gson();

    public static JsonObject validateAndCreate(String nombre, String correo, String contrasena, boolean estado) {
        JsonObject respuesta = new JsonObject();

        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Nombre, correo y contraseña son requeridos");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        if (UsuarioDAO.findByCorreo(correo) != null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El correo ya está registrado");
            respuesta.addProperty("status", 409);
            return respuesta;
        }

        Usuario nuevoUsuario = new Usuario(nombre, correo,
                PasswordHelper.hashPassword(contrasena), estado);

        Usuario usuarioCreado = UsuarioDAO.create(nuevoUsuario);

        if (usuarioCreado != null) {
            usuarioCreado.setContrasena(null);
            respuesta.addProperty("success", true);
            respuesta.addProperty("message", "Usuario creado exitosamente");
            respuesta.add("data", gson.toJsonTree(usuarioCreado));
            respuesta.addProperty("status", 201);
        } else {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al crear el usuario");
            respuesta.addProperty("status", 500);
        }

        return respuesta;
    }

    // PUT - Reemplazo completo (nombre y correo obligatorios)
    public static JsonObject validateAndUpdate(int id, String nombre, String correo, String contrasena, boolean estado) {
        JsonObject respuesta = new JsonObject();

        Usuario usuario = UsuarioDAO.findById(id);
        if (usuario == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario no encontrado");
            respuesta.addProperty("status", 404);
            return respuesta;
        }

        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Nombre y correo son obligatorios en PUT");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        if (!correo.equals(usuario.getCorreo())) {
            if (UsuarioDAO.findByCorreo(correo) != null) {
                respuesta.addProperty("success", false);
                respuesta.addProperty("message", "El correo ya está en uso por otro usuario");
                respuesta.addProperty("status", 409);
                return respuesta;
            }
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setEstado(estado);

        if (!UsuarioDAO.update(usuario)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al actualizar el usuario");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        if (contrasena != null && !contrasena.isBlank()) {
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        Usuario usuarioActualizado = UsuarioDAO.findById(id);
        usuarioActualizado.setContrasena(null);
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Usuario actualizado exitosamente");
        respuesta.add("data", gson.toJsonTree(usuarioActualizado));
        respuesta.addProperty("status", 200);

        return respuesta;
    }

    // PATCH - Actualizacion parcial (solo los campos enviados)
    public static JsonObject partialUpdate(int id, String nombre, String correo, String contrasena, Boolean estado) {
        JsonObject respuesta = new JsonObject();

        Usuario usuario = UsuarioDAO.findById(id);
        if (usuario == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Usuario no encontrado");
            respuesta.addProperty("status", 404);
            return respuesta;
        }

        if (correo != null && !correo.isBlank() && !correo.equals(usuario.getCorreo())) {
            if (UsuarioDAO.findByCorreo(correo) != null) {
                respuesta.addProperty("success", false);
                respuesta.addProperty("message", "El correo ya está en uso por otro usuario");
                respuesta.addProperty("status", 409);
                return respuesta;
            }
        }

        if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);
        if (correo != null && !correo.isBlank()) usuario.setCorreo(correo);
        if (estado != null) usuario.setEstado(estado);

        if (!UsuarioDAO.update(usuario)) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al actualizar el usuario");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        if (contrasena != null && !contrasena.isBlank()) {
            UsuarioDAO.updatePassword(id, PasswordHelper.hashPassword(contrasena));
        }

        Usuario usuarioActualizado = UsuarioDAO.findById(id);
        usuarioActualizado.setContrasena(null);
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Usuario actualizado parcialmente");
        respuesta.add("data", gson.toJsonTree(usuarioActualizado));
        respuesta.addProperty("status", 200);

        return respuesta;
    }

    // public static JsonObject deleteUser(int id) {
    //     JsonObject respuesta = new JsonObject();
    //
    //     if (UsuarioDAO.findById(id) == null) {
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "Usuario no encontrado");
    //         respuesta.addProperty("status", 404);
    //         return respuesta;
    //     }
    //
    //     if (UsuarioDAO.delete(id)) {
    //         respuesta.addProperty("success", true);
    //         respuesta.addProperty("message", "Usuario eliminado exitosamente");
    //         respuesta.addProperty("status", 200);
    //     } else {
    //         respuesta.addProperty("success", false);
    //         respuesta.addProperty("message", "Error al eliminar el usuario");
    //         respuesta.addProperty("status", 500);
    //     }
    //
    //     return respuesta;
    // }
}
