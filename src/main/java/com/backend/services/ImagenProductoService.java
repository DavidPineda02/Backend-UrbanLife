// Paquete de servicios de lógica de negocio
package com.backend.services;

// DAO para operaciones de imágenes en la base de datos
import com.backend.dao.ImagenProductoDAO;
// DAO para verificar que el producto existe
import com.backend.dao.ProductoDAO;
// Modelo de imagen de producto
import com.backend.models.ImagenProducto;
// Para construir el objeto JSON de respuesta
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// Para decodificar Base64
import java.util.Base64;
// Para guardar archivos en disco
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// Para generar nombres únicos de archivo
import java.util.UUID;
// Para acceder a la lista de imágenes
import java.util.List;
// Para la fecha actual
import java.time.LocalDate;

/**
 * Servicio con la lógica de negocio para el módulo de imágenes de productos.
 * Maneja validaciones, almacenamiento en disco y operaciones en la base de datos.
 * Recibe imágenes en formato Base64, las decodifica y las guarda en el directorio /uploads/.
 */
public class ImagenProductoService {

    /** Directorio donde se almacenan las imágenes subidas */
    private static final String UPLOADS_DIR = "uploads";
    /** Máximo de imágenes permitidas por producto */
    private static final int MAX_IMAGENES = 5;
    /** Tamaño máximo permitido en bytes del Base64 (~2MB después de comprimir) */
    private static final int MAX_BASE64_BYTES = 3_000_000;

    /**
     * Sube una imagen para un producto: decodifica Base64, guarda en disco, registra en BD.
     * @param productoId ID del producto al que pertenece la imagen
     * @param base64Data Cadena Base64 con la imagen (puede incluir prefijo data:image/...)
     * @param extension Extensión del archivo (jpg, png, webp, etc.)
     * @return JsonObject con resultado de la operación
     */
    public static JsonObject upload(int productoId, String base64Data, String extension) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Validar que el productoId sea positivo
        if (productoId <= 0) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El ID del producto no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Verificar que el producto existe en la base de datos
        if (ProductoDAO.findById(productoId) == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El producto no existe");
            respuesta.addProperty("status", 404);
            return respuesta;
        }

        // Validar que se envió una imagen
        if (base64Data == null || base64Data.isBlank()) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La imagen es requerida");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Validar que no exceda el límite de imágenes por producto
        int cantidadActual = ImagenProductoDAO.countByProductoId(productoId);
        if (cantidadActual >= MAX_IMAGENES) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El producto ya tiene el máximo de " + MAX_IMAGENES + " imágenes permitidas");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Limpiar el prefijo del base64 si viene (ej: "data:image/jpeg;base64,...")
        String base64Limpio = base64Data;
        if (base64Data.contains(",")) {
            base64Limpio = base64Data.split(",", 2)[1];
        }

        // Validar tamaño del base64 para evitar imágenes demasiado grandes
        if (base64Limpio.length() > MAX_BASE64_BYTES) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La imagen es demasiado grande. Máximo 2MB");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Determinar la extensión del archivo
        String ext = (extension != null && !extension.isBlank()) ? extension.toLowerCase() : "jpg";
        // Permitir solo extensiones de imagen seguras
        if (!ext.matches("jpg|jpeg|png|gif|webp")) ext = "jpg";

        // Decodificar el Base64 a bytes
        byte[] bytesImagen;
        try {
            bytesImagen = Base64.getDecoder().decode(base64Limpio);
        } catch (IllegalArgumentException e) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El formato de la imagen no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Generar un nombre único para el archivo para evitar colisiones
        String nombreArchivo = UUID.randomUUID().toString() + "." + ext;
        // Construir la ruta completa del archivo en disco
        Path directorioUploads = Paths.get(UPLOADS_DIR);
        Path rutaArchivo = directorioUploads.resolve(nombreArchivo);

