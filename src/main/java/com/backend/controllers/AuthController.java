package com.backend.controllers;

import com.backend.dto.LoginRequest;
import com.backend.dto.LoginResponse;
import com.backend.helpers.JsonHelper;
import com.backend.services.AuthService;
import com.backend.util.HttpResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class AuthController implements HttpHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpResponseUtil.handleCors(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpResponseUtil.sendError(exchange, 405, "Método no permitido. Use POST");
                return;
            }

            LoginRequest request = JsonHelper.fromJson(exchange.getRequestBody(), LoginRequest.class);
            LoginResponse response = authService.login(request);

            HttpResponseUtil.sendJson(exchange, response.isSuccess() ? 200 : 401, response);

        } catch (Exception e) {
            System.out.println("Error en AuthController: " + e.getMessage());
            HttpResponseUtil.sendError(exchange, 500, "Error interno del servidor");
        }
    }
}
