package com.backend.server; // Paquete de servidor HTTP

// Importar la clase que registra todas las rutas de la API
import com.backend.routes.Routes; // Clase para configuración de rutas
// Interfaz que representa un manejador de peticiones HTTP
import com.sun.net.httpserver.HttpHandler; // Interfaz para handlers HTTP
// Clase del servidor HTTP nativo de Java (com.sun.net.httpserver)
import com.sun.net.httpserver.HttpServer; // Clase para servidor HTTP

// Para el manejo de excepciones al crear el servidor
import java.io.IOException; // Clase para excepciones IO
// Para vincular el servidor a una direccion IP y puerto especificos
import java.net.InetSocketAddress; // Clase para dirección de red

/**
 * Clase responsable de crear, iniciar y detener el servidor HTTP.
 * Gestiona el ciclo de vida del servidor y el registro de rutas.
 * Centraliza la configuración y control del servidor web.
 */
public class serverConnection {

    /** Referencia estática al servidor para poder detenerlo desde stopServer() */
    private static HttpServer server; // Campo estático para instancia del servidor

    /**
     * Crea e inicia el servidor HTTP escuchando en el puerto indicado.
     * Configura el servidor con las rutas de la API y lo pone en marcha.
     * @param port Puerto en el que escuchará el servidor
     */
    public static void startServer(int port) { // Método para iniciar servidor
        try { // Bloque try para manejar excepciones
            System.out.println("\nIniciando servidor..."); // Log de inicio

            // Crear el servidor HTTP enlazado al puerto indicado (0 = cola ilimitada de conexiones)
            server = HttpServer.create(new InetSocketAddress(port), 0); // Crear instancia del servidor

            // Instanciar Routes y registrar todas las rutas de la API
            Routes routes = new Routes(); // Crear objeto de rutas
            HttpHandler router = routes.configureRoutes(); // Configurar todas las rutas

            // Registrar el router como manejador de TODAS las peticiones entrantes
            server.createContext("/", router); // Registrar handler principal

            // Usar el executor por defecto (hilo por peticion sin pool personalizado)
            server.setExecutor(null); // Configurar executor por defecto
            // Iniciar el servidor para comenzar a aceptar conexiones
            server.start(); // Iniciar servidor

            System.out.println("\nUrbanLife Backend corriendo en: http://localhost:" + port + "\n"); // Log de éxito
        } catch (IOException excepcion) { // Capturar errores de IO
            // Capturar errores al crear o iniciar el servidor (ej: puerto ya en uso)
            System.out.println("Error al iniciar el servidor: " + excepcion.getMessage()); // Log de error
        }
    }

    /**
     * Detiene el servidor de forma ordenada.
     * Espera 0 segundos antes de forzar el cierre.
     */
    public static void stopServer() { // Método para detener servidor
        // Verificar que el servidor fue iniciado antes de intentar detenerlo
        if (server != null) { // Validar que exista instancia
            server.stop(0); // Detener servidor inmediatamente
            System.out.println("Servidor detenido."); // Log de detención
        }
    }
}
