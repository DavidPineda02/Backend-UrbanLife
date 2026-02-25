package com.backend.models;

import java.time.LocalDate;

public class Venta {
    private int idVenta;
    private LocalDate fechaVenta;
    private double totalVenta;
    private String metodoPago; // 'Transferencia' | 'Efectivo'
    private int usuarioId;
    private int clienteId;

    // public Venta() {
    // }

    public Venta(int idVenta, LocalDate fechaVenta, double totalVenta, String metodoPago, int usuarioId, int clienteId) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.totalVenta = totalVenta;
        this.metodoPago = metodoPago;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
    }

    public Venta(LocalDate fechaVenta, double totalVenta, String metodoPago, int usuarioId, int clienteId) {
        this.fechaVenta = fechaVenta;
        this.totalVenta = totalVenta;
        this.metodoPago = metodoPago;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
    }

    // Getters y Setters
    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDate getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDate fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
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

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }
}
