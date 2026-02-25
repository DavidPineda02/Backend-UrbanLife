package com.backend.models;

import java.time.LocalDate;

public class GastoAdicional {
    private int idGastosAdic;
    private double monto;
    private String descripcion;
    private LocalDate fechaRegistro;
    private String metodoPago; // 'Transferencia' | 'Efectivo'
    private Integer compraId; // Puede ser NULL
    private int tipoGastoId;
    private int usuarioId;

    // public GastoAdicional() {
    // }

    public GastoAdicional(int idGastosAdic, double monto, String descripcion, LocalDate fechaRegistro,
            String metodoPago, Integer compraId, int tipoGastoId, int usuarioId) {
        this.idGastosAdic = idGastosAdic;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
        this.metodoPago = metodoPago;
        this.compraId = compraId;
        this.tipoGastoId = tipoGastoId;
        this.usuarioId = usuarioId;
    }

    public GastoAdicional(double monto, String descripcion, LocalDate fechaRegistro,
            String metodoPago, Integer compraId, int tipoGastoId, int usuarioId) {
        this.monto = monto;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
        this.metodoPago = metodoPago;
        this.compraId = compraId;
        this.tipoGastoId = tipoGastoId;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getIdGastosAdic() {
        return idGastosAdic;
    }

    public void setIdGastosAdic(int idGastosAdic) {
        this.idGastosAdic = idGastosAdic;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getCompraId() {
        return compraId;
    }

    public void setCompraId(Integer compraId) {
        this.compraId = compraId;
    }

    public int getTipoGastoId() {
        return tipoGastoId;
    }

    public void setTipoGastoId(int tipoGastoId) {
        this.tipoGastoId = tipoGastoId;
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
}
