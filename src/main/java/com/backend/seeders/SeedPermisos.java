package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedPermisos {

    private static final String SQL_INSERT = "INSERT INTO Permisos (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    private static final String[][] permisos = {
            {"Gestionar Usuarios", "Crear, editar y eliminar usuarios del sistema"},
            {"Gestionar Roles", "Asignar y modificar roles de usuarios"},
            {"Gestionar Productos", "Crear, editar y eliminar productos"},
            {"Gestionar Categorias", "Crear, editar y eliminar categorias"},
            {"Gestionar Ventas", "Registrar y consultar ventas"},
            {"Gestionar Compras", "Registrar y consultar compras"},
            {"Gestionar Movimientos", "Registrar y consultar movimientos financieros"},
            {"Gestionar Gastos", "Registrar y consultar gastos adicionales"},
            {"Ver Reportes", "Consultar reportes del sistema"},
            {"Gestionar Perfil", "Editar perfil de empresa y contacto"}
    };

    public static void insertPermisos() {
        try (Connection conexion = dbConnection.getConnection()) {

            String sqlVerificacion = "SELECT COUNT(*) FROM Permisos";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                ResultSet resultado = consultaVerificacion.executeQuery()) {
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [Permisos] Ya existen datos -> omitido");
                    return;
                }
            }

            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                for (String[] permiso : permisos) {
                    consulta.setString(1, permiso[0]);
                    consulta.setString(2, permiso[1]);
                    filas += consulta.executeUpdate();
                }
                System.out.println("  [Permisos] Insertados: " + filas);
            }

        } catch (Exception excepcion) {
            System.err.println("Error SeedPermisos: " + excepcion.getMessage());
        }
    }
}
