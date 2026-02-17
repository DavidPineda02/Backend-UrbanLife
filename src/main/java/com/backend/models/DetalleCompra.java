package com.backend.models;

public class DetalleCompra {
    private int idDetCompra;
    private int cantidad;
    private double costoUnitario;
    private double subtotal;
    private int compraId;
    private int productoId;

    // public DetalleCompra() {
    // }

    public DetalleCompra(int idDetCompra, int cantidad, double costoUnitario, double subtotal,
            int compraId, int productoId) {
        this.idDetCompra = idDetCompra;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
        this.compraId = compraId;
        this.productoId = productoId;
    }

    public DetalleCompra(int cantidad, double costoUnitario, double subtotal,
            int compraId, int productoId) {
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
        this.compraId = compraId;
        this.productoId = productoId;
    }

    // Getters y Setters
    public int getIdDetCompra() {
        return idDetCompra;
    }

    public void setIdDetCompra(int idDetCompra) {
        this.idDetCompra = idDetCompra;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public int getCompraId() {
        return compraId;
    }

    public void setCompraId(int compraId) {
        this.compraId = compraId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
}
