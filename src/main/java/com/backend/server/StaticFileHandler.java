// Paquete del servidor HTTP
package com.backend.server;

// Contexto HTTP de la petición entrante
import com.sun.net.httpserver.HttpExchange;
// Interfaz del manejador HTTP
import com.sun.net.httpserver.HttpHandler;

// Para leer bytes del archivo
import java.io.IOException;
import java.io.OutputStream;
// Para acceder al sistema de archivos
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manejador HTTP que sirve archivos estáticos desde el directorio /uploads/.
 * Registrado en el contexto "/uploads/" del servidor, independiente del router principal.
 * Sirve imágenes y otros archivos con el Content-Type correcto.
 */
public class StaticFileHandler implements HttpHandler {

    /** Directorio base donde se almacenan los archivos subidos */
    private static final String UPLOADS_DIR = "uploads";

    /**
     * Maneja peticiones GET a /uploads/*.
     * Lee el archivo del disco y lo retorna con el Content-Type adecuado.
     * @param exchange Contexto de la petición HTTP
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Agregar cabeceras CORS para permitir acceso desde el frontend
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejar petición preflight OPTIONS
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Solo permitir método GET para servir archivos
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // Obtener la ruta del archivo desde la URL: /uploads/nombre-archivo.jpg
        String rutaRelativa = exchange.getRequestURI().getPath();
        // Remover el prefijo "/uploads/" para obtener solo el nombre del archivo
        String nombreArchivo = rutaRelativa.replaceFirst("^/uploads/", "");

        // Construir la ruta absoluta al archivo en disco
        Path rutaArchivo = Paths.get(UPLOADS_DIR, nombreArchivo).normalize();

        // Seguridad: verificar que la ruta no sale del directorio de uploads (path traversal)
        if (!rutaArchivo.startsWith(Paths.get(UPLOADS_DIR).toAbsolutePath()) &&
            !rutaArchivo.toAbsolutePath().startsWith(Paths.get(UPLOADS_DIR).toAbsolutePath())) {
            // También verificar con ruta relativa
            Path uploadsAbsoluto = Paths.get(UPLOADS_DIR).toAbsolutePath();
            Path archivoAbsoluto = rutaArchivo.toAbsolutePath();
            if (!archivoAbsoluto.startsWith(uploadsAbsoluto)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
        }

        // Verificar que el archivo existe en disco
        if (!Files.exists(rutaArchivo) || !Files.isRegularFile(rutaArchivo)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Detectar el tipo de contenido según la extensión del archivo
        String contentType = detectarContentType(nombreArchivo);

        // Leer todos los bytes del archivo
        byte[] contenido = Files.readAllBytes(rutaArchivo);

        // Enviar respuesta con el Content-Type y el contenido del archivo
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, contenido.length);

        // Escribir el archivo en el cuerpo de la respuesta
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(contenido);
        }
    }

    /**
     * Detecta el Content-Type basándose en la extensión del archivo.
     * @param nombreArchivo Nombre del archivo con extensión
     * @return String con el MIME type correspondiente
     */
    private String detectarContentType(String nombreArchivo) {
        // Convertir a minúsculas para comparación sin distinción de mayúsculas
        String nombre = nombreArchivo.toLowerCase();
        // Retornar el MIME type según la extensión
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) return "image/jpeg";
        if (nombre.endsWith(".png"))  return "image/png";
        if (nombre.endsWith(".gif"))  return "image/gif";
        if (nombre.endsWith(".webp")) return "image/webp";
        if (nombre.endsWith(".svg"))  return "image/svg+xml";
        // Por defecto, tipo binario genérico
        return "application/octet-stream";
    }
}
