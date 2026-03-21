// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Para obtener la conexion a la base de datos
import com.backend.config.dbConnection;

// Para la conexion JDBC
import java.sql.Connection;
// Para las consultas parametrizadas
import java.sql.PreparedStatement;
// Para leer el resultado de la consulta de verificacion
import java.sql.ResultSet;

/**
 * Seeder que inserta los roles iniciales del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa la estructura jerárquica de roles del sistema.
 */
public class SeedRoles {

    /** SQL para insertar un rol con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Roles (NOMBRE_ROL, DESCRIPCION_ROL) VALUES (?, ?)";

    /** Datos de los 3 roles del sistema: [nombre, descripcion] */
    private static final String[][] roles = {
            // Rol de super administrador
            {"SUPER_ADMIN", "Acceso total al sistema incluyendo configuracion tecnica"},
            // Rol de administrador
            {"ADMIN", "Gestion completa del negocio: ventas, inventario, compras y reportes"},
            // Rol de empleado
            {"EMPLEADO", "Acceso operativo limitado a funciones del dia a dia"}
    };

    /**
     * Inserta los roles iniciales solo si la tabla Roles está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertRoles() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // Verificar si ya existen roles para evitar insertar duplicados en cada inicio
            String sqlVerificacion = "SELECT COUNT(*) FROM Roles";
            // Preparar consulta y ejecutar verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Si ya hay al menos un rol, omitir la insercion
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [Roles] Ya existen datos -> omitido");
                    // Salir del método
                    return;
                }
            // El ResultSet y PreparedStatement se cierran automáticamente
            }

            // Preparar el statement de insercion una vez y reutilizarlo para cada rol
            // Preparar inserción
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                // Contador de filas insertadas
                int filas = 0;
                // Iterar sobre cada rol definido en el arreglo
                // Recorrer roles predefinidos
                for (String[] rol : roles) {
                    // Nombre del rol (ej: "SUPER_ADMIN")
                    consulta.setString(1, rol[0]);
                    // Descripcion del rol
                    consulta.setString(2, rol[1]);
                    // Ejecutar INSERT y acumular filas insertadas
                    filas += consulta.executeUpdate();
                }
                // Log de resultados
                System.out.println("  [Roles] Insertados: " + filas);
            // El PreparedStatement se cierra automáticamente
            }

        // Capturar errores generales
        } catch (Exception excepcion) {
            // Log de error
            System.err.println("Error SeedRoles: " + excepcion.getMessage());
        // La Connection se cierra automáticamente
        }
    }
}
