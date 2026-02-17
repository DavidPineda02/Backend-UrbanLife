package com.backend.models;

public class RolPermiso {
    private int idRolPermiso;
    private int rolId;
    private int permisosId;

    // public RolPermiso() {
    // }

    public RolPermiso(int idRolPermiso, int rolId, int permisosId) {
        this.idRolPermiso = idRolPermiso;
        this.rolId = rolId;
        this.permisosId = permisosId;
    }

    public RolPermiso(int rolId, int permisosId) {
        this.rolId = rolId;
        this.permisosId = permisosId;
    }

    // Getters y Setters
    public int getIdRolPermiso() {
        return idRolPermiso;
    }

    public void setIdRolPermiso(int idRolPermiso) {
        this.idRolPermiso = idRolPermiso;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public int getPermisosId() {
        return permisosId;
    }

    public void setPermisosId(int permisosId) {
        this.permisosId = permisosId;
    }
}
