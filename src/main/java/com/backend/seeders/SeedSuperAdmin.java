package com.backend.seeders;

import com.backend.config.dbConnection;
import com.backend.helpers.PasswordHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedSuperAdmin {

    public static void insertSuperAdmin() {
        try (Connection conexion = dbConnection.getConnection()) {

            // Verificar si ya existe un usuario con ese correo
            String sqlVerificacion = "SELECT COUNT(*) FROM Usuarios WHERE CORREO = ?";
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion)) {
                consultaVerificacion.setString(1, "davxpa.02@gmail.com");
                ResultSet resultado = consultaVerificacion.executeQuery();
                if (resultado.next() && resultado.getInt(1) > 0) {
                    System.out.println("  [SuperAdmin] Ya existe -> omitido");
                    return;
                }
            }

            // Insertar el usuario SUPER_ADMIN
            String sqlUsuario = "INSERT INTO Usuarios (NOMBRE, APELLIDO, CORREO, CONTRASENA, ESTADO) VALUES (?, ?, ?, ?, ?)";
            int idUsuario;
            try (PreparedStatement consulta = conexion.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                consulta.setString(1, "David");
                consulta.setString(2, "Pineda");
                consulta.setString(3, "davxpa.02@gmail.com");
                consulta.setString(4, PasswordHelper.hashPassword("David@123."));
                consulta.setBoolean(5, true);
                consulta.executeUpdate();

                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                if (!clavesGeneradas.next()) {
                    System.err.println("Error SeedSuperAdmin: no se obtuvo el ID del usuario creado");
                    return;
                }
                idUsuario = clavesGeneradas.getInt(1);
            }

            // Obtener el ID del rol SUPER_ADMIN
            String sqlRol = "SELECT ID_ROLES FROM Roles WHERE NOMBRE = 'SUPER_ADMIN'";
            int idRol;
            try (PreparedStatement consulta = conexion.prepareStatement(sqlRol)) {
                ResultSet resultado = consulta.executeQuery();
                if (!resultado.next()) {
                    System.err.println("Error SeedSuperAdmin: rol SUPER_ADMIN no encontrado. Ejecuta SeedRoles primero.");
                    return;
                }
                idRol = resultado.getInt("ID_ROLES");
            }

            // Asignar el rol SUPER_ADMIN al usuario
            String sqlUsuarioRol = "INSERT INTO Usuario_Rol (USUARIO_ID, ROL_ID) VALUES (?, ?)";
            try (PreparedStatement consulta = conexion.prepareStatement(sqlUsuarioRol)) {
                consulta.setInt(1, idUsuario);
                consulta.setInt(2, idRol);
                consulta.executeUpdate();
            }

            System.out.println("  [SuperAdmin] Usuario creado: davxpa.02@gmail.com (SUPER_ADMIN)");

        } catch (Exception excepcion) {
            System.err.println("Error SeedSuperAdmin: " + excepcion.getMessage());
        }
    }
}
