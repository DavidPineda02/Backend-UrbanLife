package com.backend.middlewares;

import com.backend.helpers.JwtHelper;
import com.backend.server.http.ApiResponse;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

public class AuthMiddleware {

    public HttpHandler protect(HttpHandler next, String... rolesPermitidos) {
        return exchange -> {
            try {
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    ApiResponse.handleCors(exchange);
                    return;
                }

                String encabezadoAuth = exchange.getRequestHeaders().getFirst("Authorization");
                if (encabezadoAuth == null || !encabezadoAuth.startsWith("Bearer ")) {
                    ApiResponse.error(exchange, 401, "Token de autenticacion requerido");
                    return;
                }

                Claims datosToken = JwtHelper.validateToken(encabezadoAuth.substring(7));

                exchange.setAttribute("userId", datosToken.getSubject());
                exchange.setAttribute("correo", datosToken.get("correo", String.class));
                exchange.setAttribute("rol", datosToken.get("rol", String.class));

                // Si se especificaron roles, verificar que el usuario tenga uno permitido
                if (rolesPermitidos.length > 0) {
                    String rolUsuario = datosToken.get("rol", String.class);
                    boolean autorizado = false;
                    for (String rol : rolesPermitidos) {
                        if (rol.equalsIgnoreCase(rolUsuario)) {
                            autorizado = true;
                            break;
                        }
                    }
                    if (!autorizado) {
                        ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                        return;
                    }
                }

                next.handle(exchange);

            } catch (io.jsonwebtoken.ExpiredJwtException excepcion) {
                ApiResponse.error(exchange, 401, "Token expirado. Inicie sesion nuevamente");
            } catch (io.jsonwebtoken.JwtException excepcion) {
                ApiResponse.error(exchange, 401, "Token invalido");
            } catch (Exception excepcion) {
                System.err.println("Error en AuthMiddleware: " + excepcion.getMessage());
                ApiResponse.error(exchange, 500, "Error interno del servidor");
            }
        };
    }
}
