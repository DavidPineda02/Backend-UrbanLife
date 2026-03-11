// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un gasto adicional del negocio.
 * Mapea directamente la tabla Gastos_Adicionales de la base de datos.
 * Puede estar asociado opcionalmente a una compra (ej: flete, seguro).
 */
public class GastoAdicional {

    // Identificador único del gasto adicional en la base de datos (PK)
    private int idGastosAdic;
    // Monto del gasto adicional
    private double monto;
    // Descripción del concepto del gasto
    private String descripcion;
    // Fecha en que se registró el gasto en formato "YYYY-MM-DD"
    private String fechaRegistro;
    // Método de pago utilizado: "Transferencia" o "Efectivo"
    private String metodoPago;
    // ID de la compra asociada al gasto (FK a Compra, null si no aplica)
    private Integer compraId;
    // ID del tipo de gasto (FK a Tipo_Gasto)
    private int tipoGastoId;
    // ID del usuario que registró el gasto (FK a Usuarios)
    private int usuarioId;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idGastosAdic ID del gasto en la BD
     * @param monto Monto del gasto
     * @param descripcion Descripción del concepto del gasto
     * @param fechaRegistro Fecha de registro en formato "YYYY-MM-DD"
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param compraId ID de la compra asociada (null si no aplica)
     * @param tipoGastoId ID del tipo de gasto
     * @param usuarioId ID del usuario que registró el gasto
     */
    public GastoAdicional(int idGastosAdic, double monto, String descripcion, String fechaRegistro,
                          String metodoPago, Integer compraId, int tipoGastoId, int usuarioId) {
        // Asignar el ID del gasto
        this.idGastosAdic = idGastosAdic;
        // Asignar el monto del gasto
        this.monto = monto;
        // Asignar la descripción del gasto
        this.descripcion = descripcion;
        // Asignar la fecha de registro
        this.fechaRegistro = fechaRegistro;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID de la compra asociada (null si no aplica)
        this.compraId = compraId;
        // Asignar el ID del tipo de gasto
        this.tipoGastoId = tipoGastoId;
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    /**
     * Constructor sin ID (usado al construir un gasto nuevo antes de persistir).
     * @param monto Monto del gasto
     * @param descripcion Descripción del concepto
     * @param fechaRegistro Fecha de registro en formato "YYYY-MM-DD"
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param compraId ID de la compra asociada (null si no aplica)
     * @param tipoGastoId ID del tipo de gasto
     * @param usuarioId ID del usuario que lo registra
     */
    public GastoAdicional(double monto, String descripcion, String fechaRegistro,
                          String metodoPago, Integer compraId, int tipoGastoId, int usuarioId) {
        // Asignar el monto del gasto
        this.monto = monto;
        // Asignar la descripción del gasto
        this.descripcion = descripcion;
        // Asignar la fecha de registro
        this.fechaRegistro = fechaRegistro;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID de la compra asociada (null si no aplica)
        this.compraId = compraId;
        // Asignar el ID del tipo de gasto
        this.tipoGastoId = tipoGastoId;
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del gasto adicional.
     * @return ID del gasto
     */
    public int getIdGastosAdic() {
        // Retornar el ID del gasto
        return idGastosAdic;
    }

    /**
     * Establece el ID del gasto adicional.
     * @param idGastosAdic ID a asignar
     */
    public void setIdGastosAdic(int idGastosAdic) {
        // Asignar el ID del gasto
        this.idGastosAdic = idGastosAdic;
    }

    /**
     * Retorna el monto del gasto.
     * @return Monto del gasto
     */
    public double getMonto() {
        // Retornar el monto del gasto
        return monto;
    }

    /**
     * Establece el monto del gasto.
     * @param monto Monto a asignar
     */
    public void setMonto(double monto) {
        // Asignar el monto del gasto
        this.monto = monto;
    }

    /**
     * Retorna la descripción del gasto.
     * @return Descripción del gasto
     */
    public String getDescripcion() {
        // Retornar la descripción del gasto
        return descripcion;
    }

    /**
     * Establece la descripción del gasto.
     * @param descripcion Descripción a asignar
     */
    public void setDescripcion(String descripcion) {
        // Asignar la descripción del gasto
        this.descripcion = descripcion;
    }

    /**
     * Retorna la fecha de registro del gasto.
     * @return Fecha en formato "YYYY-MM-DD"
     */
    public String getFechaRegistro() {
        // Retornar la fecha de registro
        return fechaRegistro;
    }

    /**
     * Establece la fecha de registro del gasto.
     * @param fechaRegistro Fecha en formato "YYYY-MM-DD"
     */
    public void setFechaRegistro(String fechaRegistro) {
        // Asignar la fecha de registro
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Retorna el método de pago del gasto.
     * @return Método de pago ("Transferencia" o "Efectivo")
     */
    public String getMetodoPago() {
        // Retornar el método de pago
        return metodoPago;
    }

    /**
     * Establece el método de pago del gasto.
     * @param metodoPago Método de pago a asignar
     */
    public void setMetodoPago(String metodoPago) {
        // Asignar el método de pago
        this.metodoPago = metodoPago;
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
     * Retorna el ID del tipo de gasto.
     * @return ID del tipo de gasto
     */
    public int getTipoGastoId() {
        // Retornar el ID del tipo de gasto
        return tipoGastoId;
    }

    /**
     * Establece el ID del tipo de gasto.
     * @param tipoGastoId ID del tipo a asignar
     */
    public void setTipoGastoId(int tipoGastoId) {
        // Asignar el ID del tipo de gasto
        this.tipoGastoId = tipoGastoId;
    }

    /**
     * Retorna el ID del usuario que registró el gasto.
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
}
