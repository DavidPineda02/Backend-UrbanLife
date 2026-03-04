package com.backend.seeders; // Paquete de seeders para datos iniciales

import com.backend.config.dbConnection; // Clase para conexión a BD

import java.sql.Connection; // Interfaz para conexión JDBC
import java.sql.PreparedStatement; // Clase para consultas preparadas
import java.sql.ResultSet; // Interfaz para resultados de consultas

/**
 * Seeder que inserta los tipos de movimientos del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa la clasificación de movimientos financieros del sistema.
 */
public class SeedTipoMovimientos {

    /** SQL para insertar un tipo de movimiento con su nombre y naturaleza */
    private static final String SQL_INSERT = "INSERT INTO Tipo_Movimientos (MOVIMIENTO, NATURALEZA) VALUES (?, ?)"; // Query de inserción

    /** Catálogo de tipos de movimientos del sistema: [movimiento, naturaleza] */
    private static final String[][] tipos = { // Arreglo de tipos de movimientos predefinidos
            {"Venta", "Ingreso"}, // Movimiento de venta (ingreso)
            {"Compra", "Egreso"}, // Movimiento de compra (egreso)
            {"Gasto Adicional", "Egreso"} // Movimiento de gasto adicional (egreso)
    };

    /**
     * Inserta los tipos de movimientos iniciales solo si la tabla Tipo_Movimientos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertTipoMovimientos() { // Método principal de inserción
        try (Connection conexion = dbConnection.getConnection()) { // Obtener conexión con auto-cierre

            String sqlVerificacion = "SELECT COUNT(*) FROM Tipo_Movimientos"; // Query de verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion); // Preparar consulta
                 ResultSet resultado = consultaVerificacion.executeQuery()) { // Ejecutar consulta
                if (resultado.next() && resultado.getInt(1) > 0) { // Verificar si existen datos
                    System.out.println("  [Tipo_Movimientos] Ya existen datos -> omitido"); // Log de omisión
                    return; // Salir del método
                }
            } // El ResultSet y PreparedStatement se cierran automáticamente

            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) { // Preparar inserción
                int filas = 0; // Contador de filas insertadas
                for (String[] tipo : tipos) { // Recorrer tipos de movimientos predefinidos
                    consulta.setString(1, tipo[0]); // Nombre del tipo de movimiento
                    consulta.setString(2, tipo[1]); // Naturaleza del movimiento (Ingreso/Egreso)
                    filas += consulta.executeUpdate(); // Ejecutar INSERT y acumular filas insertadas
                }
                System.out.println("  [Tipo_Movimientos] Insertados: " + filas); // Log de resultados
            } // El PreparedStatement se cierra automáticamente

        } catch (Exception excepcion) { // Capturar errores generales
            System.err.println("Error SeedTipoMovimientos: " + excepcion.getMessage()); // Log de error
        } // La Connection se cierra automáticamente
    }
}
