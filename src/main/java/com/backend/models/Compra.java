// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa el encabezado de una compra realizada a un proveedor.
 * Mapea directamente la tabla Compra de la base de datos.
 * Cada compra tiene un encabezado (esta clase) y uno o más detalles (DetalleCompra).
 */
public class Compra {

    // Identificador único de la compra en la base de datos (PK)
    private int idCompra;
    // Fecha en que se realizó la compra en formato "YYYY-MM-DD"
    private String fechaCompra;
    // Monto total de la compra (suma de todos los subtotales de sus detalles)
    private double totalCompra;
    // Método de pago utilizado: "Transferencia" o "Efectivo"
    private String metodoPago;
    // ID del usuario que registró la compra (FK a Usuarios)
    private int usuarioId;
    // ID del proveedor al que se le realizó la compra (FK a Proveedores)
    private int proveedorId;
    // Nombre del proveedor obtenido por JOIN con la tabla Proveedores (no persistido, solo lectura)
    private String nombreProveedor;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idCompra ID de la compra en la BD
     * @param fechaCompra Fecha de la compra en formato "YYYY-MM-DD"
     * @param totalCompra Monto total de la compra
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param usuarioId ID del usuario que registró la compra
     * @param proveedorId ID del proveedor asociado a la compra
     */
    public Compra(int idCompra, String fechaCompra, double totalCompra,
                  String metodoPago, int usuarioId, int proveedorId) {
        // Asignar el ID de la compra
        this.idCompra = idCompra;
        // Asignar la fecha de la compra
        this.fechaCompra = fechaCompra;
        // Asignar el monto total de la compra
        this.totalCompra = totalCompra;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID del usuario que la registró
        this.usuarioId = usuarioId;
        // Asignar el ID del proveedor
        this.proveedorId = proveedorId;
    }

    /**
     * Constructor sin ID (usado al construir una nueva compra antes de persistir).
     * @param fechaCompra Fecha de la compra en formato "YYYY-MM-DD"
     * @param totalCompra Monto total calculado de la compra
     * @param metodoPago Método de pago ("Transferencia" o "Efectivo")
     * @param usuarioId ID del usuario que registra la compra
     * @param proveedorId ID del proveedor al que se le realiza la compra
     */
    public Compra(String fechaCompra, double totalCompra,
                  String metodoPago, int usuarioId, int proveedorId) {
        // Asignar la fecha de la compra
        this.fechaCompra = fechaCompra;
        // Asignar el monto total de la compra
        this.totalCompra = totalCompra;
        // Asignar el método de pago
        this.metodoPago = metodoPago;
        // Asignar el ID del usuario que la registra
        this.usuarioId = usuarioId;
        // Asignar el ID del proveedor
        this.proveedorId = proveedorId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID de la compra.
     * @return ID de la compra
     */
    public int getIdCompra() {
        // Retornar el ID de la compra
        return idCompra;
    }

    /**
     * Establece el ID de la compra (usado tras recuperar la clave generada en INSERT).
     * @param idCompra ID a asignar
     */
    public void setIdCompra(int idCompra) {
        // Asignar el ID de la compra
        this.idCompra = idCompra;
    }

    /**
     * Retorna la fecha de la compra.
     * @return Fecha en formato "YYYY-MM-DD"
     */
    public String getFechaCompra() {
        // Retornar la fecha de la compra
        return fechaCompra;
    }

    /**
     * Establece la fecha de la compra.
     * @param fechaCompra Fecha en formato "YYYY-MM-DD"
     */
    public void setFechaCompra(String fechaCompra) {
        // Asignar la fecha de la compra
        this.fechaCompra = fechaCompra;
    }

    /**
     * Retorna el monto total de la compra.
     * @return Total de la compra
     */
    public double getTotalCompra() {
        // Retornar el monto total de la compra
        return totalCompra;
    }

    /**
     * Establece el monto total de la compra.
     * @param totalCompra Total a asignar
     */
    public void setTotalCompra(double totalCompra) {
        // Asignar el monto total de la compra
        this.totalCompra = totalCompra;
    }

    /**
     * Retorna el método de pago de la compra.
     * @return Método de pago ("Transferencia" o "Efectivo")
     */
    public String getMetodoPago() {
        // Retornar el método de pago
        return metodoPago;
    }

    /**
     * Establece el método de pago de la compra.
     * @param metodoPago Método de pago a asignar
     */
    public void setMetodoPago(String metodoPago) {
        // Asignar el método de pago
        this.metodoPago = metodoPago;
    }

    /**
     * Retorna el ID del usuario que registró la compra.
     * @return ID del usuario
     */
    public int getUsuarioId() {
        // Retornar el ID del usuario
        return usuarioId;
    }

    /**
     * Establece el ID del usuario que registró la compra.
     * @param usuarioId ID del usuario a asignar
     */
    public void setUsuarioId(int usuarioId) {
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    /**
     * Retorna el ID del proveedor asociado a la compra.
     * @return ID del proveedor
     */
    public int getProveedorId() {
        // Retornar el ID del proveedor
        return proveedorId;
    }

    /**
     * Establece el ID del proveedor asociado a la compra.
     * @param proveedorId ID del proveedor a asignar
     */
    public void setProveedorId(int proveedorId) {
        // Asignar el ID del proveedor
        this.proveedorId = proveedorId;
    }

    /**
     * Retorna el nombre del proveedor asociado (poblado por JOIN).
     * @return Nombre del proveedor o null si no se hizo JOIN
     */
    public String getNombreProveedor() {
        // Retornar el nombre del proveedor obtenido del JOIN
        return nombreProveedor;
    }

    /**
     * Establece el nombre del proveedor (poblado por JOIN).
     * @param nombreProveedor Nombre del proveedor a asignar
     */
    public void setNombreProveedor(String nombreProveedor) {
        // Asignar el nombre del proveedor obtenido del JOIN
        this.nombreProveedor = nombreProveedor;
    }
}
