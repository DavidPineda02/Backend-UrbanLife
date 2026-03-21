// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un movimiento financiero del negocio.
 * Mapea directamente la tabla Movimientos_Financieros de la base de datos.
 * Se crea automáticamente al registrar una Venta, Compra o Gasto Adicional.
 * Usa VENTA_ID, COMPRA_ID o GASTO_ADICIONAL_ID (FKs separadas) para identificar la operación origen.
 */
public class MovimientoFinanciero {

    // Identificador único del movimiento financiero en la base de datos (PK)
    private int idMovsFinancieros;
    // Descripción del concepto del movimiento (ej: "Venta #5")
    private String concepto;
    // Monto del movimiento financiero
    private double monto;
    // Fecha en que se realizó el movimiento en formato "YYYY-MM-DD"
    private String fecha;
    // ID del tipo de movimiento (FK a Tipo_Movimientos: 1=Venta, 2=Compra, 3=Gasto)
    private int tipoMovimientoId;
    // ID de la venta asociada (solo si tipo=1, null en otro caso)
    private Integer ventaId;
    // ID de la compra asociada (solo si tipo=2, null en otro caso)
    private Integer compraId;
    // ID del gasto adicional asociado (solo si tipo=3, null en otro caso)
    private Integer gastoAdicionalId;
    // Nombre del tipo de movimiento obtenido por JOIN (ej: "Venta", "Compra", "Gasto Adicional")
    private String tipoMovimiento;
    // Naturaleza del movimiento obtenida por JOIN ("Ingreso" o "Egreso")
    private String naturaleza;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idMovsFinancieros ID del movimiento en la BD
     * @param concepto Descripción del concepto del movimiento
     * @param monto Monto del movimiento
     * @param fecha Fecha del movimiento en formato "YYYY-MM-DD"
     * @param tipoMovimientoId ID del tipo de movimiento (1=Venta, 2=Compra, 3=Gasto)
     * @param ventaId ID de la venta asociada (null si no aplica)
     * @param compraId ID de la compra asociada (null si no aplica)
     * @param gastoAdicionalId ID del gasto adicional asociado (null si no aplica)
     */
    public MovimientoFinanciero(int idMovsFinancieros, String concepto, double monto,
                                String fecha, int tipoMovimientoId,
                                Integer ventaId, Integer compraId, Integer gastoAdicionalId) {
        // Asignar el ID del movimiento
        this.idMovsFinancieros = idMovsFinancieros;
        // Asignar el concepto del movimiento
        this.concepto = concepto;
        // Asignar el monto del movimiento
        this.monto = monto;
        // Asignar la fecha del movimiento
        this.fecha = fecha;
        // Asignar el ID del tipo de movimiento
        this.tipoMovimientoId = tipoMovimientoId;
        // Asignar el ID de la venta asociada (null si no es tipo Venta)
        this.ventaId = ventaId;
        // Asignar el ID de la compra asociada (null si no es tipo Compra)
        this.compraId = compraId;
        // Asignar el ID del gasto adicional asociado (null si no es tipo Gasto)
        this.gastoAdicionalId = gastoAdicionalId;
    }

