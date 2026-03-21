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
 * Seeder que inserta los permisos del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa los permisos básicos del sistema de forma segura.
 */
public class SeedPermisos {

    /** SQL para insertar un permiso con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Permisos (NOMBRE_PERMISO, DESCRIPCION_PERMISO) VALUES (?, ?)";

    /** Catálogo de permisos del sistema: [nombre, descripcion] */
    private static final String[][] permisos = {
            // Permisos de usuarios
            {"Gestionar Usuarios", "Crear, editar y eliminar usuarios del sistema"},
            // Permisos de roles
            {"Gestionar Roles", "Asignar y modificar roles de usuarios"},
            // Permisos de productos
            {"Gestionar Productos", "Crear, editar y eliminar productos"},
            // Permisos de categorías
            {"Gestionar Categorias", "Crear, editar y eliminar categorias"},
            // Permisos de ventas
            {"Gestionar Ventas", "Registrar y consultar ventas"},
            // Permisos de compras
            {"Gestionar Compras", "Registrar y consultar compras"},
            // Permisos de movimientos
            {"Gestionar Movimientos", "Registrar y consultar movimientos financieros"},
            // Permisos de gastos
            {"Gestionar Gastos", "Registrar y consultar gastos adicionales"},
            // Permisos de reportes
            {"Ver Reportes", "Consultar reportes del sistema"},
            // Permisos de perfil
            {"Gestionar Perfil", "Editar perfil de empresa y contacto"}
    };

    /**
     * Inserta los permisos iniciales solo si la tabla Permisos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertPermisos() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // Consultar cuantos permisos hay actualmente en la tabla
            String sqlVerificacion = "SELECT COUNT(*) FROM Permisos";
            // Preparar consulta y ejecutar verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Si ya hay al menos un permiso, omitir la insercion
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [Permisos] Ya existen datos -> omitido");
                    // Salir del método
                    return;
                }
            // El ResultSet y PreparedStatement se cierran automáticamente
            }

            // Preparar el statement de insercion y reutilizarlo para cada permiso
            // Preparar inserción
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                // Contador de filas insertadas
                int filas = 0;
                // Iterar sobre cada permiso definido en el arreglo
                // Recorrer permisos predefinidos
                for (String[] permiso : permisos) {
                    // Nombre del permiso (ej: "Gestionar Ventas")
                    consulta.setString(1, permiso[0]);
                    // Descripcion del permiso
                    consulta.setString(2, permiso[1]);
                    // Ejecutar INSERT y acumular filas insertadas
                    filas += consulta.executeUpdate();
                }
                // Log de resultados
                System.out.println("  [Permisos] Insertados: " + filas);
            // El PreparedStatement se cierra automáticamente
            }

        // Capturar errores generales
        } catch (Exception excepcion) {
            // Log de error
            System.err.println("Error SeedPermisos: " + excepcion.getMessage());
        // La Connection se cierra automáticamente
        }
    }
}
