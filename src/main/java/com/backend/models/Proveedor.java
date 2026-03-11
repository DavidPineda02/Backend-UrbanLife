// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un proveedor del negocio.
 * Mapea directamente la tabla Proveedores de la base de datos.
 * Los proveedores son registros de contacto — no tienen acceso al sistema.
 */
public class Proveedor {

    // Identificador único del proveedor en la base de datos (PK)
    private int idProveedor;
    // Nombre comercial o de contacto del proveedor
    private String nombre;
    // Razón social legal del proveedor (nombre oficial de la empresa)
    private String razonSocial;
    // Número de identificación tributaria del proveedor (único)
    private String nit;
    // Correo electrónico del proveedor (opcional)
    private String correo;
    // Número de teléfono del proveedor (opcional)
    private String telefono;
    // Dirección física del proveedor (opcional)
    private String direccion;
    // Ciudad donde opera el proveedor (opcional)
    private String ciudad;
    // Estado del proveedor (true = activo, false = inactivo)
    private boolean estado;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idProveedor ID del proveedor en la BD
     * @param nombre Nombre comercial del proveedor
     * @param razonSocial Razón social legal del proveedor
     * @param nit Número de identificación tributaria
     * @param correo Correo electrónico del proveedor
     * @param telefono Número de teléfono del proveedor
     * @param direccion Dirección física del proveedor
     * @param ciudad Ciudad donde opera el proveedor
     * @param estado Estado activo/inactivo del proveedor
     */
    public Proveedor(int idProveedor, String nombre, String razonSocial, String nit,
                     String correo, String telefono, String direccion, String ciudad, boolean estado) {
        // Asignar el ID del proveedor
        this.idProveedor = idProveedor;
        // Asignar el nombre comercial del proveedor
        this.nombre = nombre;
        // Asignar la razón social del proveedor
        this.razonSocial = razonSocial;
        // Asignar el NIT del proveedor
        this.nit = nit;
        // Asignar el correo del proveedor
        this.correo = correo;
        // Asignar el teléfono del proveedor
        this.telefono = telefono;
        // Asignar la dirección del proveedor
        this.direccion = direccion;
        // Asignar la ciudad del proveedor
        this.ciudad = ciudad;
        // Asignar el estado del proveedor
        this.estado = estado;
    }

    /**
     * Constructor sin ID (usado al crear un nuevo proveedor antes de persistir).
     * El estado se establece como activo (true) por defecto.
     * @param nombre Nombre comercial del proveedor
     * @param razonSocial Razón social legal del proveedor
     * @param nit Número de identificación tributaria
     * @param correo Correo electrónico del proveedor
     * @param telefono Número de teléfono del proveedor
     * @param direccion Dirección física del proveedor
     * @param ciudad Ciudad donde opera el proveedor
     */
    public Proveedor(String nombre, String razonSocial, String nit,
                     String correo, String telefono, String direccion, String ciudad) {
        // Asignar el nombre comercial del proveedor
        this.nombre = nombre;
        // Asignar la razón social del proveedor
        this.razonSocial = razonSocial;
        // Asignar el NIT del proveedor
        this.nit = nit;
        // Asignar el correo del proveedor
        this.correo = correo;
        // Asignar el teléfono del proveedor
        this.telefono = telefono;
        // Asignar la dirección del proveedor
        this.direccion = direccion;
        // Asignar la ciudad del proveedor
        this.ciudad = ciudad;
        // El proveedor se crea activo por defecto
        this.estado = true;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del proveedor.
     * @return ID del proveedor
     */
    public int getIdProveedor() {
        // Retornar el ID del proveedor
        return idProveedor;
    }

    /**
     * Establece el ID del proveedor (usado tras recuperar la clave generada en INSERT).
     * @param idProveedor ID a asignar
     */
    public void setIdProveedor(int idProveedor) {
        // Asignar el ID del proveedor
        this.idProveedor = idProveedor;
    }

    /**
     * Retorna el nombre comercial del proveedor.
     * @return Nombre del proveedor
     */
    public String getNombre() {
        // Retornar el nombre del proveedor
        return nombre;
    }

    /**
     * Establece el nombre comercial del proveedor.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre del proveedor
        this.nombre = nombre;
    }

    /**
     * Retorna la razón social legal del proveedor.
     * @return Razón social del proveedor
     */
    public String getRazonSocial() {
        // Retornar la razón social del proveedor
        return razonSocial;
    }

    /**
     * Establece la razón social legal del proveedor.
     * @param razonSocial Razón social a asignar
     */
    public void setRazonSocial(String razonSocial) {
        // Asignar la razón social del proveedor
        this.razonSocial = razonSocial;
    }

    /**
     * Retorna el número de identificación tributaria del proveedor.
     * @return NIT del proveedor
     */
    public String getNit() {
        // Retornar el NIT del proveedor
        return nit;
    }

    /**
     * Establece el número de identificación tributaria del proveedor.
     * @param nit NIT a asignar
     */
    public void setNit(String nit) {
        // Asignar el NIT del proveedor
        this.nit = nit;
    }

    /**
     * Retorna el correo electrónico del proveedor.
     * @return Correo del proveedor
     */
    public String getCorreo() {
        // Retornar el correo del proveedor
        return correo;
    }

    /**
     * Establece el correo electrónico del proveedor.
     * @param correo Correo a asignar
     */
    public void setCorreo(String correo) {
        // Asignar el correo del proveedor
        this.correo = correo;
    }

    /**
     * Retorna el número de teléfono del proveedor.
     * @return Teléfono del proveedor
     */
    public String getTelefono() {
        // Retornar el teléfono del proveedor
        return telefono;
    }

    /**
     * Establece el número de teléfono del proveedor.
     * @param telefono Teléfono a asignar
     */
    public void setTelefono(String telefono) {
        // Asignar el teléfono del proveedor
        this.telefono = telefono;
    }

    /**
     * Retorna la dirección física del proveedor.
     * @return Dirección del proveedor
     */
    public String getDireccion() {
        // Retornar la dirección del proveedor
        return direccion;
    }

    /**
     * Establece la dirección física del proveedor.
     * @param direccion Dirección a asignar
     */
    public void setDireccion(String direccion) {
        // Asignar la dirección del proveedor
        this.direccion = direccion;
    }

    /**
     * Retorna la ciudad donde opera el proveedor.
     * @return Ciudad del proveedor
     */
    public String getCiudad() {
        // Retornar la ciudad del proveedor
        return ciudad;
    }

    /**
     * Establece la ciudad donde opera el proveedor.
     * @param ciudad Ciudad a asignar
     */
    public void setCiudad(String ciudad) {
        // Asignar la ciudad del proveedor
        this.ciudad = ciudad;
    }

    /**
     * Retorna el estado del proveedor (activo/inactivo).
     * @return true si el proveedor está activo
     */
    public boolean isEstado() {
        // Retornar el estado del proveedor
        return estado;
    }

    /**
     * Establece el estado del proveedor.
     * @param estado Estado a asignar (true = activo)
     */
    public void setEstado(boolean estado) {
        // Asignar el estado del proveedor
        this.estado = estado;
    }
}
