package com.backend.seeders; // Paquete de seeders para datos iniciales

import com.backend.config.dbConnection; // Clase para conexión a BD

import java.sql.Connection; // Interfaz para conexión JDBC
import java.sql.PreparedStatement; // Clase para consultas preparadas
import java.sql.ResultSet; // Interfaz para resultados de consultas

/**
 * Seeder que inserta los tipos de gasto del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa la categorización de gastos operativos del sistema.
 */
public class SeedTipoGasto {

    /** SQL para insertar un tipo de gasto con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Tipo_Gasto (NOMBRE, DESCRIPCION) VALUES (?, ?)"; // Query de inserción

    /** Catálogo de tipos de gasto del sistema: [nombre, descripcion] */
    private static final String[][] tipos = { // Arreglo de tipos de gasto predefinidos
            {"Transporte", "Gastos de envio y transporte de mercancia"}, // Gastos de transporte
            {"Impuestos", "Impuestos aplicados a compras o importaciones"}, // Gastos de impuestos
            {"Almacenamiento", "Costos de bodega y almacenamiento"}, // Gastos de almacenamiento
            {"Embalaje", "Costos de empaque y embalaje"}, // Gastos de embalaje
            {"Otros", "Gastos adicionales no clasificados"} // Gastos misceláneos
    };

    /**
     * Inserta los tipos de gasto iniciales solo si la tabla Tipo_Gasto está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertTipoGasto() { // Método principal de inserción
        try (Connection conexion = dbConnection.getConnection()) { // Obtener conexión con auto-cierre

            String sqlVerificacion = "SELECT COUNT(*) FROM Tipo_Gasto"; // Query de verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion); // Preparar consulta
                 ResultSet resultado = consultaVerificacion.executeQuery()) { // Ejecutar consulta
                if (resultado.next() && resultado.getInt(1) > 0) { // Verificar si existen datos
                    System.out.println("  [Tipo_Gasto] Ya existen datos -> omitido"); // Log de omisión
                    return; // Salir del método
                }
            } // El ResultSet y PreparedStatement se cierran automáticamente

            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) { // Preparar inserción
                int filas = 0; // Contador de filas insertadas
                for (String[] tipo : tipos) { // Recorrer tipos de gasto predefinidos
                    consulta.setString(1, tipo[0]); // Nombre del tipo de gasto
                    consulta.setString(2, tipo[1]); // Descripción del tipo de gasto
                    filas += consulta.executeUpdate(); // Ejecutar INSERT y acumular filas insertadas
                }
                System.out.println("  [Tipo_Gasto] Insertados: " + filas); // Log de resultados
            } // El PreparedStatement se cierra automáticamente

        } catch (Exception excepcion) { // Capturar errores generales
            System.err.println("Error SeedTipoGasto: " + excepcion.getMessage()); // Log de error
        } // La Connection se cierra automáticamente
    }
}
