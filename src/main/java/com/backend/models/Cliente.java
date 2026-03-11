// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa un cliente del negocio.
 * Mapea directamente la tabla Clientes de la base de datos.
 * Los clientes son registros de contacto — no tienen acceso al sistema.
 */
public class Cliente {

    // Identificador único del cliente en la base de datos (PK)
    private int idCliente;
    // Nombre completo del cliente
    private String nombre;
    // Número de documento de identidad del cliente (único, BIGINT en BD)
    private Long documento;
    // Correo electrónico del cliente (opcional)
    private String correo;
    // Número de teléfono del cliente (opcional)
    private String telefono;
    // Dirección física del cliente (opcional)
    private String direccion;
    // Ciudad de residencia del cliente (opcional)
    private String ciudad;
    // Estado del cliente (true = activo, false = inactivo)
    private boolean estado;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idCliente ID del cliente en la BD
     * @param nombre Nombre completo del cliente
     * @param documento Número de documento de identidad (BIGINT, puede ser null)
     * @param correo Correo electrónico del cliente
     * @param telefono Número de teléfono del cliente
     * @param direccion Dirección física del cliente
     * @param ciudad Ciudad de residencia del cliente
     * @param estado Estado activo/inactivo del cliente
     */
    public Cliente(int idCliente, String nombre, Long documento, String correo,
                   String telefono, String direccion, String ciudad, boolean estado) {
        // Asignar el ID del cliente
        this.idCliente = idCliente;
        // Asignar el nombre del cliente
        this.nombre = nombre;
        // Asignar el documento del cliente
        this.documento = documento;
        // Asignar el correo del cliente
        this.correo = correo;
        // Asignar el teléfono del cliente
        this.telefono = telefono;
        // Asignar la dirección del cliente
        this.direccion = direccion;
        // Asignar la ciudad del cliente
        this.ciudad = ciudad;
        // Asignar el estado del cliente
        this.estado = estado;
    }

    /**
     * Constructor sin ID (usado al crear un nuevo cliente antes de persistir).
     * El estado se establece como activo (true) por defecto.
     * @param nombre Nombre completo del cliente
     * @param documento Número de documento de identidad (BIGINT, puede ser null)
     * @param correo Correo electrónico del cliente
     * @param telefono Número de teléfono del cliente
     * @param direccion Dirección física del cliente
     * @param ciudad Ciudad de residencia del cliente
     */
    public Cliente(String nombre, Long documento, String correo,
                   String telefono, String direccion, String ciudad) {
        // Asignar el nombre del cliente
        this.nombre = nombre;
        // Asignar el documento del cliente
        this.documento = documento;
        // Asignar el correo del cliente
        this.correo = correo;
        // Asignar el teléfono del cliente
        this.telefono = telefono;
        // Asignar la dirección del cliente
        this.direccion = direccion;
        // Asignar la ciudad del cliente
        this.ciudad = ciudad;
        // El cliente se crea activo por defecto
        this.estado = true;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID del cliente.
     * @return ID del cliente
     */
    public int getIdCliente() {
        // Retornar el ID del cliente
        return idCliente;
    }

    /**
     * Establece el ID del cliente (usado tras recuperar la clave generada en INSERT).
     * @param idCliente ID a asignar
     */
    public void setIdCliente(int idCliente) {
        // Asignar el ID del cliente
        this.idCliente = idCliente;
    }

    /**
     * Retorna el nombre completo del cliente.
     * @return Nombre del cliente
     */
    public String getNombre() {
        // Retornar el nombre del cliente
        return nombre;
    }

    /**
     * Establece el nombre completo del cliente.
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        // Asignar el nombre del cliente
        this.nombre = nombre;
    }

    /**
     * Retorna el número de documento de identidad del cliente.
     * @return Documento del cliente (Long, puede ser null)
     */
    public Long getDocumento() {
        // Retornar el documento del cliente
        return documento;
    }

    /**
     * Establece el número de documento de identidad del cliente.
     * @param documento Documento a asignar (Long, puede ser null)
     */
    public void setDocumento(Long documento) {
        // Asignar el documento del cliente
        this.documento = documento;
    }

    /**
     * Retorna el correo electrónico del cliente.
     * @return Correo del cliente
     */
    public String getCorreo() {
        // Retornar el correo del cliente
        return correo;
    }

    /**
     * Establece el correo electrónico del cliente.
     * @param correo Correo a asignar
     */
    public void setCorreo(String correo) {
        // Asignar el correo del cliente
        this.correo = correo;
    }

    /**
     * Retorna el número de teléfono del cliente.
     * @return Teléfono del cliente
     */
    public String getTelefono() {
        // Retornar el teléfono del cliente
        return telefono;
    }

    /**
     * Establece el número de teléfono del cliente.
     * @param telefono Teléfono a asignar
     */
    public void setTelefono(String telefono) {
        // Asignar el teléfono del cliente
        this.telefono = telefono;
    }

    /**
     * Retorna la dirección física del cliente.
     * @return Dirección del cliente
     */
    public String getDireccion() {
        // Retornar la dirección del cliente
        return direccion;
    }

    /**
     * Establece la dirección física del cliente.
     * @param direccion Dirección a asignar
     */
    public void setDireccion(String direccion) {
        // Asignar la dirección del cliente
        this.direccion = direccion;
    }

    /**
     * Retorna la ciudad de residencia del cliente.
     * @return Ciudad del cliente
     */
    public String getCiudad() {
        // Retornar la ciudad del cliente
        return ciudad;
    }

    /**
     * Establece la ciudad de residencia del cliente.
     * @param ciudad Ciudad a asignar
     */
    public void setCiudad(String ciudad) {
        // Asignar la ciudad del cliente
        this.ciudad = ciudad;
    }

    /**
     * Retorna el estado del cliente (activo/inactivo).
     * @return true si el cliente está activo
     */
    public boolean isEstado() {
        // Retornar el estado del cliente
        return estado;
    }

    /**
     * Establece el estado del cliente.
     * @param estado Estado a asignar (true = activo)
     */
    public void setEstado(boolean estado) {
        // Asignar el estado del cliente
        this.estado = estado;
    }
}
