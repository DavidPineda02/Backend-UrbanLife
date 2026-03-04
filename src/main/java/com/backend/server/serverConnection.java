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

// Clase responsable de crear, iniciar y detener el servidor HTTP
public class serverConnection {

    // Referencia estatica al servidor para poder detenerlo desde stopServer()
    private static HttpServer server;

    // Crea e inicia el servidor HTTP escuchando en el puerto indicado
    public static void startServer(int port) {
        try {
            System.out.println("\nIniciando servidor...");

            // Crear el servidor HTTP enlazado al puerto indicado (0 = cola ilimitada de conexiones)
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Instanciar Routes y registrar todas las rutas de la API
            Routes routes = new Routes();
            HttpHandler router = routes.configureRoutes();

            // Registrar el router como manejador de TODAS las peticiones entrantes
            server.createContext("/", router);

            // Usar el executor por defecto (hilo por peticion sin pool personalizado)
            server.setExecutor(null);
            // Iniciar el servidor para comenzar a aceptar conexiones
            server.start();

            System.out.println("\nUrbanLife Backend corriendo en: http://localhost:" + port + "\n");
        } catch (IOException excepcion) {
            // Capturar errores al crear o iniciar el servidor (ej: puerto ya en uso)
            System.out.println("Error al iniciar el servidor: " + excepcion.getMessage());
        }
    }

    // Detiene el servidor de forma ordenada (espera 0 segundos antes de forzar el cierre)
    public static void stopServer() {
        // Verificar que el servidor fue iniciado antes de intentar detenerlo
        if (server != null) {
            server.stop(0);
            System.out.println("Servidor detenido.");
        }
    }
}
