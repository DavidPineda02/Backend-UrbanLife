package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Seeder que inserta los tipos de gasto del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 */
public class SeedTipoGasto {

    /** SQL para insertar un tipo de gasto con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Tipo_Gasto (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    /** Catálogo de tipos de gasto del sistema: [nombre, descripcion] */
    private static final String[][] tipos = {
            {"Transporte", "Gastos de envio y transporte de mercancia"},
            {"Impuestos", "Impuestos aplicados a compras o importaciones"},
            {"Almacenamiento", "Costos de bodega y almacenamiento"},
            {"Embalaje", "Costos de empaque y embalaje"},
            {"Otros", "Gastos adicionales no clasificados"}
    };

    /**
     * Inserta los tipos de gasto iniciales solo si la tabla Tipo_Gasto está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertTipoGasto() {
        try (Connection conexion = dbConnection.getConnection()) {

            String sqlVerificacion = "SELECT COUNT(*) FROM Tipo_Gasto";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [Tipo_Gasto] Ya existen datos -> omitido");
                    return;
                }
            }

            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                for (String[] tipo : tipos) {
                    consulta.setString(1, tipo[0]);
                    consulta.setString(2, tipo[1]);
                    filas += consulta.executeUpdate();
                }
                System.out.println("  [Tipo_Gasto] Insertados: " + filas);
            }

        } catch (Exception excepcion) {
            System.err.println("Error SeedTipoGasto: " + excepcion.getMessage());
        }
    }
}
