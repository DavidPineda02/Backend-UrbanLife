package com.backend.models;

public class TipoMovimiento {
    private int idTipoMovimientos;
    private String movimiento; // 'Venta' | 'Compra' | 'Gasto Adicional'
    private String naturaleza; // 'Ingreso' | 'Egreso'

    // public TipoMovimiento() {
    // }

    public TipoMovimiento(int idTipoMovimientos, String movimiento, String naturaleza) {
        this.idTipoMovimientos = idTipoMovimientos;
        this.movimiento = movimiento;
        this.naturaleza = naturaleza;
    }

    public TipoMovimiento(String movimiento, String naturaleza) {
        this.movimiento = movimiento;
        this.naturaleza = naturaleza;
    }

    // Getters y Setters
    public int getIdTipoMovimientos() {
        return idTipoMovimientos;
    }

    public void setIdTipoMovimientos(int idTipoMovimientos) {
        this.idTipoMovimientos = idTipoMovimientos;
    }

    public String getMovimiento() {
        return movimiento;
    }

    public void setMovimiento(String movimiento) {
        this.movimiento = movimiento;
    }

    public String getNaturaleza() {
        return naturaleza;
    }

    public void setNaturaleza(String naturaleza) {
        this.naturaleza = naturaleza;
    }
}
