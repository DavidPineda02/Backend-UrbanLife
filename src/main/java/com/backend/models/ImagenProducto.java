package com.backend.models;

import java.time.LocalDate;

public class ImagenProducto {
    private int imagenProducto;
    private String url;
    private LocalDate fechaRegistro;
    private int productoId;

    // public ImagenProducto() {
    // }

    public ImagenProducto(int imagenProducto, String url, LocalDate fechaRegistro, int productoId) {
        this.imagenProducto = imagenProducto;
        this.url = url;
        this.fechaRegistro = fechaRegistro;
        this.productoId = productoId;
    }

    public ImagenProducto(String url, LocalDate fechaRegistro, int productoId) {
        this.url = url;
        this.fechaRegistro = fechaRegistro;
        this.productoId = productoId;
    }

    // Getters y Setters
    public int getImagenProducto() {
        return imagenProducto;
    }

    public void setImagenProducto(int imagenProducto) {
        this.imagenProducto = imagenProducto;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
}
