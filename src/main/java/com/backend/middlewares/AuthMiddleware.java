// Paquete de middlewares de seguridad
package com.backend.middlewares;

// Para validar el JWT y extraer sus claims
import com.backend.helpers.JwtHelper;
// Para enviar respuestas de error estandarizadas
import com.backend.server.http.ApiResponse;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;
// Representa el payload decodificado del JWT (id, correo, rol, etc.)
import io.jsonwebtoken.Claims;

/**
 * Middleware de autenticación: protege rutas verificando el JWT y los roles del usuario.
 * Intercepta las peticiones y valida tokens antes de permitir el acceso a los endpoints.
 * Centraliza la seguridad y autorización del sistema.
 */
public class AuthMiddleware {

    /**
     * Envuelve un HttpHandler con verificación de autenticación y roles opcionales.
     * Crea una capa de seguridad alrededor de los endpoints protegidos.
     * @param next Handler a ejecutar si la autenticación es exitosa
     * @param rolesPermitidos Varargs de roles que pueden acceder (vacío = solo requiere token válido)
     * @return HttpHandler que incluye la verificación de autenticación
     */
    public HttpHandler protect(HttpHandler next, String... rolesPermitidos) {
        // Crear handler con middleware
        return exchange -> {
            // Bloque try para manejar excepciones
            try {
                // Responder peticiones OPTIONS (preflight CORS) sin requerir autenticacion
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Manejar CORS
                    ApiResponse.handleCors(exchange);
                    // Salir del handler
                    return;
                }

                // Leer el encabezado Authorization de la peticion
                String encabezadoAuth = exchange.getRequestHeaders().getFirst("Authorization");
                // Verificar que el encabezado exista y tenga el formato "Bearer <token>"
                if (encabezadoAuth == null || !encabezadoAuth.startsWith("Bearer ")) {
                    // Error 401
                    ApiResponse.error(exchange, 401, "Token de autenticacion requerido");
                    // Salir del handler
                    return;
                }

                // Extraer solo el token (eliminar el prefijo "Bearer ")
                // Validar la firma y expiracion del token con JwtHelper
                Claims datosToken = JwtHelper.validateToken(encabezadoAuth.substring(7));

                // Guardar los datos del usuario en atributos del exchange para los controllers
                exchange.setAttribute("userId", datosToken.getSubject());
                // Correo
                exchange.setAttribute("correo", datosToken.get("correo", String.class));
                // Rol del sistema
                exchange.setAttribute("rol", datosToken.get("rol", String.class));

                // Si se especificaron roles permitidos, verificar que el usuario tenga uno de ellos
                if (rolesPermitidos.length > 0) {
                    // Obtener el rol del usuario desde el token ya validado
                    String rolUsuario = datosToken.get("rol", String.class);
                    // Bandera de autorización
                    boolean autorizado = false;
                    // Recorrer los roles permitidos y verificar si el usuario tiene alguno
                    for (String rol : rolesPermitidos) {
                        // Comparar roles
                        if (rol.equalsIgnoreCase(rolUsuario)) {
                            // El usuario tiene uno de los roles permitidos
                            autorizado = true;
                            // Salir del bucle
                            break;
                        }
                    }
                    // Si el usuario no tiene ningun rol permitido, denegar con 403
                    if (!autorizado) {
                        // Error 403
                        ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                        // Salir del handler
                        return;
                    }
                }

                // Autenticacion y autorizacion exitosas: pasar la peticion al handler real
                next.handle(exchange);

            // Capturar token expirado
            } catch (io.jsonwebtoken.ExpiredJwtException excepcion) {
                // El token es valido pero ya expiro (24 horas)
                ApiResponse.error(exchange, 401, "Token expirado. Inicie sesion nuevamente");
            // Capturar token inválido
            } catch (io.jsonwebtoken.JwtException excepcion) {
                // El token tiene firma incorrecta o formato invalido
                ApiResponse.error(exchange, 401, "Token invalido");
            // Capturar errores generales
            } catch (Exception excepcion) {
                // Error inesperado en el middleware
                System.err.println("Error en AuthMiddleware: " + excepcion.getMessage());
                // Error 500
                ApiResponse.error(exchange, 500, "Error interno del servidor");
            }
        // Retornar handler con middleware
        };
    }
}
