package com.backend.seeders;

import com.backend.config.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Seeder que inserta los tipos de movimientos del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 */
public class SeedTipoMovimientos {

    /** SQL para insertar un tipo de movimiento con su nombre y naturaleza */
    private static final String SQL_INSERT = "INSERT INTO Tipo_Movimientos (MOVIMIENTO, NATURALEZA) VALUES (?, ?)";

    /** Catálogo de tipos de movimientos del sistema: [movimiento, naturaleza] */
    private static final String[][] tipos = {
            {"Venta", "Ingreso"},
            {"Compra", "Egreso"},
            {"Gasto Adicional", "Egreso"}
    };

    /**
     * Inserta los tipos de movimientos iniciales solo si la tabla Tipo_Movimientos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertTipoMovimientos() {
        try (Connection conexion = dbConnection.getConnection()) {

            String sqlVerificacion = "SELECT COUNT(*) FROM Tipo_Movimientos";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [Tipo_Movimientos] Ya existen datos -> omitido");
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
                System.out.println("  [Tipo_Movimientos] Insertados: " + filas);
            }

        } catch (Exception excepcion) {
            System.err.println("Error SeedTipoMovimientos: " + excepcion.getMessage());
        }
    }
}
