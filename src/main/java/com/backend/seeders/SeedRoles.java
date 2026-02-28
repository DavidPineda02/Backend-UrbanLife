package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedRoles {

    private static final String SQL_INSERT = "INSERT INTO Roles (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    private static final String[][] roles = {
            {"SUPER_ADMIN", "Acceso total al sistema incluyendo configuracion tecnica"},
            {"ADMIN", "Gestion completa del negocio: ventas, inventario, compras y reportes"},
            {"EMPLEADO", "Acceso operativo limitado a funciones del dia a dia"}
    };

    public static void insertRoles() {
        try (Connection conexion = dbConnection.getConnection()) {

            String sqlVerificacion = "SELECT COUNT(*) FROM Roles";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [Roles] Ya existen datos -> omitido");
                    return;
                }
            }

            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                for (String[] rol : roles) {
                    consulta.setString(1, rol[0]);
                    consulta.setString(2, rol[1]);
                    filas += consulta.executeUpdate();
                }
                System.out.println("  [Roles] Insertados: " + filas);
            }

        } catch (Exception excepcion) {
            System.err.println("Error SeedRoles: " + excepcion.getMessage());
        }
    }
}
