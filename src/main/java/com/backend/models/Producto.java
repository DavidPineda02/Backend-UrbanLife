// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un producto del inventario.
 * Mapea directamente la tabla Producto de la base de datos.
 * Pertenece a una categoría y mantiene su precio, costo promedio y stock.
 */
public class Producto {

    // Identificador único del producto en la base de datos (PK)
    private int idProducto;
    // Nombre del producto (ej: "Camiseta Polo")
    private String nombre;
    // Descripción detallada del producto
    private String descripcion;
    // Precio de venta al público del producto
    private double precioVenta;
    // Costo promedio de adquisición del producto
    private double costoPromedio;
    // Cantidad de unidades disponibles en inventario
    private int stock;
    // Estado del producto (true = activo, false = inactivo)
    private boolean estado;
    // ID de la categoría a la que pertenece el producto (FK → Categorias)
    private int categoriaId;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idProducto ID del producto en la BD
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @param precioVenta Precio de venta al público
     * @param costoPromedio Costo promedio de adquisición
     * @param stock Cantidad disponible en inventario
     * @param estado Estado activo/inactivo del producto
     * @param categoriaId ID de la categoría asociada
     */
    public Producto(int idProducto, String nombre, String descripcion, double precioVenta,
            double costoPromedio, int stock, boolean estado, int categoriaId) {
        // Asignar el ID del producto
        this.idProducto = idProducto;
        // Asignar el nombre del producto
        this.nombre = nombre;
        // Asignar la descripción del producto
        this.descripcion = descripcion;
        // Asignar el precio de venta del producto
        this.precioVenta = precioVenta;
        // Asignar el costo promedio del producto
        this.costoPromedio = costoPromedio;
        // Asignar el stock disponible del producto
        this.stock = stock;
        // Asignar el estado del producto
        this.estado = estado;
        // Asignar el ID de la categoría asociada
        this.categoriaId = categoriaId;
    }

    /**
     * Constructor sin ID (usado al crear un nuevo producto antes de persistir).
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @param precioVenta Precio de venta al público
     * @param costoPromedio Costo promedio de adquisición
     * @param stock Cantidad disponible en inventario
     * @param estado Estado activo/inactivo del producto
     * @param categoriaId ID de la categoría asociada
     */
    public Producto(String nombre, String descripcion, double precioVenta,
            double costoPromedio, int stock, boolean estado, int categoriaId) {
        // Asignar el nombre del producto
        this.nombre = nombre;
        // Asignar la descripción del producto
        this.descripcion = descripcion;
        // Asignar el precio de venta del producto
        this.precioVenta = precioVenta;
        // Asignar el costo promedio del producto
        this.costoPromedio = costoPromedio;
        // Asignar el stock disponible del producto
        this.stock = stock;
        // Asignar el estado del producto
        this.estado = estado;
        // Asignar el ID de la categoría asociada
        this.categoriaId = categoriaId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del producto.
     * @return ID del producto
     */
    public int getIdProducto() {
        // Retornar el ID del producto
        return idProducto;
    }

    /**
     * Establece el ID del producto (usado tras recuperar la clave generada en INSERT).
     * @param idProducto ID a asignar
     */
    public void setIdProducto(int idProducto) {
        // Asignar el ID del producto
        this.idProducto = idProducto;
    }

    /**
     * Retorna el nombre del producto.
     * @return Nombre del producto
     */
    public String getNombre() {
        // Retornar el nombre del producto
        return nombre;
    }

    /**
     * Establece el nombre del producto.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre del producto
        this.nombre = nombre;
    }

    /**
     * Retorna la descripción del producto.
     * @return Descripción del producto
     */
    public String getDescripcion() {
        // Retornar la descripción del producto
        return descripcion;
    }

    /**
     * Establece la descripción del producto.
     * @param descripcion Descripción a asignar
     */
    public void setDescripcion(String descripcion) {
        // Asignar la descripción del producto
        this.descripcion = descripcion;
    }

    /**
     * Retorna el precio de venta del producto.
     * @return Precio de venta al público
     */
    public double getPrecioVenta() {
        // Retornar el precio de venta del producto
        return precioVenta;
    }

    /**
     * Establece el precio de venta del producto.
     * @param precioVenta Precio a asignar
     */
    public void setPrecioVenta(double precioVenta) {
        // Asignar el precio de venta del producto
        this.precioVenta = precioVenta;
    }

    /**
     * Retorna el costo promedio de adquisición del producto.
     * @return Costo promedio del producto
     */
    public double getCostoPromedio() {
        // Retornar el costo promedio del producto
        return costoPromedio;
    }

    /**
     * Establece el costo promedio de adquisición del producto.
     * @param costoPromedio Costo a asignar
     */
    public void setCostoPromedio(double costoPromedio) {
        // Asignar el costo promedio del producto
        this.costoPromedio = costoPromedio;
    }

    /**
     * Retorna el stock disponible del producto.
     * @return Cantidad de unidades en inventario
     */
    public int getStock() {
        // Retornar el stock disponible del producto
        return stock;
    }

    /**
     * Establece el stock disponible del producto.
     * @param stock Cantidad de unidades a asignar
     */
    public void setStock(int stock) {
        // Asignar el stock disponible del producto
        this.stock = stock;
    }

    /**
     * Retorna el estado del producto (activo/inactivo).
     * @return true si el producto está activo
     */
    public boolean isEstado() {
        // Retornar el estado del producto
        return estado;
    }

    /**
     * Establece el estado del producto.
     * @param estado Estado a asignar (true = activo)
     */
    public void setEstado(boolean estado) {
        // Asignar el estado del producto
        this.estado = estado;
    }

    /**
     * Retorna el ID de la categoría a la que pertenece el producto.
     * @return ID de la categoría asociada
     */
    public int getCategoriaId() {
        // Retornar el ID de la categoría asociada
        return categoriaId;
    }

    /**
     * Establece el ID de la categoría a la que pertenece el producto.
     * @param categoriaId ID de la categoría a asignar
     */
    public void setCategoriaId(int categoriaId) {
        // Asignar el ID de la categoría asociada
        this.categoriaId = categoriaId;
    }
}
