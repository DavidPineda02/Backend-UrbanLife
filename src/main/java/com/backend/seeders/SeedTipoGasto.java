package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedTipoGasto {

    private static final String SQL_INSERT = "INSERT INTO Tipo_Gasto (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    private static final String[][] tipos = {
            {"Transporte", "Gastos de envio y transporte de mercancia"},
            {"Impuestos", "Impuestos aplicados a compras o importaciones"},
            {"Almacenamiento", "Costos de bodega y almacenamiento"},
            {"Embalaje", "Costos de empaque y embalaje"},
            {"Otros", "Gastos adicionales no clasificados"}
    };

    public static void insertTipoGasto() {
        try (Connection conn = dbConnection.getConnection()) {

            String checkSQL = "SELECT COUNT(*) FROM Tipo_Gasto";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                 ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("  [Tipo_Gasto] Ya existen datos -> omitido");
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
                System.out.println("  [Tipo_Gasto] Insertados: " + filas);
            }

        } catch (Exception e) {
            System.err.println("Error SeedTipoGasto: " + e.getMessage());
        }
    }
}
