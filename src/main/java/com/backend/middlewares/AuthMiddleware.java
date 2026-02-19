package com.backend.middlewares;

import com.backend.helpers.JwtHelper;
import com.backend.util.HttpResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

import java.io.IOException;

public class AuthMiddleware implements HttpHandler {

    private final HttpHandler next;

    public AuthMiddleware(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpResponseUtil.handleCors(exchange);
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                HttpResponseUtil.sendError(exchange, 401, "Token de autenticación requerido");
                return;
            }

            Claims claims = JwtHelper.validateToken(authHeader.substring(7));

            exchange.setAttribute("userId", claims.getSubject());
            exchange.setAttribute("correo", claims.get("correo", String.class));
            exchange.setAttribute("rol", claims.get("rol", String.class));

            next.handle(exchange);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            HttpResponseUtil.sendError(exchange, 401, "Token expirado. Inicie sesión nuevamente");
        } catch (io.jsonwebtoken.JwtException e) {
            HttpResponseUtil.sendError(exchange, 401, "Token inválido");
        } catch (Exception e) {
            System.out.println("Error en AuthMiddleware: " + e.getMessage());
            HttpResponseUtil.sendError(exchange, 500, "Error interno del servidor");
        }
    }
}
