package com.backend.controllers;

import com.backend.dao.UsuarioDAO;
import com.backend.models.Usuario;
import com.backend.server.http.ApiRequest;
import com.backend.server.http.ApiResponse;
import com.backend.services.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;

import java.util.List;
import java.util.Map;

public class UserController {

    public static HttpHandler listAll() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users");

            List<Usuario> lista = UsuarioDAO.findAll();
            lista.forEach(u -> u.setContrasena(null));
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
        };
    }

    public static HttpHandler getById() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(query.split("=")[1]);

            Usuario u = UsuarioDAO.findById(id);
            if (u == null) {
                ApiResponse.error(exchange, 404, "Usuario no encontrado");
                return;
            }
            u.setContrasena(null);
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", u));
        };
    }

    public static HttpHandler create() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users");

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            String nombre = json.has("nombre") ? json.get("nombre").getAsString() : "";
            String correo = json.has("correo") ? json.get("correo").getAsString() : "";
            String contrasena = json.has("contrasena") ? json.get("contrasena").getAsString() : "";
            boolean estado = json.has("estado") ? json.get("estado").getAsBoolean() : true;

            JsonObject response = UserService.validateAndCreate(nombre, correo, contrasena, estado);
            int code = response.get("status").getAsInt();
            response.remove("status");

            ApiResponse.send(exchange, response.toString(), code);
        };
    }

    public static HttpHandler update() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(query.split("=")[1]);

            ApiRequest request = new ApiRequest(exchange);
            String body = request.readBody();

            if (body.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            String nombre = json.has("nombre") ? json.get("nombre").getAsString() : "";
            String correo = json.has("correo") ? json.get("correo").getAsString() : "";
            String contrasena = json.has("contrasena") ? json.get("contrasena").getAsString() : "";
            Boolean estado = json.has("estado") ? json.get("estado").getAsBoolean() : null;

            JsonObject response = UserService.validateAndUpdate(id, nombre, correo, contrasena, estado);
            int code = response.get("status").getAsInt();
            response.remove("status");

            ApiResponse.send(exchange, response.toString(), code);
        };
    }

    public static HttpHandler delete() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(query.split("=")[1]);

            JsonObject response = UserService.deleteUser(id);
            int code = response.get("status").getAsInt();
            response.remove("status");

            ApiResponse.send(exchange, response.toString(), code);
        };
    }
}
