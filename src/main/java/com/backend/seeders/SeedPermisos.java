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
 * Seeder que inserta los permisos del sistema si la tabla está vacía.
 * Es idempotente: solo inserta datos si no existen previamente.
 * Inicializa los permisos básicos del sistema de forma segura.
 */
public class SeedPermisos {

    /** SQL para insertar un permiso con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Permisos (NOMBRE, DESCRIPCION) VALUES (?, ?)"; // Query de inserción

    /** Catálogo de permisos del sistema: [nombre, descripcion] */
    private static final String[][] permisos = { // Arreglo de permisos predefinidos
            {"Gestionar Usuarios", "Crear, editar y eliminar usuarios del sistema"}, // Permisos de usuarios
            {"Gestionar Roles", "Asignar y modificar roles de usuarios"}, // Permisos de roles
            {"Gestionar Productos", "Crear, editar y eliminar productos"}, // Permisos de productos
            {"Gestionar Categorias", "Crear, editar y eliminar categorias"}, // Permisos de categorías
            {"Gestionar Ventas", "Registrar y consultar ventas"}, // Permisos de ventas
            {"Gestionar Compras", "Registrar y consultar compras"}, // Permisos de compras
            {"Gestionar Movimientos", "Registrar y consultar movimientos financieros"}, // Permisos de movimientos
            {"Gestionar Gastos", "Registrar y consultar gastos adicionales"}, // Permisos de gastos
            {"Ver Reportes", "Consultar reportes del sistema"}, // Permisos de reportes
            {"Gestionar Perfil", "Editar perfil de empresa y contacto"} // Permisos de perfil
    };

    /**
     * Inserta los permisos iniciales solo si la tabla Permisos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertPermisos() { // Método principal de inserción
        try (Connection conexion = dbConnection.getConnection()) { // Obtener conexión con auto-cierre

            // Consultar cuantos permisos hay actualmente en la tabla
            String sqlVerificacion = "SELECT COUNT(*) FROM Permisos"; // Query de verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion); // Preparar consulta
                ResultSet resultado = consultaVerificacion.executeQuery()) { // Ejecutar consulta
                // Si ya hay al menos un permiso, omitir la insercion
                if (resultado.next() && resultado.getInt(1) > 0) { // Verificar si existen datos
                    System.out.println("  [Permisos] Ya existen datos -> omitido"); // Log de omisión
                    return; // Salir del método
                }
            } // El ResultSet y PreparedStatement se cierran automáticamente

            // Preparar el statement de insercion y reutilizarlo para cada permiso
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) { // Preparar inserción
                int filas = 0; // Contador de filas insertadas
                // Iterar sobre cada permiso definido en el arreglo
                for (String[] permiso : permisos) { // Recorrer permisos predefinidos
                    consulta.setString(1, permiso[0]); // Nombre del permiso (ej: "Gestionar Ventas")
                    consulta.setString(2, permiso[1]); // Descripcion del permiso
                    filas += consulta.executeUpdate(); // Ejecutar INSERT y acumular filas insertadas
                }
                System.out.println("  [Permisos] Insertados: " + filas); // Log de resultados
            } // El PreparedStatement se cierra automáticamente

        } catch (Exception excepcion) { // Capturar errores generales
            System.err.println("Error SeedPermisos: " + excepcion.getMessage()); // Log de error
        } // La Connection se cierra automáticamente
    }
}
