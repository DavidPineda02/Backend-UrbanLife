package com.backend.models;

import java.time.LocalDate;

public class MovimientoFinanciero {
    private int idMovsFinancieros;
    private LocalDate fechaMovimiento;
    private String concepto;
    private double monto;
    private String metodoPago; // 'Transferencia' | 'Efectivo'
    private int tipoMovimientoId;
    private int usuarioId;

    // public MovimientoFinanciero() {
    // }

    public MovimientoFinanciero(int idMovsFinancieros, LocalDate fechaMovimiento, String concepto,
            double monto, String metodoPago, int tipoMovimientoId, int usuarioId) {
        this.idMovsFinancieros = idMovsFinancieros;
        this.fechaMovimiento = fechaMovimiento;
        this.concepto = concepto;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.tipoMovimientoId = tipoMovimientoId;
        this.usuarioId = usuarioId;
    }

    public MovimientoFinanciero(LocalDate fechaMovimiento, String concepto, double monto,
            String metodoPago, int tipoMovimientoId, int usuarioId) {
        this.fechaMovimiento = fechaMovimiento;
        this.concepto = concepto;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.tipoMovimientoId = tipoMovimientoId;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getIdMovsFinancieros() {
        return idMovsFinancieros;
    }

    public void setIdMovsFinancieros(int idMovsFinancieros) {
        this.idMovsFinancieros = idMovsFinancieros;
    }

    public LocalDate getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDate fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public int getTipoMovimientoId() {
        return tipoMovimientoId;
    }

    public void setTipoMovimientoId(int tipoMovimientoId) {
        this.tipoMovimientoId = tipoMovimientoId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
}