        // Guardar el archivo en disco
        try {
            // Crear el directorio /uploads/ si no existe
            if (!Files.exists(directorioUploads)) {
                Files.createDirectories(directorioUploads);
            }
            // Escribir los bytes de la imagen en el archivo
            Files.write(rutaArchivo, bytesImagen);
        } catch (Exception e) {
            System.out.println("Error guardando imagen en disco: " + e.getMessage());
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al guardar la imagen en el servidor");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // URL relativa con la que el frontend puede acceder a la imagen
        String urlImagen = "/uploads/" + nombreArchivo;

        // Obtener la fecha actual en formato YYYY-MM-DD como String
        String fechaHoy = LocalDate.now().toString();
        // Crear el objeto ImagenProducto para guardar en la BD
        ImagenProducto nuevaImagen = new ImagenProducto(urlImagen, fechaHoy, productoId);
        // Insertar en la base de datos usando el DAO
        ImagenProducto imagenCreada = ImagenProductoDAO.create(nuevaImagen);

        // Verificar que la inserción fue exitosa
        if (imagenCreada == null) {
            // Si falló la BD, eliminar el archivo del disco para mantener consistencia
            try { Files.deleteIfExists(rutaArchivo); } catch (Exception ignored) {}
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al registrar la imagen en la base de datos");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // Construir respuesta exitosa con la URL de la imagen guardada
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Imagen subida correctamente");
        respuesta.addProperty("url", urlImagen);
        respuesta.addProperty("imagenId", imagenCreada.getImagenProducto());
        respuesta.addProperty("status", 201);
        return respuesta;
    }

    /**
     * Obtiene todas las imágenes de un producto.
     * @param productoId ID del producto
     * @return JsonObject con la lista de imágenes
     */
    public static JsonObject getByProductoId(int productoId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Validar que el productoId sea positivo
        if (productoId <= 0) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El ID del producto no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Obtener la lista de imágenes del DAO
        List<ImagenProducto> imagenes = ImagenProductoDAO.findByProductoId(productoId);

        // Construir el array JSON con las URLs de las imágenes
        JsonArray listaImagenes = new JsonArray();
        for (ImagenProducto img : imagenes) {
            // Crear objeto con los datos de cada imagen
            JsonObject item = new JsonObject();
            item.addProperty("id", img.getImagenProducto());
            item.addProperty("url", img.getUrl());
            item.addProperty("fechaRegistro", img.getFechaRegistro());
            listaImagenes.add(item);
        }

        // Construir respuesta exitosa con el array de imágenes
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Imágenes obtenidas correctamente");
        respuesta.add("imagenes", listaImagenes);
        respuesta.addProperty("status", 200);
        return respuesta;
    }

    /**
     * Elimina una imagen por su ID: la borra de la BD y del disco.
     * @param imagenId ID de la imagen a eliminar
     * @return JsonObject con resultado de la operación
     */
    public static JsonObject delete(int imagenId) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();

        // Validar que el imagenId sea positivo
        if (imagenId <= 0) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "El ID de la imagen no es válido");
            respuesta.addProperty("status", 400);
            return respuesta;
        }

        // Buscar la imagen en la BD para obtener su URL antes de eliminarla
        ImagenProducto imagen = ImagenProductoDAO.findById(imagenId);
        if (imagen == null) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "La imagen no existe");
            respuesta.addProperty("status", 404);
            return respuesta;
        }

        // Guardar la URL para eliminar el archivo del disco después
        String url = imagen.getUrl();

        // Eliminar el registro de la base de datos
        boolean eliminadaBD = ImagenProductoDAO.delete(imagenId);
        if (!eliminadaBD) {
            respuesta.addProperty("success", false);
            respuesta.addProperty("message", "Error al eliminar la imagen de la base de datos");
            respuesta.addProperty("status", 500);
            return respuesta;
        }

        // Eliminar el archivo físico del disco
        try {
            // Construir la ruta del archivo (quitar el "/" inicial de la URL)
            String rutaRelativa = url.startsWith("/") ? url.substring(1) : url;
            Path rutaArchivo = Paths.get(rutaRelativa);
            // Intentar eliminar el archivo (si no existe, no lanzar error)
            Files.deleteIfExists(rutaArchivo);
        } catch (Exception e) {
            // Loguear el error pero no fallar la respuesta (BD ya fue actualizada)
            System.out.println("Advertencia: no se pudo eliminar el archivo " + url + ": " + e.getMessage());
        }

        // Respuesta exitosa
        respuesta.addProperty("success", true);
        respuesta.addProperty("message", "Imagen eliminada correctamente");
        respuesta.addProperty("status", 200);
        return respuesta;
    }
}
