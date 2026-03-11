// Paquete de modelos de datos de la aplicación
package com.backend.models;

// Para manejar fechas de registro de la imagen
import java.time.LocalDate;

/**
 * Modelo que representa una imagen asociada a un producto.
 * Cada producto puede tener múltiples imágenes almacenadas por URL.
 */
public class ImagenProducto {

    /** Identificador único de la imagen del producto en la base de datos */
    private int imagenProducto;
    /** URL donde se encuentra almacenada la imagen */
    private String url;
    /** Fecha en que se registró la imagen en el sistema */
    private LocalDate fechaRegistro;
    /** ID del producto al que pertenece esta imagen */
    private int productoId;

    /**
     * Constructor para crear una imagen de producto con ID existente (lectura desde BD).
     * @param imagenProducto ID de la imagen en la base de datos
     * @param url URL de la imagen almacenada
     * @param fechaRegistro Fecha de registro de la imagen
     * @param productoId ID del producto asociado
     */
    public ImagenProducto(int imagenProducto, String url, LocalDate fechaRegistro, int productoId) {
        // Asignar el ID de la imagen
        this.imagenProducto = imagenProducto;
        // Asignar la URL de la imagen
        this.url = url;
        // Asignar la fecha de registro
        this.fechaRegistro = fechaRegistro;
        // Asignar el ID del producto asociado
        this.productoId = productoId;
    }

    /**
     * Constructor para crear una nueva imagen de producto sin ID (inserción en BD).
     * @param url URL de la imagen almacenada
     * @param fechaRegistro Fecha de registro de la imagen
     * @param productoId ID del producto asociado
     */
    public ImagenProducto(String url, LocalDate fechaRegistro, int productoId) {
        // Asignar la URL de la imagen
        this.url = url;
        // Asignar la fecha de registro
        this.fechaRegistro = fechaRegistro;
        // Asignar el ID del producto asociado
        this.productoId = productoId;
    }

    /**
     * Obtiene el ID de la imagen del producto.
     * @return ID de la imagen en la base de datos
     */
    public int getImagenProducto() {
        // Retornar el ID de la imagen
        return imagenProducto;
    }

    /**
     * Establece el ID de la imagen del producto.
     * @param imagenProducto Nuevo ID de la imagen
     */
    public void setImagenProducto(int imagenProducto) {
        // Asignar el nuevo ID de la imagen
        this.imagenProducto = imagenProducto;
    }

    /**
     * Obtiene la URL de la imagen.
     * @return URL donde se almacena la imagen
     */
    public String getUrl() {
        // Retornar la URL de la imagen
        return url;
    }

    /**
     * Establece la URL de la imagen.
     * @param url Nueva URL de la imagen
     */
    public void setUrl(String url) {
        // Asignar la nueva URL de la imagen
        this.url = url;
    }

    /**
     * Obtiene la fecha de registro de la imagen.
     * @return Fecha en que se registró la imagen
     */
    public LocalDate getFechaRegistro() {
        // Retornar la fecha de registro
        return fechaRegistro;
    }

    /**
     * Establece la fecha de registro de la imagen.
     * @param fechaRegistro Nueva fecha de registro
     */
    public void setFechaRegistro(LocalDate fechaRegistro) {
        // Asignar la nueva fecha de registro
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Obtiene el ID del producto asociado.
     * @return ID del producto al que pertenece la imagen
     */
    public int getProductoId() {
        // Retornar el ID del producto
        return productoId;
    }

    /**
     * Establece el ID del producto asociado.
     * @param productoId Nuevo ID del producto
     */
    public void setProductoId(int productoId) {
        // Asignar el nuevo ID del producto
        this.productoId = productoId;
    }
}
