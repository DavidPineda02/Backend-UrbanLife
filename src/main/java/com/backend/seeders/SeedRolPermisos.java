// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Para obtener la conexión a la base de datos
import com.backend.config.dbConnection;

// Para la conexión JDBC
import java.sql.Connection;
// Para las consultas parametrizadas
import java.sql.PreparedStatement;
// Para leer el resultado de la consulta de verificación
import java.sql.ResultSet;

/**
 * Seeder que asigna los permisos a cada rol del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Depende de SeedRoles y SeedPermisos: ambos deben ejecutarse antes.
 * Permisos: 1=Gestionar Usuarios, 2=Gestionar Roles, 3=Gestionar Productos,
 * 4=Gestionar Categorías, 5=Gestionar Ventas, 6=Gestionar Compras,
 * 7=Gestionar Movimientos, 8=Gestionar Gastos, 9=Ver Reportes, 10=Gestionar Perfil.
 */
public class SeedRolPermisos {

    /** SQL para insertar una relación rol-permiso */
    private static final String SQL_INSERT = "INSERT INTO Roles_Permisos (ROL_ID, PERMISOS_ID) VALUES (?, ?)";

    /** Asignaciones de permisos por rol: [rolId, permisoId] */
    private static final int[][] asignaciones = {
            // SUPER_ADMIN (rol 1) → todos los permisos
            {1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {1, 7}, {1, 8}, {1, 9}, {1, 10},
            // ADMIN (rol 2) → todo excepto Gestionar Usuarios y Gestionar Roles
            {2, 3}, {2, 4}, {2, 5}, {2, 6}, {2, 7}, {2, 8}, {2, 9}, {2, 10},
            // EMPLEADO (rol 3) → solo operaciones del día a día
            {3, 3}, {3, 5}, {3, 7}, {3, 10}
    };

    /**
     * Inserta las asignaciones rol-permiso solo si la tabla Roles_Permisos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertRolPermisos() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // Verificar si ya existen asignaciones para evitar duplicados
            String sqlVerificacion = "SELECT COUNT(*) FROM Roles_Permisos";
            // Preparar consulta y ejecutar verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Si ya hay al menos una asignación, omitir la inserción
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [Roles_Permisos] Ya existen datos -> omitido");
                    // Salir del método
                    return;
                }
            }

            // Preparar el statement de inserción y reutilizarlo para cada asignación
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                // Contador de filas insertadas
                int filas = 0;
                // Recorrer cada asignación rol-permiso definida en el arreglo
                for (int[] asignacion : asignaciones) {
                    // ID del rol (1=SUPER_ADMIN, 2=ADMIN, 3=EMPLEADO)
                    consulta.setInt(1, asignacion[0]);
                    // ID del permiso asignado a ese rol
                    consulta.setInt(2, asignacion[1]);
                    // Ejecutar INSERT y acumular filas insertadas
                    filas += consulta.executeUpdate();
                }
                // Log de resultados
                System.out.println("  [Roles_Permisos] Insertados: " + filas);
            }

        // Capturar errores generales
        } catch (Exception excepcion) {
            // Log de error
            System.err.println("Error SeedRolPermisos: " + excepcion.getMessage());
        }
    }
}
