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
            lista.forEach(usuario -> usuario.setContrasena(null));
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
        };
    }

    public static HttpHandler getById() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            String parametrosUrl = exchange.getRequestURI().getQuery();
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            Usuario usuario = UsuarioDAO.findById(id);
            if (usuario == null) {
                ApiResponse.error(exchange, 404, "Usuario no encontrado");
                return;
            }
            usuario.setContrasena(null);
            ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", usuario));
        };
    }

    public static HttpHandler create() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users");

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class);

            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : "";
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";

            JsonObject respuesta = UserService.validateAndCreate(nombre, correo, contrasena);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    public static HttpHandler update() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            String parametrosUrl = exchange.getRequestURI().getQuery();
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class);

            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : "";
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : "";
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : "";
            boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : true;

            JsonObject respuesta = UserService.validateAndUpdate(id, nombre, correo, contrasena, estado);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    public static HttpHandler patch() {
        return exchange -> {
            System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");

            String parametrosUrl = exchange.getRequestURI().getQuery();
            if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
                ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
                return;
            }
            int id = Integer.parseInt(parametrosUrl.split("=")[1]);

            ApiRequest peticion = new ApiRequest(exchange);
            String cuerpo = peticion.readBody();

            if (cuerpo.isEmpty()) {
                ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
                return;
            }

            Gson gson = new Gson();
            JsonObject datosJson = gson.fromJson(cuerpo, JsonObject.class);

            String nombre = datosJson.has("nombre") ? datosJson.get("nombre").getAsString() : null;
            String correo = datosJson.has("correo") ? datosJson.get("correo").getAsString() : null;
            String contrasena = datosJson.has("contrasena") ? datosJson.get("contrasena").getAsString() : null;
            Boolean estado = datosJson.has("estado") ? datosJson.get("estado").getAsBoolean() : null;

            JsonObject respuesta = UserService.partialUpdate(id, nombre, correo, contrasena, estado);
            int codigoHttp = respuesta.get("status").getAsInt();
            respuesta.remove("status");

            ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
        };
    }

    // public static HttpHandler delete() {
    //     return exchange -> {
    //         System.out.println("Peticion: " + exchange.getRequestMethod() + " /api/users/id");
    //
    //         String parametrosUrl = exchange.getRequestURI().getQuery();
    //         if (parametrosUrl == null || !parametrosUrl.matches("id=\\d+")) {
    //             ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
    //             return;
    //         }
    //         int id = Integer.parseInt(parametrosUrl.split("=")[1]);
    //
    //         JsonObject respuesta = UserService.deleteUser(id);
    //         int codigoHttp = respuesta.get("status").getAsInt();
    //         respuesta.remove("status");
    //
    //         ApiResponse.send(exchange, respuesta.toString(), codigoHttp);
    //     };
    // }
}
