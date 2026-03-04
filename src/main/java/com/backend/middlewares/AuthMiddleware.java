package com.backend.middlewares; // Paquete de middlewares de seguridad

// Para validar el JWT y extraer sus claims
import com.backend.helpers.JwtHelper; // Helper para validación de tokens JWT
// Para enviar respuestas de error estandarizadas
import com.backend.server.http.ApiResponse; // Clase para respuestas HTTP
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler; // Interfaz para manejar peticiones HTTP
// Representa el payload decodificado del JWT (id, correo, rol, etc.)
import io.jsonwebtoken.Claims; // Clase para claims del token JWT

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
    public HttpHandler protect(HttpHandler next, String... rolesPermitidos) { // Método de protección
        return exchange -> { // Crear handler con middleware
            try { // Bloque try para manejar excepciones
                // Responder peticiones OPTIONS (preflight CORS) sin requerir autenticacion
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { // Validar método OPTIONS
                    ApiResponse.handleCors(exchange); // Manejar CORS
                    return; // Salir del handler
                }

                // Leer el encabezado Authorization de la peticion
                String encabezadoAuth = exchange.getRequestHeaders().getFirst("Authorization"); // Obtener header
                // Verificar que el encabezado exista y tenga el formato "Bearer <token>"
                if (encabezadoAuth == null || !encabezadoAuth.startsWith("Bearer ")) { // Validar formato
                    ApiResponse.error(exchange, 401, "Token de autenticacion requerido"); // Error 401
                    return; // Salir del handler
                }

                // Extraer solo el token (eliminar el prefijo "Bearer ")
                // Validar la firma y expiracion del token con JwtHelper
                Claims datosToken = JwtHelper.validateToken(encabezadoAuth.substring(7)); // Validar token

                // Guardar los datos del usuario en atributos del exchange para los controllers
                exchange.setAttribute("userId", datosToken.getSubject());                     // ID del usuario
                exchange.setAttribute("correo", datosToken.get("correo", String.class));      // Correo
                exchange.setAttribute("rol", datosToken.get("rol", String.class));            // Rol del sistema

                // Si se especificaron roles permitidos, verificar que el usuario tenga uno de ellos
                if (rolesPermitidos.length > 0) { // Validar si hay roles específicos
                    // Obtener el rol del usuario desde el token ya validado
                    String rolUsuario = datosToken.get("rol", String.class); // Extraer rol del token
                    boolean autorizado = false; // Bandera de autorización
                    // Recorrer los roles permitidos y verificar si el usuario tiene alguno
                    for (String rol : rolesPermitidos) { // Iterar roles permitidos
                        if (rol.equalsIgnoreCase(rolUsuario)) { // Comparar roles
                            autorizado = true; // El usuario tiene uno de los roles permitidos
                            break; // Salir del bucle
                        }
                    }
                    // Si el usuario no tiene ningun rol permitido, denegar con 403
                    if (!autorizado) { // Validar autorización
                        ApiResponse.error(exchange, 403, "No tiene permiso para esta accion"); // Error 403
                        return; // Salir del handler
                    }
                }

                // Autenticacion y autorizacion exitosas: pasar la peticion al handler real
                next.handle(exchange); // Continuar con el siguiente handler

            } catch (io.jsonwebtoken.ExpiredJwtException excepcion) { // Capturar token expirado
                // El token es valido pero ya expiro (24 horas)
                ApiResponse.error(exchange, 401, "Token expirado. Inicie sesion nuevamente"); // Error 401
            } catch (io.jsonwebtoken.JwtException excepcion) { // Capturar token inválido
                // El token tiene firma incorrecta o formato invalido
                ApiResponse.error(exchange, 401, "Token invalido"); // Error 401
            } catch (Exception excepcion) { // Capturar errores generales
                // Error inesperado en el middleware
                System.err.println("Error en AuthMiddleware: " + excepcion.getMessage()); // Log de error
                ApiResponse.error(exchange, 500, "Error interno del servidor"); // Error 500
            }
        }; // Retornar handler con middleware
    }
}
