// Paquete de servidor HTTP
package com.backend.server;

// Importar la clase que registra todas las rutas de la API
import com.backend.routes.Routes;
// Interfaz que representa un manejador de peticiones HTTP
import com.sun.net.httpserver.HttpHandler;
// Clase del servidor HTTP nativo de Java (com.sun.net.httpserver)
import com.sun.net.httpserver.HttpServer;

// Para el manejo de excepciones al crear el servidor
import java.io.IOException;
// Para vincular el servidor a una direccion IP y puerto especificos
import java.net.InetSocketAddress;

/**
 * Clase responsable de crear, iniciar y detener el servidor HTTP.
 * Gestiona el ciclo de vida del servidor y el registro de rutas.
 * Centraliza la configuración y control del servidor web.
 */
public class serverConnection {

    /** Referencia estática al servidor para poder detenerlo desde stopServer() */
    private static HttpServer server;

    /**
     * Crea e inicia el servidor HTTP escuchando en el puerto indicado.
     * Configura el servidor con las rutas de la API y lo pone en marcha.
     * @param port Puerto en el que escuchará el servidor
     */
    public static void startServer(int port) {
        // Bloque try para manejar excepciones
        try {
            // Log de inicio
            System.out.println("\nIniciando servidor...");

            // Crear el servidor HTTP enlazado al puerto indicado (0 = cola ilimitada de conexiones)
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Instanciar Routes y registrar todas las rutas de la API
            Routes routes = new Routes();
            // Configurar todas las rutas
            HttpHandler router = routes.configureRoutes();

            // Registrar el manejador de archivos estáticos ANTES del router principal
            // Sirve imágenes desde /uploads/ con el Content-Type correcto
            server.createContext("/uploads/", new StaticFileHandler());

            // Registrar el router como manejador de TODAS las peticiones entrantes
            server.createContext("/", router);

            // Usar el executor por defecto (hilo por peticion sin pool personalizado)
            server.setExecutor(null);
            // Iniciar el servidor para comenzar a aceptar conexiones
            server.start();

            // Log de éxito
            System.out.println("\nUrbanLife Backend corriendo en: http://localhost:" + port + "\n");
        // Capturar errores de IO
        } catch (IOException excepcion) {
            // Capturar errores al crear o iniciar el servidor (ej: puerto ya en uso)
            System.out.println("Error al iniciar el servidor: " + excepcion.getMessage());
        }
    }

    /**
     * Detiene el servidor de forma ordenada.
     * Espera 0 segundos antes de forzar el cierre.
     */
    public static void stopServer() {
        // Verificar que el servidor fue iniciado antes de intentar detenerlo
        if (server != null) {
            // Detener servidor inmediatamente
            server.stop(0);
            // Log de detención
            System.out.println("Servidor detenido.");
        }
    }
}
