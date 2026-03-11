// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un ítem (línea) de una venta.
 * Mapea directamente la tabla Detalle_Venta de la base de datos.
 * Cada DetalleVenta pertenece a una Venta y referencia un Producto.
 */
public class DetalleVenta {

    // Identificador único del detalle de venta en la base de datos (PK)
    private int idDetVenta;
    // Cantidad de unidades vendidas del producto
    private int cantidad;
    // Precio unitario del producto al momento de registrar la venta
    private double precioUnitario;
    // Subtotal calculado del ítem (precioUnitario × cantidad)
    private double subtotal;
    // ID de la venta a la que pertenece este ítem (FK a Venta)
    private int ventaId;
    // ID del producto vendido en este ítem (FK a Producto)
    private int productoId;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idDetVenta ID del detalle en la BD
     * @param cantidad Cantidad de unidades vendidas
     * @param precioUnitario Precio unitario al momento de la venta
     * @param subtotal Subtotal calculado del ítem
     * @param ventaId ID de la venta padre
     * @param productoId ID del producto vendido
     */
    public DetalleVenta(int idDetVenta, int cantidad, double precioUnitario,
                        double subtotal, int ventaId, int productoId) {
        // Asignar el ID del detalle
        this.idDetVenta = idDetVenta;
        // Asignar la cantidad vendida
        this.cantidad = cantidad;
        // Asignar el precio unitario del producto
        this.precioUnitario = precioUnitario;
        // Asignar el subtotal del ítem
        this.subtotal = subtotal;
        // Asignar el ID de la venta padre
        this.ventaId = ventaId;
        // Asignar el ID del producto
        this.productoId = productoId;
    }

    /**
     * Constructor sin ID (usado al construir un ítem nuevo antes de persistir).
     * @param cantidad Cantidad de unidades vendidas
     * @param precioUnitario Precio unitario al momento de la venta
     * @param subtotal Subtotal calculado del ítem
     * @param ventaId ID de la venta a la que pertenece
     * @param productoId ID del producto vendido
     */
    public DetalleVenta(int cantidad, double precioUnitario,
                        double subtotal, int ventaId, int productoId) {
        // Asignar la cantidad vendida
        this.cantidad = cantidad;
        // Asignar el precio unitario del producto
        this.precioUnitario = precioUnitario;
        // Asignar el subtotal del ítem
        this.subtotal = subtotal;
        // Asignar el ID de la venta padre
        this.ventaId = ventaId;
        // Asignar el ID del producto
        this.productoId = productoId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del detalle de venta.
     * @return ID del detalle
     */
    public int getIdDetVenta() {
        // Retornar el ID del detalle
        return idDetVenta;
    }

    /**
     * Establece el ID del detalle de venta.
     * @param idDetVenta ID a asignar
     */
    public void setIdDetVenta(int idDetVenta) {
        // Asignar el ID del detalle
        this.idDetVenta = idDetVenta;
    }

    /**
     * Retorna la cantidad de unidades vendidas.
     * @return Cantidad vendida
     */
    public int getCantidad() {
        // Retornar la cantidad vendida
        return cantidad;
    }

    /**
     * Establece la cantidad de unidades vendidas.
     * @param cantidad Cantidad a asignar
     */
    public void setCantidad(int cantidad) {
        // Asignar la cantidad vendida
        this.cantidad = cantidad;
    }

    /**
     * Retorna el precio unitario del producto al momento de la venta.
     * @return Precio unitario
     */
    public double getPrecioUnitario() {
        // Retornar el precio unitario
        return precioUnitario;
    }

    /**
     * Establece el precio unitario del producto.
     * @param precioUnitario Precio unitario a asignar
     */
    public void setPrecioUnitario(double precioUnitario) {
        // Asignar el precio unitario
        this.precioUnitario = precioUnitario;
    }

    /**
     * Retorna el subtotal del ítem (precioUnitario × cantidad).
     * @return Subtotal del ítem
     */
    public double getSubtotal() {
        // Retornar el subtotal del ítem
        return subtotal;
    }

    /**
     * Establece el subtotal del ítem.
     * @param subtotal Subtotal a asignar
     */
    public void setSubtotal(double subtotal) {
        // Asignar el subtotal del ítem
        this.subtotal = subtotal;
    }

    /**
     * Retorna el ID de la venta a la que pertenece este ítem.
     * @return ID de la venta
     */
    public int getVentaId() {
        // Retornar el ID de la venta
        return ventaId;
    }

    /**
     * Establece el ID de la venta padre.
     * @param ventaId ID de la venta a asignar
     */
    public void setVentaId(int ventaId) {
        // Asignar el ID de la venta padre
        this.ventaId = ventaId;
    }

    /**
     * Retorna el ID del producto vendido.
     * @return ID del producto
     */
    public int getProductoId() {
        // Retornar el ID del producto
        return productoId;
    }

    /**
     * Establece el ID del producto vendido.
     * @param productoId ID del producto a asignar
     */
    public void setProductoId(int productoId) {
        // Asignar el ID del producto
        this.productoId = productoId;
    }
}
