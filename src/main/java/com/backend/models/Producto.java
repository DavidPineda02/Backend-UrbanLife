package com.backend.models;

public class Producto {
    private int idProducto;
    private String nombre;
    private String descripcion;
    private double precioVenta;
    private double costoPromedio;
    private int stock;
    private String estado; // 'Activo' | 'Inactivo'
    private int categoriaId;

    // public Producto() {
    // }

    public Producto(int idProducto, String nombre, String descripcion, double precioVenta,
            double costoPromedio, int stock, String estado, int categoriaId) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.costoPromedio = costoPromedio;
        this.stock = stock;
        this.estado = estado;
        this.categoriaId = categoriaId;
    }

    public Producto(String nombre, String descripcion, double precioVenta,
            double costoPromedio, int stock, String estado, int categoriaId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.costoPromedio = costoPromedio;
        this.stock = stock;
        this.estado = estado;
        this.categoriaId = categoriaId;
    }

    // Getters y Setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(double costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
}
