package com.backend.middlewares;

import com.backend.helpers.JwtHelper;
import com.backend.server.http.ApiResponse;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

public class AuthMiddleware {

    public HttpHandler protect(HttpHandler next, String... allowedRoles) {
        return exchange -> {
            try {
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    ApiResponse.handleCors(exchange);
                    return;
                }

                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    ApiResponse.error(exchange, 401, "Token de autenticacion requerido");
                    return;
                }

                Claims claims = JwtHelper.validateToken(authHeader.substring(7));

                exchange.setAttribute("userId", claims.getSubject());
                exchange.setAttribute("correo", claims.get("correo", String.class));
                exchange.setAttribute("rol", claims.get("rol", String.class));

                // Si se especificaron roles, verificar que el usuario tenga uno permitido
                if (allowedRoles.length > 0) {
                    String userRol = claims.get("rol", String.class);
                    boolean authorized = false;
                    for (String rol : allowedRoles) {
                        if (rol.equalsIgnoreCase(userRol)) {
                            authorized = true;
                            break;
                        }
                    }
                    if (!authorized) {
                        ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                        return;
                    }
                }

                next.handle(exchange);

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                ApiResponse.error(exchange, 401, "Token expirado. Inicie sesion nuevamente");
            } catch (io.jsonwebtoken.JwtException e) {
                ApiResponse.error(exchange, 401, "Token invalido");
            } catch (Exception e) {
                System.err.println("Error en AuthMiddleware: " + e.getMessage());
                ApiResponse.error(exchange, 500, "Error interno del servidor");
            }
        };
    }
}
