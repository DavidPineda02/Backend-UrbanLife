package com.backend.seeders; // Paquete de seeders para datos iniciales

// Para obtener la conexion a la base de datos
import com.backend.config.dbConnection; // Clase para conexión a BD

// Para la conexion JDBC
import java.sql.Connection; // Interfaz para conexión JDBC
// Para las consultas parametrizadas
import java.sql.PreparedStatement; // Clase para consultas preparadas
// Para leer el resultado de la consulta de verificacion
import java.sql.ResultSet; // Interfaz para resultados de consultas

/**
 * Seeder que inserta los roles iniciales del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa la estructura jerárquica de roles del sistema.
 */
public class SeedRoles {

    /** SQL para insertar un rol con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Roles (NOMBRE, DESCRIPCION) VALUES (?, ?)"; // Query de inserción

    /** Datos de los 3 roles del sistema: [nombre, descripcion] */
    private static final String[][] roles = { // Arreglo de roles predefinidos
            {"SUPER_ADMIN", "Acceso total al sistema incluyendo configuracion tecnica"}, // Rol de super administrador
            {"ADMIN", "Gestion completa del negocio: ventas, inventario, compras y reportes"}, // Rol de administrador
            {"EMPLEADO", "Acceso operativo limitado a funciones del dia a dia"} // Rol de empleado
    };

    /**
     * Inserta los roles iniciales solo si la tabla Roles está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertRoles() { // Método principal de inserción
        try (Connection conexion = dbConnection.getConnection()) { // Obtener conexión con auto-cierre

            // Verificar si ya existen roles para evitar insertar duplicados en cada inicio
            String sqlVerificacion = "SELECT COUNT(*) FROM Roles"; // Query de verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion); // Preparar consulta
                 ResultSet resultado = consultaVerificacion.executeQuery()) { // Ejecutar consulta
                // Si ya hay al menos un rol, omitir la insercion
                if (resultado.next() && resultado.getInt(1) > 0) { // Verificar si existen datos
                    System.out.println("  [Roles] Ya existen datos -> omitido"); // Log de omisión
                    return; // Salir del método
                }
            } // El ResultSet y PreparedStatement se cierran automáticamente

            // Preparar el statement de insercion una vez y reutilizarlo para cada rol
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) { // Preparar inserción
                int filas = 0; // Contador de filas insertadas
                // Iterar sobre cada rol definido en el arreglo
                for (String[] rol : roles) { // Recorrer roles predefinidos
                    consulta.setString(1, rol[0]); // Nombre del rol (ej: "SUPER_ADMIN")
                    consulta.setString(2, rol[1]); // Descripcion del rol
                    filas += consulta.executeUpdate(); // Ejecutar INSERT y acumular filas insertadas
                }
                System.out.println("  [Roles] Insertados: " + filas); // Log de resultados
            } // El PreparedStatement se cierra automáticamente

        } catch (Exception excepcion) { // Capturar errores generales
            System.err.println("Error SeedRoles: " + excepcion.getMessage()); // Log de error
        } // La Connection se cierra automáticamente
    }
}
