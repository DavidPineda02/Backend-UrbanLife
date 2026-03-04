package com.backend.routes;

// Para enviar respuesta 404 cuando la ruta no existe y para manejar CORS
import com.backend.server.http.ApiResponse;
// Representa el intercambio HTTP (peticion + respuesta)
import com.sun.net.httpserver.HttpExchange;
// Interfaz que representa un manejador de peticiones HTTP
import com.sun.net.httpserver.HttpHandler;

// Para el manejo de excepciones de entrada/salida
import java.io.IOException;
// Para el mapa de rutas por metodo HTTP
import java.util.HashMap;
import java.util.Map;

/**
 * Dispatcher de rutas: mapea método HTTP + path a su handler correspondiente.
 * Implementa HttpHandler para ser registrado como contexto del servidor.
 */
public class Router implements HttpHandler {

    /** Estructura: método HTTP -> (path -> handler) */
    /** Ejemplo: "GET" -> { "/api/users" -> listAll, "/api/users/id" -> getById } */
    Map<String, Map<String, HttpHandler>> routes = new HashMap<>();

    /**
     * Registra un handler para un método HTTP y path específicos.
     * @param metodo Método HTTP (GET, POST, PUT, DELETE, etc.)
     * @param ruta Path de la ruta (ej: "/api/users")
     * @param handler Handler que procesará las peticiones a esta ruta
     */
    public void addRoute(String metodo, String ruta, HttpHandler handler) {
        // computeIfAbsent crea el mapa interno si el método no existe aún
        routes.computeIfAbsent(metodo, clave -> new HashMap<>()).put(ruta, handler);
    }

    /**
     * Registra una ruta GET.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones GET
     */
    public void get(String path, HttpHandler handler) {
        addRoute("GET", path, handler);
    }

    /**
     * Registra una ruta POST.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones POST
     */
    public void post(String path, HttpHandler handler) {
        addRoute("POST", path, handler);
    }

    /**
     * Registra una ruta PUT.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones PUT
     */
    public void put(String path, HttpHandler handler) {
        addRoute("PUT", path, handler);
    }

    /**
     * Registra una ruta PATCH.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones PATCH
     */
    public void patch(String path, HttpHandler handler) {
        addRoute("PATCH", path, handler);
    }

    /**
     * Registra una ruta DELETE.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones DELETE
     */
    public void delete(String path, HttpHandler handler) {
        addRoute("DELETE", path, handler);
    }

    /**
     * Método principal que maneja todas las peticiones HTTP.
     * Implementación de la interfaz HttpHandler.
     * @param exchange Intercambio HTTP con la petición y respuesta
     * @throws IOException Si hay error de E/S
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Obtener el metodo HTTP de la peticion (GET, POST, etc.)
        String metodo = exchange.getRequestMethod();

        // Responder peticiones OPTIONS (preflight CORS) sin buscar rutas
        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            ApiResponse.handleCors(exchange);
            return;
        }

        // Obtener solo el path sin query string (ej: /api/users sin ?id=5)
        String ruta = exchange.getRequestURI().getPath();
        // Buscar el mapa de rutas para el metodo HTTP recibido
        Map<String, HttpHandler> rutasDelMetodo = routes.get(metodo);

        if (rutasDelMetodo != null) {
            // Buscar el handler exacto para el path recibido
            HttpHandler handler = rutasDelMetodo.get(ruta);
            if (handler != null) {
                // Handler encontrado: delegar la ejecucion
                handler.handle(exchange);
                return;
            }
        }

        // No se encontro ninguna ruta que coincida con metodo + path
        ApiResponse.error(exchange, 404, "Ruta no encontrada");
    }
}
