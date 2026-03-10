// Paquete de modelos (entidades) de la aplicación
package com.backend.models;

/**
 * Entidad que representa la relación muchos-a-muchos entre usuarios y roles.
 * Mapea directamente la tabla Usuario_Rol de la base de datos.
 * Permite que un usuario tenga múltiples roles asignados.
 */
public class UsuarioRol {

    // Identificador único de la relación usuario-rol (PK)
    private int idUsuarioRol;
    // ID del usuario que recibe el rol (FK → Usuarios)
    private int usuarioId;
    // ID del rol asignado al usuario (FK → Roles)
    private int rolId;

    /**
     * Constructor completo con ID (usado al leer desde la base de datos).
     * @param idUsuarioRol ID de la relación en la BD
     * @param usuarioId ID del usuario
     * @param rolId ID del rol asignado
     */
    public UsuarioRol(int idUsuarioRol, int usuarioId, int rolId) {
        // Asignar el ID de la relación usuario-rol
        this.idUsuarioRol = idUsuarioRol;
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
        // Asignar el ID del rol
        this.rolId = rolId;
    }

    /**
     * Constructor sin ID (usado al crear una nueva relación antes de persistir).
     * @param usuarioId ID del usuario al que se asigna el rol
     * @param rolId ID del rol a asignar
     */
    public UsuarioRol(int usuarioId, int rolId) {
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
        // Asignar el ID del rol
        this.rolId = rolId;
    }

    // ========== Getters y Setters ==========

    /**
     * Retorna el ID de la relación usuario-rol.
     * @return ID de la relación
     */
    public int getIdUsuarioRol() {
        // Retornar el ID de la relación usuario-rol
        return idUsuarioRol;
    }

    /**
     * Establece el ID de la relación (usado tras recuperar la clave generada en INSERT).
     * @param idUsuarioRol ID a asignar
     */
    public void setIdUsuarioRol(int idUsuarioRol) {
        // Asignar el ID de la relación usuario-rol
        this.idUsuarioRol = idUsuarioRol;
    }

    /**
     * Retorna el ID del usuario.
     * @return ID del usuario
     */
    public int getUsuarioId() {
        // Retornar el ID del usuario
        return usuarioId;
    }

    /**
     * Establece el ID del usuario.
     * @param usuarioId ID a asignar
     */
    public void setUsuarioId(int usuarioId) {
        // Asignar el ID del usuario
        this.usuarioId = usuarioId;
    }

    /**
     * Retorna el ID del rol asignado.
     * @return ID del rol
     */
    public int getRolId() {
        // Retornar el ID del rol
        return rolId;
    }

    /**
     * Establece el ID del rol.
     * @param rolId ID a asignar
     */
    public void setRolId(int rolId) {
        // Asignar el ID del rol
        this.rolId = rolId;
    }
}
