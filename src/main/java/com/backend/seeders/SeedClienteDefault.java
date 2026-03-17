// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;

// Interfaz para conexión JDBC
import java.sql.Connection;
// Clase para consultas preparadas
import java.sql.PreparedStatement;
// Interfaz para resultados de consultas
import java.sql.ResultSet;

/**
 * Seeder que crea el cliente predeterminado "Administracion" de UrbanLife.
 * Este cliente se usa para registrar ventas cuando no se quiere asociar un cliente específico.
 * Es idempotente: verifica por nombre antes de insertar para evitar duplicados.
 */
public class SeedClienteDefault {

    /**
     * Inserta el cliente predeterminado "Administracion" solo si no existe previamente.
     * Contiene los datos de la empresa UrbanLife como cliente interno del sistema.
     */
    public static void insertClienteDefault() {
        // Obtener conexión con auto-cierre
        try (Connection conexion = dbConnection.getConnection()) {

            // SQL para verificar si ya existe el cliente predeterminado
            String sqlVerificacion = "SELECT COUNT(*) FROM Clientes WHERE NOMBRE = ?";
            // Preparar consulta de verificación con auto-cierre
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion)) {
                // Asignar el nombre del cliente predeterminado como parámetro
                consultaVerificacion.setString(1, "Administracion");
                // Ejecutar consulta y obtener resultado
                ResultSet resultado = consultaVerificacion.executeQuery();
                // Si ya existe un cliente con ese nombre, omitir la inserción
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [ClienteDefault] Ya existe -> omitido");
                    // Salir del método sin insertar
                    return;
                }
            }

            // SQL para insertar el cliente predeterminado con todos sus campos
            String sqlCliente = "INSERT INTO Clientes (NOMBRE, DOCUMENTO, CORREO, TELEFONO, DIRECCION, CIUDAD, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?)";
            // Preparar consulta de inserción con auto-cierre
            try (PreparedStatement consulta = conexion.prepareStatement(sqlCliente)) {
                // Asignar el nombre del cliente predeterminado
                consulta.setString(1, "Administracion");
                // Asignar el documento de la empresa (NIT ficticio de UrbanLife)
                consulta.setLong(2, 9001234560L);
                // Asignar el correo de la empresa
                consulta.setString(3, "administracion@urbanlife.com");
                // Asignar el teléfono de la empresa
                consulta.setString(4, "3000000000");
                // Asignar la dirección de la empresa
                consulta.setString(5, "Sede Principal UrbanLife");
                // Asignar la ciudad de la empresa
                consulta.setString(6, "Colombia");
                // Asignar el estado activo del cliente
                consulta.setBoolean(7, true);
                // Ejecutar INSERT del cliente predeterminado
                consulta.executeUpdate();
            }

            // Log de confirmación del cliente creado
            System.out.println("  [ClienteDefault] Cliente creado: Administracion (UrbanLife)");

        // Capturar cualquier error durante el proceso
        } catch (Exception excepcion) {
            // Log de error con el mensaje de la excepción
            System.err.println("Error SeedClienteDefault: " + excepcion.getMessage());
        // La Connection se cierra automáticamente
        }
    }
}
