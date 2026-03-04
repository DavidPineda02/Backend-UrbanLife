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
 */
public class SeedRoles {

    /** SQL para insertar un rol con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Roles (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    /** Datos de los 3 roles del sistema: [nombre, descripcion] */
    private static final String[][] roles = {
            {"SUPER_ADMIN", "Acceso total al sistema incluyendo configuracion tecnica"},
            {"ADMIN", "Gestion completa del negocio: ventas, inventario, compras y reportes"},
            {"EMPLEADO", "Acceso operativo limitado a funciones del dia a dia"}
    };

    /**
     * Inserta los roles iniciales solo si la tabla Roles está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertRoles() {
        try (Connection conexion = dbConnection.getConnection()) {

            // Verificar si ya existen roles para evitar insertar duplicados en cada inicio
            String sqlVerificacion = "SELECT COUNT(*) FROM Roles";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Si ya hay al menos un rol, omitir la insercion
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [Roles] Ya existen datos -> omitido");
                    return;
                }
            }

            // Preparar el statement de insercion una vez y reutilizarlo para cada rol
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                // Iterar sobre cada rol definido en el arreglo
                for (String[] rol : roles) {
                    consulta.setString(1, rol[0]); // Nombre del rol (ej: "SUPER_ADMIN")
                    consulta.setString(2, rol[1]); // Descripcion del rol
                    filas += consulta.executeUpdate(); // Ejecutar INSERT y acumular filas insertadas
                }
                System.out.println("  [Roles] Insertados: " + filas);
            }

        } catch (Exception excepcion) {
            System.err.println("Error SeedRoles: " + excepcion.getMessage());
        }
    }
}
