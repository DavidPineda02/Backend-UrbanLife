package com.backend.routes; // Paquete de rutas de la API

// Para enviar respuesta 404 cuando la ruta no existe y para manejar CORS
import com.backend.server.http.ApiResponse; // Clase para respuestas HTTP
// Representa el intercambio HTTP (peticion + respuesta)
import com.sun.net.httpserver.HttpExchange; // Clase para intercambio HTTP
// Interfaz que representa un manejador de peticiones HTTP
import com.sun.net.httpserver.HttpHandler; // Interfaz para handlers HTTP

// Para el manejo de excepciones de entrada/salida
import java.io.IOException; // Clase para excepciones IO
// Para el mapa de rutas por metodo HTTP
import java.util.HashMap; // Clase para mapas hash
import java.util.Map; // Interfaz para mapas

/**
 * Dispatcher de rutas: mapea método HTTP + path a su handler correspondiente.
 * Implementa HttpHandler para ser registrado como contexto del servidor.
 * Proporciona un sistema de enrutamiento flexible y extensible.
 */
public class Router implements HttpHandler {

    /** Estructura: método HTTP -> (path -> handler) */
    /** Ejemplo: "GET" -> { "/api/users" -> listAll, "/api/users/id" -> getById } */
    Map<String, Map<String, HttpHandler>> routes = new HashMap<>(); // Mapa anidado de rutas

    /**
     * Registra un handler para un método HTTP y path específicos.
     * Almacena la ruta en la estructura de datos interna.
     * @param metodo Método HTTP (GET, POST, PUT, DELETE, etc.)
     * @param ruta Path de la ruta (ej: "/api/users")
     * @param handler Handler que procesará las peticiones a esta ruta
     */
    public void addRoute(String metodo, String ruta, HttpHandler handler) { // Método para agregar ruta
        // computeIfAbsent crea el mapa interno si el método no existe aún
        routes.computeIfAbsent(metodo, clave -> new HashMap<>()).put(ruta, handler); // Agregar ruta al mapa
    }

    /**
     * Registra una ruta GET.
     * Método conveniente para rutas de tipo GET.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones GET
     */
    public void get(String path, HttpHandler handler) { // Método para GET
        addRoute("GET", path, handler); // Delegar a addRoute
    }

    /**
     * Registra una ruta POST.
     * Método conveniente para rutas de tipo POST.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones POST
     */
    public void post(String path, HttpHandler handler) { // Método para POST
        addRoute("POST", path, handler); // Delegar a addRoute
    }

    /**
     * Registra una ruta PUT.
     * Método conveniente para rutas de tipo PUT.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones PUT
     */
    public void put(String path, HttpHandler handler) { // Método para PUT
        addRoute("PUT", path, handler); // Delegar a addRoute
    }

    /**
     * Registra una ruta PATCH.
     * Método conveniente para rutas de tipo PATCH.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones PATCH
     */
    public void patch(String path, HttpHandler handler) { // Método para PATCH
        addRoute("PATCH", path, handler); // Delegar a addRoute
    }

    /**
     * Registra una ruta DELETE.
     * Método conveniente para rutas de tipo DELETE.
     * @param path Path de la ruta
     * @param handler Handler para procesar peticiones DELETE
     */
    public void delete(String path, HttpHandler handler) { // Método para DELETE
        addRoute("DELETE", path, handler); // Delegar a addRoute
    }

    /**
     * Método principal que maneja todas las peticiones HTTP.
     * Implementación de la interfaz HttpHandler.
     * @param exchange Intercambio HTTP con la petición y respuesta
     * @throws IOException Si hay error de E/S
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException { // Método principal del router
        // Obtener el metodo HTTP de la peticion (GET, POST, etc.)
        String metodo = exchange.getRequestMethod(); // Extraer método HTTP

        // Responder peticiones OPTIONS (preflight CORS) sin buscar rutas
        if ("OPTIONS".equalsIgnoreCase(metodo)) { // Validar método OPTIONS
            ApiResponse.handleCors(exchange); // Manejar CORS
            return; // Salir del handler
        }

        // Obtener solo el path sin query string (ej: /api/users sin ?id=5)
        String ruta = exchange.getRequestURI().getPath(); // Extraer path de la URI
        // Buscar el mapa de rutas para el metodo HTTP recibido
        Map<String, HttpHandler> rutasDelMetodo = routes.get(metodo); // Obtener rutas del método

        if (rutasDelMetodo != null) { // Validar que existan rutas para el método
            // Buscar el handler exacto para el path recibido
            HttpHandler handler = rutasDelMetodo.get(ruta); // Buscar handler específico
            if (handler != null) { // Validar que exista el handler
                // Handler encontrado: delegar la ejecucion
                handler.handle(exchange); // Ejecutar handler
                return; // Salir del método
            }
        }

        // No se encontro ninguna ruta que coincida con metodo + path
        ApiResponse.error(exchange, 404, "Ruta no encontrada"); // Enviar error 404
    }
}
