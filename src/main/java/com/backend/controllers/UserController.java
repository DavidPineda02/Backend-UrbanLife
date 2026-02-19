package com.backend.controllers;

import com.backend.dto.CreateUserRequest;
import com.backend.dto.UpdateUserRequest;
import com.backend.helpers.JsonHelper;
import com.backend.models.Usuario;
import com.backend.services.UserService;
import com.backend.util.HttpResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserController implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                HttpResponseUtil.handleCors(exchange);
                return;
            }

            Integer id = extractId(exchange.getRequestURI().getPath());

            switch (method.toUpperCase()) {
                case "GET"    -> handleGet(exchange, id);
                case "POST"   -> handlePost(exchange);
                case "PUT"    -> handlePut(exchange, id);
                case "DELETE" -> handleDelete(exchange, id);
                default -> HttpResponseUtil.sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            System.out.println("Error en UserController: " + e.getMessage());
            HttpResponseUtil.sendError(exchange, 500, "Error interno del servidor");
        }
    }

    private void handleGet(HttpExchange exchange, Integer id) throws IOException {
        if (id != null) {
            Usuario u = userService.getById(id);
            if (u == null) {
                HttpResponseUtil.sendError(exchange, 404, "Usuario no encontrado");
                return;
            }
            u.setContrasena(null);
            HttpResponseUtil.sendJson(exchange, 200, Map.of("success", true, "data", u));
        } else {
            List<Usuario> lista = userService.getAll();
            lista.forEach(u -> u.setContrasena(null));
            HttpResponseUtil.sendJson(exchange, 200, Map.of("success", true, "data", lista));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            CreateUserRequest req = JsonHelper.fromJson(exchange.getRequestBody(), CreateUserRequest.class);
            Usuario creado = userService.create(req);
            if (creado != null) {
                creado.setContrasena(null);
                HttpResponseUtil.sendJson(exchange, 201, Map.of(
                        "success", true, "message", "Usuario creado exitosamente", "data", creado));
            } else {
                HttpResponseUtil.sendError(exchange, 500, "Error al crear el usuario");
            }
        } catch (IllegalArgumentException e) {
            HttpResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    private void handlePut(HttpExchange exchange, Integer id) throws IOException {
        if (id == null) {
            HttpResponseUtil.sendError(exchange, 400, "ID de usuario requerido en la URL");
            return;
        }
        try {
            UpdateUserRequest req = JsonHelper.fromJson(exchange.getRequestBody(), UpdateUserRequest.class);
            Usuario actualizado = userService.update(id, req);
            actualizado.setContrasena(null);
            HttpResponseUtil.sendJson(exchange, 200, Map.of(
                    "success", true, "message", "Usuario actualizado exitosamente", "data", actualizado));
        } catch (IllegalArgumentException e) {
            HttpResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, Integer id) throws IOException {
        if (id == null) {
            HttpResponseUtil.sendError(exchange, 400, "ID de usuario requerido en la URL");
            return;
        }
        try {
            if (userService.delete(id)) {
                HttpResponseUtil.sendJson(exchange, 200, Map.of(
                        "success", true, "message", "Usuario eliminado exitosamente"));
            } else {
                HttpResponseUtil.sendError(exchange, 500, "Error al eliminar el usuario");
            }
        } catch (IllegalArgumentException e) {
            HttpResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    private Integer extractId(String path) {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            try { return Integer.parseInt(parts[3]); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