    /**
     * Constructor sin ID (usado al construir un movimiento nuevo antes de persistir).
     * @param concepto Descripción del concepto
     * @param monto Monto del movimiento
     * @param fecha Fecha del movimiento en formato "YYYY-MM-DD"
     * @param tipoMovimientoId ID del tipo de movimiento
     * @param ventaId ID de la venta asociada (null si no aplica)
     * @param compraId ID de la compra asociada (null si no aplica)
     * @param gastoAdicionalId ID del gasto adicional asociado (null si no aplica)
     */
    public MovimientoFinanciero(String concepto, double monto, String fecha,
                                int tipoMovimientoId,
                                Integer ventaId, Integer compraId, Integer gastoAdicionalId) {
        // Asignar el concepto del movimiento
        this.concepto = concepto;
        // Asignar el monto del movimiento
        this.monto = monto;
        // Asignar la fecha del movimiento
        this.fecha = fecha;
        // Asignar el ID del tipo de movimiento
        this.tipoMovimientoId = tipoMovimientoId;
        // Asignar el ID de la venta asociada (null si no es tipo Venta)
        this.ventaId = ventaId;
        // Asignar el ID de la compra asociada (null si no es tipo Compra)
        this.compraId = compraId;
        // Asignar el ID del gasto adicional asociado (null si no es tipo Gasto)
        this.gastoAdicionalId = gastoAdicionalId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del movimiento financiero.
     * @return ID del movimiento
     */
    public int getIdMovsFinancieros() {
        // Retornar el ID del movimiento
        return idMovsFinancieros;
    }

    /**
     * Establece el ID del movimiento.
     * @param idMovsFinancieros ID a asignar
     */
    public void setIdMovsFinancieros(int idMovsFinancieros) {
        // Asignar el ID del movimiento
        this.idMovsFinancieros = idMovsFinancieros;
    }

    /**
     * Retorna el concepto del movimiento.
     * @return Concepto del movimiento
     */
    public String getConcepto() {
        // Retornar el concepto del movimiento
        return concepto;
    }

    /**
     * Establece el concepto del movimiento.
     * @param concepto Concepto a asignar
     */
    public void setConcepto(String concepto) {
        // Asignar el concepto del movimiento
        this.concepto = concepto;
    }

    /**
     * Retorna el monto del movimiento.
     * @return Monto del movimiento
     */
    public double getMonto() {
        // Retornar el monto del movimiento
        return monto;
    }

    /**
     * Establece el monto del movimiento.
     * @param monto Monto a asignar
     */
    public void setMonto(double monto) {
        // Asignar el monto del movimiento
        this.monto = monto;
    }

    /**
     * Retorna la fecha del movimiento.
     * @return Fecha en formato "YYYY-MM-DD"
     */
    public String getFecha() {
        // Retornar la fecha del movimiento
        return fecha;
    }

    /**
     * Establece la fecha del movimiento.
     * @param fecha Fecha en formato "YYYY-MM-DD"
     */
    public void setFecha(String fecha) {
        // Asignar la fecha del movimiento
        this.fecha = fecha;
    }

    /**
     * Retorna el ID del tipo de movimiento.
     * @return ID del tipo de movimiento
     */
    public int getTipoMovimientoId() {
        // Retornar el ID del tipo de movimiento
        return tipoMovimientoId;
    }

    /**
     * Establece el ID del tipo de movimiento.
     * @param tipoMovimientoId ID del tipo a asignar
     */
    public void setTipoMovimientoId(int tipoMovimientoId) {
        // Asignar el ID del tipo de movimiento
        this.tipoMovimientoId = tipoMovimientoId;
    }

    /**
     * Retorna el ID de la venta asociada.
     * @return ID de la venta o null si no es tipo Venta
     */
    public Integer getVentaId() {
        // Retornar el ID de la venta asociada
        return ventaId;
    }

    /**
     * Establece el ID de la venta asociada.
     * @param ventaId ID de la venta a asignar
     */
    public void setVentaId(Integer ventaId) {
        // Asignar el ID de la venta asociada
        this.ventaId = ventaId;
    }

    /**
     * Retorna el ID de la compra asociada.
     * @return ID de la compra o null si no es tipo Compra
     */
    public Integer getCompraId() {
        // Retornar el ID de la compra asociada
        return compraId;
    }

    /**
     * Establece el ID de la compra asociada.
     * @param compraId ID de la compra a asignar
     */
    public void setCompraId(Integer compraId) {
        // Asignar el ID de la compra asociada
        this.compraId = compraId;
    }

    /**
     * Retorna el ID del gasto adicional asociado.
     * @return ID del gasto adicional o null si no es tipo Gasto
     */
    public Integer getGastoAdicionalId() {
        // Retornar el ID del gasto adicional asociado
        return gastoAdicionalId;
    }

    /**
     * Establece el ID del gasto adicional asociado.
     * @param gastoAdicionalId ID del gasto adicional a asignar
     */
    public void setGastoAdicionalId(Integer gastoAdicionalId) {
        // Asignar el ID del gasto adicional asociado
        this.gastoAdicionalId = gastoAdicionalId;
    }

    /**
     * Retorna el nombre del tipo de movimiento (poblado por JOIN).
     * @return Nombre del tipo ("Venta", "Compra", "Gasto Adicional") o null si no se hizo JOIN
     */
    public String getTipoMovimiento() {
        // Retornar el nombre del tipo de movimiento
        return tipoMovimiento;
    }

    /**
     * Establece el nombre del tipo de movimiento (poblado por JOIN).
     * @param tipoMovimiento Nombre del tipo a asignar
     */
    public void setTipoMovimiento(String tipoMovimiento) {
        // Asignar el nombre del tipo de movimiento
        this.tipoMovimiento = tipoMovimiento;
    }

    /**
     * Retorna la naturaleza del movimiento (poblada por JOIN).
     * @return Naturaleza ("Ingreso" o "Egreso") o null si no se hizo JOIN
     */
    public String getNaturaleza() {
        // Retornar la naturaleza del movimiento
        return naturaleza;
    }

    /**
     * Establece la naturaleza del movimiento (poblada por JOIN).
     * @param naturaleza Naturaleza a asignar ("Ingreso" o "Egreso")
     */
    public void setNaturaleza(String naturaleza) {
        // Asignar la naturaleza del movimiento
        this.naturaleza = naturaleza;
    }
}
