// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Helper para hashear contraseñas con BCrypt
import com.backend.helpers.PasswordHelper;

// Interfaz para conexión JDBC
import java.sql.Connection;
// Clase para consultas preparadas
import java.sql.PreparedStatement;
// Interfaz para resultados de consultas
import java.sql.ResultSet;

/**
 * Seeder que crea el usuario SUPER_ADMIN del sistema si no existe previamente.
 * Es idempotente: verifica por correo en Correos_Usuarios antes de insertar para evitar duplicados.
 * Depende de SeedRoles: el rol SUPER_ADMIN debe existir antes de ejecutar este seeder.
 */
public class SeedSuperAdmin {

    /**
     * Inserta el usuario SUPER_ADMIN solo si no existe un usuario con el correo configurado.
     * Crea el usuario en Usuarios, inserta el correo principal en Correos_Usuarios,
     * obtiene el ID del rol SUPER_ADMIN y asigna la relación en la tabla Usuarios_Roles.
     */
    public static void insertSuperAdmin() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // SQL para verificar si ya existe el correo del super admin en Correos_Usuarios
            String sqlVerificacion = "SELECT COUNT(*) FROM Correos_Usuarios WHERE CORREO_USUARIO = ?";
            // Preparar consulta de verificación con auto-cierre
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion)) {
                // Asignar el correo del super admin como parámetro de búsqueda
                consultaVerificacion.setString(1, "davxpa.02@gmail.com");
                // Ejecutar consulta y obtener resultado
                ResultSet resultado = consultaVerificacion.executeQuery();
                // Si ya existe un correo con esa dirección, omitir la inserción
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [SuperAdmin] Ya existe -> omitido");
                    // Salir del método sin insertar
                    return;
                }
            }

            // SQL para insertar el nuevo usuario SUPER_ADMIN en la tabla Usuarios (sin correo)
            String sqlUsuario = "INSERT INTO Usuarios (NOMBRE_USUARIO, APELLIDO_USUARIO, CONTRASENA, ESTADO_USUARIO) VALUES (?, ?, ?, ?)";
            // Variable para almacenar el ID del usuario creado
            int idUsuario;
            // Preparar consulta solicitando las claves generadas automáticamente
            try (PreparedStatement consulta = conexion.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                // Asignar el nombre del super admin
                consulta.setString(1, "David");
                // Asignar el apellido del super admin
                consulta.setString(2, "Pineda");
                // Asignar la contraseña hasheada con BCrypt
                consulta.setString(3, PasswordHelper.hashPassword("David@123."));
                // Asignar el estado activo del usuario
                consulta.setBoolean(4, true);
                // Ejecutar INSERT del usuario
                consulta.executeUpdate();

                // Obtener las claves generadas por la BD (ID auto-incrementado)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Verificar que se obtuvo el ID generado
                if (!clavesGeneradas.next()) {
                    // Log de error si no se pudo obtener el ID
                    System.err.println("Error SeedSuperAdmin: no se obtuvo el ID del usuario creado");
                    // Salir del método sin continuar
                    return;
                }
                // Leer el ID generado para usarlo en la asignación de rol y correo
                idUsuario = clavesGeneradas.getInt(1);
            }

            // SQL para insertar el correo principal del super admin en Correos_Usuarios
            String sqlCorreo = "INSERT INTO Correos_Usuarios (CORREO_USUARIO, ES_PRINCIPAL, USUARIO_ID) VALUES (?, TRUE, ?)";
            // Preparar consulta de inserción del correo con auto-cierre
            try (PreparedStatement consulta = conexion.prepareStatement(sqlCorreo)) {
                // Asignar el correo del super admin
                consulta.setString(1, "davxpa.02@gmail.com");
                // Asignar el ID del usuario recién creado
                consulta.setInt(2, idUsuario);
                // Ejecutar INSERT del correo principal
                consulta.executeUpdate();
            }

            // SQL para obtener el ID del rol SUPER_ADMIN desde la tabla Roles
            String sqlRol = "SELECT ID_ROLES FROM Roles WHERE NOMBRE_ROL = 'SUPER_ADMIN'";
            // Variable para almacenar el ID del rol SUPER_ADMIN
            int idRol;
            // Preparar y ejecutar consulta de búsqueda del rol con auto-cierre
            try (PreparedStatement consulta = conexion.prepareStatement(sqlRol)) {
                // Ejecutar consulta y obtener resultado
                ResultSet resultado = consulta.executeQuery();
                // Verificar que el rol SUPER_ADMIN existe en la BD
                if (!resultado.next()) {
                    // Log de error indicando la dependencia con SeedRoles
                    System.err.println("Error SeedSuperAdmin: rol SUPER_ADMIN no encontrado. Ejecuta SeedRoles primero.");
                    // Salir del método sin continuar
                    return;
                }
                // Leer el ID del rol SUPER_ADMIN para asignarlo al usuario
                idRol = resultado.getInt("ID_ROLES");
            }

            // SQL para insertar la relación usuario-rol en la tabla Usuarios_Roles
            String sqlUsuarioRol = "INSERT INTO Usuarios_Roles (USUARIO_ID, ROL_ID) VALUES (?, ?)";
            // Preparar consulta de asignación de rol con auto-cierre
            try (PreparedStatement consulta = conexion.prepareStatement(sqlUsuarioRol)) {
                // Asignar el ID del usuario creado
                consulta.setInt(1, idUsuario);
                // Asignar el ID del rol SUPER_ADMIN
                consulta.setInt(2, idRol);
                // Ejecutar INSERT de la relación usuario-rol
                consulta.executeUpdate();
            }

            // Log de confirmación del super admin creado
            System.out.println("  [SuperAdmin] Usuario creado: davxpa.02@gmail.com (SUPER_ADMIN)");

        // Capturar cualquier error durante el proceso
        } catch (Exception excepcion) {
            // Log de error con el mensaje de la excepción
            System.err.println("Error SeedSuperAdmin: " + excepcion.getMessage());
        // La Connection se cierra automáticamente
        }
    }
}
