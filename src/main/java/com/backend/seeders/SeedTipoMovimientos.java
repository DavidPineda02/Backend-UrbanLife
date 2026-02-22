package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedTipoMovimientos {

    private static final String SQL_INSERT = "INSERT INTO Tipo_Movimientos (MOVIMIENTO, NATURALEZA) VALUES (?, ?)";

    private static final String[][] tipos = {
            {"Venta", "Ingreso"},
            {"Compra", "Egreso"},
            {"Gasto Adicional", "Egreso"}
    };

    public static void insertTipoMovimientos() {
        try (Connection conn = dbConnection.getConnection()) {

            String checkSQL = "SELECT COUNT(*) FROM Tipo_Movimientos";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                 ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("  [Tipo_Movimientos] Ya existen datos -> omitido");
                    return;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                for (String[] tipo : tipos) {
                    pstmt.setString(1, tipo[0]);
                    pstmt.setString(2, tipo[1]);
                    filas += pstmt.executeUpdate();
                }
                System.out.println("  [Tipo_Movimientos] Insertados: " + filas);
            }

        } catch (Exception e) {
            System.err.println("Error SeedTipoMovimientos: " + e.getMessage());
        }
    }
}
