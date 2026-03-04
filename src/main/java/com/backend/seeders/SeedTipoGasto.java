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
 * Seeder que inserta los tipos de gasto del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa la categorización de gastos operativos del sistema.
 */
public class SeedTipoGasto {

    /** SQL para insertar un tipo de gasto con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Tipo_Gasto (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    /** Catálogo de tipos de gasto del sistema: [nombre, descripcion] */
    private static final String[][] tipos = {
            // Gastos de transporte
            {"Transporte", "Gastos de envio y transporte de mercancia"},
            // Gastos de impuestos
            {"Impuestos", "Impuestos aplicados a compras o importaciones"},
            // Gastos de almacenamiento
            {"Almacenamiento", "Costos de bodega y almacenamiento"},
            // Gastos de embalaje
            {"Embalaje", "Costos de empaque y embalaje"},
            // Gastos misceláneos
            {"Otros", "Gastos adicionales no clasificados"}
    };

    /**
     * Inserta los tipos de gasto iniciales solo si la tabla Tipo_Gasto está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertTipoGasto() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // Query de verificación
            String sqlVerificacion = "SELECT COUNT(*) FROM Tipo_Gasto";
            // Preparar consulta y ejecutar verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Verificar si existen datos
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [Tipo_Gasto] Ya existen datos -> omitido");
                    // Salir del método
                    return;
                }
            // El ResultSet y PreparedStatement se cierran automáticamente
            }

            // Preparar inserción
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                // Contador de filas insertadas
                int filas = 0;
                // Recorrer tipos de gasto predefinidos
                for (String[] tipo : tipos) {
                    // Nombre del tipo de gasto
                    consulta.setString(1, tipo[0]);
                    // Descripción del tipo de gasto
                    consulta.setString(2, tipo[1]);
                    // Ejecutar INSERT y acumular filas insertadas
                    filas += consulta.executeUpdate();
                }
                // Log de resultados
                System.out.println("  [Tipo_Gasto] Insertados: " + filas);
            // El PreparedStatement se cierra automáticamente
            }

        // Capturar errores generales
        } catch (Exception excepcion) {
            // Log de error
            System.err.println("Error SeedTipoGasto: " + excepcion.getMessage());
        // La Connection se cierra automáticamente
        }
    }
}
