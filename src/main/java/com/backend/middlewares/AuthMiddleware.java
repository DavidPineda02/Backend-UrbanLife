package com.backend.middlewares;

// Para validar el JWT y extraer sus claims
import com.backend.helpers.JwtHelper;
// Para enviar respuestas de error estandarizadas
import com.backend.server.http.ApiResponse;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;
// Representa el payload decodificado del JWT (id, correo, rol, etc.)
import io.jsonwebtoken.Claims;

// Middleware de autenticacion: protege rutas verificando el JWT y los roles del usuario
public class AuthMiddleware {

    // Envuelve un HttpHandler con verificacion de autenticacion y roles opcionales
    // next: handler a ejecutar si la autenticacion es exitosa
    // rolesPermitidos: varargs de roles que pueden acceder (vacio = solo requiere token valido)
    public HttpHandler protect(HttpHandler next, String... rolesPermitidos) {
        return exchange -> {
            try {
                // Responder peticiones OPTIONS (preflight CORS) sin requerir autenticacion
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    ApiResponse.handleCors(exchange);
                    return;
                }

                // Leer el encabezado Authorization de la peticion
                String encabezadoAuth = exchange.getRequestHeaders().getFirst("Authorization");
                // Verificar que el encabezado exista y tenga el formato "Bearer <token>"
                if (encabezadoAuth == null || !encabezadoAuth.startsWith("Bearer ")) {
                    ApiResponse.error(exchange, 401, "Token de autenticacion requerido");
                    return;
                }

                // Extraer solo el token (eliminar el prefijo "Bearer ")
                // Validar la firma y expiracion del token con JwtHelper
                Claims datosToken = JwtHelper.validateToken(encabezadoAuth.substring(7));

                // Guardar los datos del usuario en atributos del exchange para los controllers
                exchange.setAttribute("userId", datosToken.getSubject());                     // ID del usuario
                exchange.setAttribute("correo", datosToken.get("correo", String.class));      // Correo
                exchange.setAttribute("rol", datosToken.get("rol", String.class));            // Rol del sistema

                // Si se especificaron roles permitidos, verificar que el usuario tenga uno de ellos
                if (rolesPermitidos.length > 0) {
                    // Obtener el rol del usuario desde el token ya validado
                    String rolUsuario = datosToken.get("rol", String.class);
                    boolean autorizado = false;
                    // Recorrer los roles permitidos y verificar si el usuario tiene alguno
                    for (String rol : rolesPermitidos) {
                        if (rol.equalsIgnoreCase(rolUsuario)) {
                            autorizado = true; // El usuario tiene uno de los roles permitidos
                            break;
                        }
                    }
                    // Si el usuario no tiene ningun rol permitido, denegar con 403
                    if (!autorizado) {
                        ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                        return;
                    }
                }

                // Autenticacion y autorizacion exitosas: pasar la peticion al handler real
                next.handle(exchange);

            } catch (io.jsonwebtoken.ExpiredJwtException excepcion) {
                // El token es valido pero ya expiro (24 horas)
                ApiResponse.error(exchange, 401, "Token expirado. Inicie sesion nuevamente");
            } catch (io.jsonwebtoken.JwtException excepcion) {
                // El token tiene firma incorrecta o formato invalido
                ApiResponse.error(exchange, 401, "Token invalido");
            } catch (Exception excepcion) {
                // Error inesperado en el middleware
                System.err.println("Error en AuthMiddleware: " + excepcion.getMessage());
                ApiResponse.error(exchange, 500, "Error interno del servidor");
            }
        };
    }
}
