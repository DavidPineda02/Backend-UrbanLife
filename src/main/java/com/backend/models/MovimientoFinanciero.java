// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un movimiento financiero del negocio.
 * Mapea directamente la tabla Movimientos_Financieros de la base de datos.
 * Se crea automáticamente al registrar una Venta, Compra o Gasto Adicional.
 */
public class MovimientoFinanciero {

    // Identificador único del movimiento financiero en la base de datos (PK)
    private int idMovsFinancieros;
    // Fecha en que se realizó el movimiento en formato "YYYY-MM-DD"
    private String fechaMovimiento;
    // Descripción del concepto del movimiento (ej: "Venta #5")
    private String concepto;
    // Monto del movimiento financiero
    private double monto;
    // Método de pago: "Transferencia" o "Efectivo"
    private String metodoPago;
    // ID del tipo de movimiento (FK a Tipo_Movimientos: 1=Venta, 2=Compra, 3=Gasto)
    private int tipoMovimientoId;
    // ID del usuario que generó el movimiento (FK a Usuarios)
    private int usuarioId;
    // ID de la venta asociada (FK a Venta, null si no es una venta)
    private Integer ventaId;
    // ID de la compra asociada (FK a Compra, null si no es una compra)
    private Integer compraId;
    // ID del gasto adicional asociado (FK a Gastos_Adicionales, null si no aplica)
    private Integer gastoAdicionalId;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idMovsFinancieros ID del movimiento en la BD
     * @param fechaMovimiento Fecha del movimiento en formato "YYYY-MM-DD"
     * @param concepto Descripción del concepto del movimiento
     * @param monto Monto del movimiento
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param tipoMovimientoId ID del tipo de movimiento (1=Venta, 2=Compra, 3=Gasto)
     * @param usuarioId ID del usuario que generó el movimiento
     * @param ventaId ID de la venta asociada (null si no aplica)
     * @param compraId ID de la compra asociada (null si no aplica)
     * @param gastoAdicionalId ID del gasto adicional (null si no aplica)
     */
    public MovimientoFinanciero(int idMovsFinancieros, String fechaMovimiento, String concepto,
                                double monto, String metodoPago, int tipoMovimientoId, int usuarioId,
                                Integer ventaId, Integer compraId, Integer gastoAdicionalId) {
        // Asignar el ID del movimiento
        this.idMovsFinancieros = idMovsFinancieros;
        // Asignar la fecha del movimiento
        this.fechaMovimiento = fechaMovimiento;
        // Asignar el concepto del movimiento
        this.concepto = concepto;
        // Asignar el monto del movimiento
        this.monto = monto;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID del tipo de movimiento
        this.tipoMovimientoId = tipoMovimientoId;
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
        // Asignar el ID de la venta (null si no aplica)
        this.ventaId = ventaId;
        // Asignar el ID de la compra (null si no aplica)
        this.compraId = compraId;
        // Asignar el ID del gasto adicional (null si no aplica)
        this.gastoAdicionalId = gastoAdicionalId;
    }

    /**
     * Constructor sin ID (usado al construir un movimiento nuevo antes de persistir).
     * @param fechaMovimiento Fecha del movimiento en formato "YYYY-MM-DD"
     * @param concepto Descripción del concepto
     * @param monto Monto del movimiento
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param tipoMovimientoId ID del tipo de movimiento
     * @param usuarioId ID del usuario que lo genera
     * @param ventaId ID de la venta (null si no aplica)
     * @param compraId ID de la compra (null si no aplica)
     * @param gastoAdicionalId ID del gasto adicional (null si no aplica)
     */
    public MovimientoFinanciero(String fechaMovimiento, String concepto, double monto,
                                String metodoPago, int tipoMovimientoId, int usuarioId,
                                Integer ventaId, Integer compraId, Integer gastoAdicionalId) {
        // Asignar la fecha del movimiento
        this.fechaMovimiento = fechaMovimiento;
        // Asignar el concepto del movimiento
        this.concepto = concepto;
        // Asignar el monto del movimiento
        this.monto = monto;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID del tipo de movimiento
        this.tipoMovimientoId = tipoMovimientoId;
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
        // Asignar el ID de la venta (null si no aplica)
        this.ventaId = ventaId;
        // Asignar el ID de la compra (null si no aplica)
        this.compraId = compraId;
        // Asignar el ID del gasto adicional (null si no aplica)
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
     * Retorna la fecha del movimiento.
     * @return Fecha en formato "YYYY-MM-DD"
     */
    public String getFechaMovimiento() {
        // Retornar la fecha del movimiento
        return fechaMovimiento;
    }

    /**
     * Establece la fecha del movimiento.
     * @param fechaMovimiento Fecha en formato "YYYY-MM-DD"
     */
    public void setFechaMovimiento(String fechaMovimiento) {
        // Asignar la fecha del movimiento
        this.fechaMovimiento = fechaMovimiento;
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
     * Retorna el método de pago del movimiento.
     * @return Método de pago
     */
    public String getMetodoPago() {
        // Retornar el método de pago
        return metodoPago;
    }

    /**
     * Establece el método de pago del movimiento.
     * @param metodoPago Método de pago a asignar
     */
    public void setMetodoPago(String metodoPago) {
        // Asignar el método de pago
        this.metodoPago = metodoPago;
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
     * Retorna el ID del usuario que generó el movimiento.
     * @return ID del usuario
     */
    public int getUsuarioId() {
        // Retornar el ID del usuario
        return usuarioId;
    }

    /**
     * Establece el ID del usuario.
     * @param usuarioId ID del usuario a asignar
     */
    public void setUsuarioId(int usuarioId) {
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    /**
     * Retorna el ID de la venta asociada (puede ser null).
     * @return ID de la venta o null
     */
    public Integer getVentaId() {
        // Retornar el ID de la venta (nullable)
        return ventaId;
    }

    /**
     * Establece el ID de la venta asociada.
     * @param ventaId ID de la venta (null si no aplica)
     */
    public void setVentaId(Integer ventaId) {
        // Asignar el ID de la venta
        this.ventaId = ventaId;
    }

    /**
     * Retorna el ID de la compra asociada (puede ser null).
     * @return ID de la compra o null
     */
    public Integer getCompraId() {
        // Retornar el ID de la compra (nullable)
        return compraId;
    }

    /**
     * Establece el ID de la compra asociada.
     * @param compraId ID de la compra (null si no aplica)
     */
    public void setCompraId(Integer compraId) {
        // Asignar el ID de la compra
        this.compraId = compraId;
    }

    /**
     * Retorna el ID del gasto adicional asociado (puede ser null).
     * @return ID del gasto adicional o null
     */
    public Integer getGastoAdicionalId() {
        // Retornar el ID del gasto adicional (nullable)
        return gastoAdicionalId;
    }

    /**
     * Establece el ID del gasto adicional asociado.
     * @param gastoAdicionalId ID del gasto (null si no aplica)
     */
    public void setGastoAdicionalId(Integer gastoAdicionalId) {
        // Asignar el ID del gasto adicional
        this.gastoAdicionalId = gastoAdicionalId;
    }
}
