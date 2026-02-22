package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedRoles {

    private static final String SQL_INSERT = "INSERT INTO Roles (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    private static final String[][] roles = {
            {"Administrador", "Acceso total al sistema"},
            {"Vendedor", "Gestion de ventas y clientes"},
            {"Almacenero", "Gestion de inventario y compras"}
    };

    public static void insertRoles() {
        try (Connection conn = dbConnection.getConnection()) {

            String checkSQL = "SELECT COUNT(*) FROM Roles";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                 ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("  [Roles] Ya existen datos -> omitido");
                    return;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                for (String[] rol : roles) {
                    pstmt.setString(1, rol[0]);
                    pstmt.setString(2, rol[1]);
                    filas += pstmt.executeUpdate();
                }
                System.out.println("  [Roles] Insertados: " + filas);
            }

        } catch (Exception e) {
            System.err.println("Error SeedRoles: " + e.getMessage());
        }
    }
}
