// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un ítem (línea) de una compra a proveedor.
 * Mapea directamente la tabla Detalle_Compra de la base de datos.
 * Cada DetalleCompra pertenece a una Compra y referencia un Producto.
 */
public class DetalleCompra {

    // Identificador único del detalle de compra en la base de datos (PK)
    private int idDetCompra;
    // Cantidad de unidades compradas del producto
    private int cantidad;
    // Costo unitario del producto al momento de registrar la compra
    private double costoUnitario;
    // Subtotal calculado del ítem (costoUnitario × cantidad)
    private double subtotal;
    // ID de la compra a la que pertenece este ítem (FK a Compra)
    private int compraId;
    // ID del producto comprado en este ítem (FK a Producto)
    private int productoId;
    // Costo promedio ponderado calculado en el Service antes de persistir (no se mapea a la BD)
    // Se usa para que el DAO aplique el UPDATE sin necesidad de hacer un SELECT extra dentro de la transacción
    private double costoPromedioNuevo;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idDetCompra ID del detalle en la BD
     * @param cantidad Cantidad de unidades compradas
     * @param costoUnitario Costo unitario al momento de la compra
     * @param subtotal Subtotal calculado del ítem
     * @param compraId ID de la compra padre
     * @param productoId ID del producto comprado
     */
    public DetalleCompra(int idDetCompra, int cantidad, double costoUnitario,
                         double subtotal, int compraId, int productoId) {
        // Asignar el ID del detalle
        this.idDetCompra = idDetCompra;
        // Asignar la cantidad comprada
        this.cantidad = cantidad;
        // Asignar el costo unitario del producto
        this.costoUnitario = costoUnitario;
        // Asignar el subtotal del ítem
        this.subtotal = subtotal;
        // Asignar el ID de la compra padre
        this.compraId = compraId;
        // Asignar el ID del producto
        this.productoId = productoId;
    }

    /**
     * Constructor sin ID (usado al construir un ítem nuevo antes de persistir).
     * @param cantidad Cantidad de unidades compradas
     * @param costoUnitario Costo unitario al momento de la compra
     * @param subtotal Subtotal calculado del ítem
     * @param compraId ID de la compra a la que pertenece
     * @param productoId ID del producto comprado
     */
    public DetalleCompra(int cantidad, double costoUnitario,
                         double subtotal, int compraId, int productoId) {
        // Asignar la cantidad comprada
        this.cantidad = cantidad;
        // Asignar el costo unitario del producto
        this.costoUnitario = costoUnitario;
        // Asignar el subtotal del ítem
        this.subtotal = subtotal;
        // Asignar el ID de la compra padre
        this.compraId = compraId;
        // Asignar el ID del producto
        this.productoId = productoId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del detalle de compra.
     * @return ID del detalle
     */
    public int getIdDetCompra() {
        // Retornar el ID del detalle
        return idDetCompra;
    }

    /**
     * Establece el ID del detalle de compra.
     * @param idDetCompra ID a asignar
     */
    public void setIdDetCompra(int idDetCompra) {
        // Asignar el ID del detalle
        this.idDetCompra = idDetCompra;
    }

    /**
     * Retorna la cantidad de unidades compradas.
     * @return Cantidad comprada
     */
    public int getCantidad() {
        // Retornar la cantidad comprada
        return cantidad;
    }

    /**
     * Establece la cantidad de unidades compradas.
     * @param cantidad Cantidad a asignar
     */
    public void setCantidad(int cantidad) {
        // Asignar la cantidad comprada
        this.cantidad = cantidad;
    }

    /**
     * Retorna el costo unitario del producto al momento de la compra.
     * @return Costo unitario
     */
    public double getCostoUnitario() {
        // Retornar el costo unitario
        return costoUnitario;
    }

    /**
     * Establece el costo unitario del producto.
     * @param costoUnitario Costo unitario a asignar
     */
    public void setCostoUnitario(double costoUnitario) {
        // Asignar el costo unitario
        this.costoUnitario = costoUnitario;
    }

    /**
     * Retorna el subtotal del ítem (costoUnitario × cantidad).
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
     * Retorna el ID de la compra a la que pertenece este ítem.
     * @return ID de la compra
     */
    public int getCompraId() {
        // Retornar el ID de la compra
        return compraId;
    }

    /**
     * Establece el ID de la compra padre.
     * @param compraId ID de la compra a asignar
     */
    public void setCompraId(int compraId) {
        // Asignar el ID de la compra padre
        this.compraId = compraId;
    }

    /**
     * Retorna el ID del producto comprado.
     * @return ID del producto
     */
    public int getProductoId() {
        // Retornar el ID del producto
        return productoId;
    }

    /**
     * Establece el ID del producto comprado.
     * @param productoId ID del producto a asignar
     */
    public void setProductoId(int productoId) {
        // Asignar el ID del producto
        this.productoId = productoId;
    }

    /**
     * Retorna el costo promedio ponderado calculado por el Service antes de persistir.
     * Este valor no está en la BD; se usa para que CompraDAO aplique el UPDATE sin recalcular.
     * @return Costo promedio nuevo calculado en CompraService
     */
    public double getCostoPromedioNuevo() {
        // Retornar el costo promedio calculado por el servicio
        return costoPromedioNuevo;
    }

    /**
     * Establece el costo promedio ponderado calculado en CompraService.
     * Se llama justo antes de pasar el detalle al DAO para persistir.
     * @param costoPromedioNuevo Valor calculado: (stockActual * costoActual + cantidad * costoCompra) / stockTotal
     */
    public void setCostoPromedioNuevo(double costoPromedioNuevo) {
        // Asignar el costo promedio calculado por el servicio
        this.costoPromedioNuevo = costoPromedioNuevo;
    }
}
