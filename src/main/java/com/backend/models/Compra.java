package com.backend.models;

import java.time.LocalDate;

public class Compra {
    private int idCompra;
    private LocalDate fechaCompra;
    private double totalCompra;
    private String metodoPago; // 'Transferencia' | 'Efectivo'
    private int usuarioId;
    private int proveedorId;

    // public Compra() {
    // }

    public Compra(int idCompra, LocalDate fechaCompra, double totalCompra, String metodoPago, int usuarioId, int proveedorId) {
        this.idCompra = idCompra;
        this.fechaCompra = fechaCompra;
        this.totalCompra = totalCompra;
        this.metodoPago = metodoPago;
        this.usuarioId = usuarioId;
        this.proveedorId = proveedorId;
    }

    public Compra(LocalDate fechaCompra, double totalCompra, String metodoPago, int usuarioId, int proveedorId) {
        this.fechaCompra = fechaCompra;
        this.totalCompra = totalCompra;
        this.metodoPago = metodoPago;
        this.usuarioId = usuarioId;
        this.proveedorId = proveedorId;
    }

    // Getters y Setters
    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(double totalCompra) {
        this.totalCompra = totalCompra;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(int proveedorId) {
        this.proveedorId = proveedorId;
    }
}
