package com.backend.models;

public class DetalleVenta {
    private int idDetVenta;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private int ventaId;
    private int productoId;

    // public DetalleVenta() {
    // }

    public DetalleVenta(int idDetVenta, int cantidad, double precioUnitario, double subtotal,
            int ventaId, int productoId) {
        this.idDetVenta = idDetVenta;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.ventaId = ventaId;
        this.productoId = productoId;
    }

    public DetalleVenta(int cantidad, double precioUnitario, double subtotal,
            int ventaId, int productoId) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.ventaId = ventaId;
        this.productoId = productoId;
    }

    // Getters y Setters
    public int getIdDetVenta() {
        return idDetVenta;
    }

    public void setIdDetVenta(int idDetVenta) {
        this.idDetVenta = idDetVenta;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public int getVentaId() {
        return ventaId;
    }

    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
}
