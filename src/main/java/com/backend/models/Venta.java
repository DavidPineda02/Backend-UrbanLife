// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa el encabezado de una venta realizada en el negocio.
 * Mapea directamente la tabla Venta de la base de datos.
 * Cada venta tiene un encabezado (esta clase) y uno o más detalles (DetalleVenta).
 */
public class Venta {

    // Identificador único de la venta en la base de datos (PK)
    private int idVenta;
    // Fecha en que se realizó la venta en formato "YYYY-MM-DD"
    private String fechaVenta;
    // Monto total de la venta (suma de todos los subtotales de sus detalles)
    private double totalVenta;
    // Método de pago utilizado: "Transferencia" o "Efectivo"
    private String metodoPago;
    // ID del usuario que registró la venta (FK a Usuarios)
    private int usuarioId;
    // ID del cliente al que se le realizó la venta (FK a Clientes)
    private int clienteId;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idVenta ID de la venta en la BD
     * @param fechaVenta Fecha de la venta en formato "YYYY-MM-DD"
     * @param totalVenta Monto total de la venta
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param usuarioId ID del usuario que registró la venta
     * @param clienteId ID del cliente asociado a la venta
     */
    public Venta(int idVenta, String fechaVenta, double totalVenta,
                 String metodoPago, int usuarioId, int clienteId) {
        // Asignar el ID de la venta
        this.idVenta = idVenta;
        // Asignar la fecha de la venta
        this.fechaVenta = fechaVenta;
        // Asignar el monto total de la venta
        this.totalVenta = totalVenta;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID del usuario que la registró
        this.usuarioId = usuarioId;
        // Asignar el ID del cliente
        this.clienteId = clienteId;
    }

    /**
     * Constructor sin ID (usado al construir una nueva venta antes de persistir).
     * @param fechaVenta Fecha de la venta en formato "YYYY-MM-DD"
     * @param totalVenta Monto total calculado de la venta
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param usuarioId ID del usuario que registra la venta
     * @param clienteId ID del cliente al que se le realiza la venta
     */
    public Venta(String fechaVenta, double totalVenta,
                 String metodoPago, int usuarioId, int clienteId) {
        // Asignar la fecha de la venta
        this.fechaVenta = fechaVenta;
        // Asignar el monto total de la venta
        this.totalVenta = totalVenta;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID del usuario que la registra
        this.usuarioId = usuarioId;
        // Asignar el ID del cliente
        this.clienteId = clienteId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID de la venta.
     * @return ID de la venta
     */
    public int getIdVenta() {
        // Retornar el ID de la venta
        return idVenta;
    }

    /**
     * Establece el ID de la venta (usado tras recuperar la clave generada en INSERT).
     * @param idVenta ID a asignar
     */
    public void setIdVenta(int idVenta) {
        // Asignar el ID de la venta
        this.idVenta = idVenta;
    }

    /**
     * Retorna la fecha de la venta.
     * @return Fecha en formato "YYYY-MM-DD"
     */
    public String getFechaVenta() {
        // Retornar la fecha de la venta
        return fechaVenta;
    }

    /**
     * Establece la fecha de la venta.
     * @param fechaVenta Fecha en formato "YYYY-MM-DD"
     */
    public void setFechaVenta(String fechaVenta) {
        // Asignar la fecha de la venta
        this.fechaVenta = fechaVenta;
    }

    /**
     * Retorna el monto total de la venta.
     * @return Total de la venta
     */
    public double getTotalVenta() {
        // Retornar el monto total de la venta
        return totalVenta;
    }

    /**
     * Establece el monto total de la venta.
     * @param totalVenta Total a asignar
     */
    public void setTotalVenta(double totalVenta) {
        // Asignar el monto total de la venta
        this.totalVenta = totalVenta;
    }

    /**
     * Retorna el método de pago de la venta.
     * @return Método de pago ("Transferencia" o "Efectivo")
     */
    public String getMetodoPago() {
        // Retornar el método de pago
        return metodoPago;
    }

    /**
     * Establece el método de pago de la venta.
     * @param metodoPago Método de pago a asignar
     */
    public void setMetodoPago(String metodoPago) {
        // Asignar el método de pago
        this.metodoPago = metodoPago;
    }

    /**
     * Retorna el ID del usuario que registró la venta.
     * @return ID del usuario
     */
    public int getUsuarioId() {
        // Retornar el ID del usuario
        return usuarioId;
    }

    /**
     * Establece el ID del usuario que registró la venta.
     * @param usuarioId ID del usuario a asignar
     */
    public void setUsuarioId(int usuarioId) {
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    /**
     * Retorna el ID del cliente asociado a la venta.
     * @return ID del cliente
     */
    public int getClienteId() {
        // Retornar el ID del cliente
        return clienteId;
    }

    /**
     * Establece el ID del cliente asociado a la venta.
     * @param clienteId ID del cliente a asignar
     */
    public void setClienteId(int clienteId) {
        // Asignar el ID del cliente
        this.clienteId = clienteId;
    }
}
