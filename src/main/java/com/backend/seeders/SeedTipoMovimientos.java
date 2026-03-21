// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Clase para conexión a BD
import com.backend.config.dbConnection;

// Interfaz para conexión JDBC
import java.sql.Connection;
// Clase para consultas preparadas
import java.sql.PreparedStatement;
// Interfaz para resultados de consultas
import java.sql.ResultSet;

/**
 * Seeder que inserta los tipos de movimientos del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa la clasificación de movimientos financieros del sistema.
 */
public class SeedTipoMovimientos {

    /** SQL para insertar un tipo de movimiento con su nombre y naturaleza */
    private static final String SQL_INSERT = "INSERT INTO Tipos_Movimientos (MOVIMIENTO, NATURALEZA) VALUES (?, ?)";

    /** Catálogo de tipos de movimientos del sistema: [movimiento, naturaleza] */
    private static final String[][] tipos = {
            // Movimiento de venta (ingreso)
            {"Venta", "Ingreso"},
            // Movimiento de compra (egreso)
            {"Compra", "Egreso"},
            // Movimiento de gasto adicional (egreso)
            {"Gasto Adicional", "Egreso"}
    };

    /**
     * Inserta los tipos de movimientos iniciales solo si la tabla Tipos_Movimientos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertTipoMovimientos() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // Query de verificación
            String sqlVerificacion = "SELECT COUNT(*) FROM Tipos_Movimientos";
            // Preparar consulta y ejecutar verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Verificar si existen datos
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [Tipos_Movimientos] Ya existen datos -> omitido");
                    // Salir del método
                    return;
                }
            // El ResultSet y PreparedStatement se cierran automáticamente
            }

            // Preparar inserción
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                // Contador de filas insertadas
                int filas = 0;
                // Recorrer tipos de movimientos predefinidos
                for (String[] tipo : tipos) {
                    // Nombre del tipo de movimiento
                    consulta.setString(1, tipo[0]);
                    // Naturaleza del movimiento (Ingreso/Egreso)
                    consulta.setString(2, tipo[1]);
                    // Ejecutar INSERT y acumular filas insertadas
                    filas += consulta.executeUpdate();
                }
                // Log de resultados
                System.out.println("  [Tipos_Movimientos] Insertados: " + filas);
            // El PreparedStatement se cierra automáticamente
            }

        // Capturar errores generales
        } catch (Exception excepcion) {
            // Log de error
            System.err.println("Error SeedTipoMovimientos: " + excepcion.getMessage());
        // La Connection se cierra automáticamente
        }
    }
}
