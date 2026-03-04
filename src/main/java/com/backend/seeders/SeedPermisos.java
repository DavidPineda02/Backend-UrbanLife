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
 */
public class SeedPermisos {

    /** SQL para insertar un permiso con su nombre y descripcion */
    private static final String SQL_INSERT = "INSERT INTO Permisos (NOMBRE, DESCRIPCION) VALUES (?, ?)";

    /** Catálogo de permisos del sistema: [nombre, descripcion] */
    private static final String[][] permisos = {
            {"Gestionar Usuarios", "Crear, editar y eliminar usuarios del sistema"},
            {"Gestionar Roles", "Asignar y modificar roles de usuarios"},
            {"Gestionar Productos", "Crear, editar y eliminar productos"},
            {"Gestionar Categorias", "Crear, editar y eliminar categorias"},
            {"Gestionar Ventas", "Registrar y consultar ventas"},
            {"Gestionar Compras", "Registrar y consultar compras"},
            {"Gestionar Movimientos", "Registrar y consultar movimientos financieros"},
            {"Gestionar Gastos", "Registrar y consultar gastos adicionales"},
            {"Ver Reportes", "Consultar reportes del sistema"},
            {"Gestionar Perfil", "Editar perfil de empresa y contacto"}
    };

    /**
     * Inserta los permisos iniciales solo si la tabla Permisos está vacía (idempotente).
     * Verifica primero si existen datos antes de insertar para evitar duplicados.
     */
    public static void insertPermisos() {
        try (Connection conexion = dbConnection.getConnection()) {

            // Consultar cuantos permisos hay actualmente en la tabla
            String sqlVerificacion = "SELECT COUNT(*) FROM Permisos";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Si ya hay al menos un permiso, omitir la insercion
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [Permisos] Ya existen datos -> omitido");
                    return;
                }
            }

            // Preparar el statement de insercion y reutilizarlo para cada permiso
            try (PreparedStatement consulta = conexion.prepareStatement(SQL_INSERT)) {
                int filas = 0;
                // Iterar sobre cada permiso definido en el arreglo
                for (String[] permiso : permisos) {
                    consulta.setString(1, permiso[0]); // Nombre del permiso (ej: "Gestionar Ventas")
                    consulta.setString(2, permiso[1]); // Descripcion del permiso
                    filas += consulta.executeUpdate(); // Ejecutar INSERT y acumular filas insertadas
                }
                System.out.println("  [Permisos] Insertados: " + filas);
            }

        } catch (Exception excepcion) {
            System.err.println("Error SeedPermisos: " + excepcion.getMessage());
        }
    }
}
