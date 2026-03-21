// Paquete de acceso a datos (Data Access Object) de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Para construir el objeto JSON de respuesta de validación
import com.google.gson.JsonObject;

// Para codificar el token a bytes UTF-8 antes de hacer el hash
import java.nio.charset.StandardCharsets;
// Para aplicar el algoritmo SHA-256 al token antes de guardarlo
import java.security.MessageDigest;
// Para manejar la conexión con la base de datos
import java.sql.Connection;
// Para ejecutar consultas SQL con parámetros seguros
import java.sql.PreparedStatement;
// Para obtener los resultados de las consultas SELECT
import java.sql.ResultSet;
// Para representar la fecha de expiración del token
import java.time.LocalDateTime;
// Para generar tokens UUID únicos y aleatorios
import java.util.UUID;

/**
 * DAO que centraliza todas las operaciones SQL de la tabla Token_Recuperacion.
 * Almacena tokens hasheados con SHA-256 para no exponer tokens en texto plano.
 * Provee métodos para guardar, validar y marcar como usados los tokens.
 */
public class TokenRecuperacionDAO {

    /**
     * Genera un token UUID, lo hashea con SHA-256 y lo guarda en la base de datos.
     * El token original se retorna para enviarlo al usuario por correo.
     * En la BD solo se almacena el hash (nunca el token en texto plano).
     * @param usuarioId ID del usuario que solicita la recuperación
     * @param fechaExpiracion Fecha y hora límite de validez del token (1 hora desde ahora)
     * @return Token UUID original (para enviarlo por correo), o null si falló la inserción
     */
    public static String guardarToken(int usuarioId, LocalDateTime fechaExpiracion) {
        // Generar un token UUID aleatorio y único que se enviará al usuario por correo
        String token = UUID.randomUUID().toString();
        // Calcular el hash SHA-256 del token para almacenarlo de forma segura en la BD
        String tokenHash = sha256(token);
        // Consulta SQL para insertar el hash del token y su fecha de expiración
        String sql = "INSERT INTO Tokens_Recuperacion (USUARIO_ID, TOKEN, FECHA_EXPIRACION) VALUES (?, ?, ?)";
        // Abrir conexión y preparar la consulta (se cierran automáticamente con try-with-resources)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del usuario al primer parámetro
            consulta.setInt(1, usuarioId);
            // Asignar el hash SHA-256 del token al segundo parámetro (nunca el token en texto plano)
            consulta.setString(2, tokenHash);
            // Asignar la fecha de expiración al tercer parámetro
            consulta.setObject(3, fechaExpiracion);
            // Ejecutar el INSERT para guardar el token en la base de datos
            consulta.executeUpdate();
            // Retornar el token original (no el hash) para enviarlo al usuario por correo
            return token;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error TokenRecuperacionDAO.guardarToken: " + excepcion.getMessage());
        }
        // Retornar null si hubo un error al guardar el token
        return null;
    }

    /**
     * Valida que el token exista en la BD, no haya sido usado y no haya expirado.
     * Hashea el token recibido y lo compara contra el hash almacenado en la BD.
     * @param token Token UUID recibido del usuario (en texto plano, desde el link del correo)
     * @return JsonObject con success, idToken, usuarioId si es válido; o error si no
     */
    public static JsonObject validarToken(String token) {
        // Crear el objeto de respuesta
        JsonObject respuesta = new JsonObject();
        // Consulta SQL para buscar el token por su hash SHA-256, solo si no fue usado
        String sql = "SELECT ID_TOKEN, USUARIO_ID, FECHA_EXPIRACION FROM Tokens_Recuperacion " +
                    "WHERE TOKEN = ? AND USADO = FALSE";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el hash SHA-256 del token recibido como parámetro de búsqueda
            consulta.setString(1, sha256(token));
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();

            // Verificar si se encontró un token válido (no usado) en la BD
            if (resultado.next()) {
                // Leer la fecha de expiración del token encontrado
                LocalDateTime fechaExpiracion = resultado.getObject("FECHA_EXPIRACION", LocalDateTime.class);

                // Verificar si el token ya expiró comparando con la fecha actual
                if (LocalDateTime.now().isAfter(fechaExpiracion)) {
                    // Indicar que la operación falló por token expirado
                    respuesta.addProperty("success", false);
                    // Mensaje descriptivo del error
                    respuesta.addProperty("message", "El token ha expirado");
                    // Código HTTP 400 Bad Request
                    respuesta.addProperty("status", 400);
                } else {
                    // El token es válido y no ha expirado: retornar los IDs necesarios
                    respuesta.addProperty("success", true);
                    // ID del token para marcarlo como usado después del cambio de contraseña
                    respuesta.addProperty("idToken", resultado.getInt("ID_TOKEN"));
                    // ID del usuario para actualizar su contraseña
                    respuesta.addProperty("usuarioId", resultado.getInt("USUARIO_ID"));
                    // Código HTTP 200 OK
                    respuesta.addProperty("status", 200);
                }
            } else {
                // No se encontró el token: es inválido o ya fue usado
                respuesta.addProperty("success", false);
                // Mensaje descriptivo del error
                respuesta.addProperty("message", "Token invalido o ya utilizado");
                // Código HTTP 400 Bad Request
                respuesta.addProperty("status", 400);
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error TokenRecuperacionDAO.validarToken: " + excepcion.getMessage());
            // Indicar que hubo un error interno del servidor
            respuesta.addProperty("success", false);
            // Mensaje genérico de error de servidor
            respuesta.addProperty("message", "Error del servidor");
            // Código HTTP 500 Internal Server Error
            respuesta.addProperty("status", 500);
        }
        // Retornar la respuesta con el resultado de la validación
        return respuesta;
    }

    /**
     * Marca un token como usado para que no pueda reutilizarse.
     * Se llama tras cambiar exitosamente la contraseña del usuario.
     * @param idToken ID del token a marcar como usado
     * @return true si la actualización fue exitosa, false si falló
     */
    public static boolean marcarTokenUsado(int idToken) {
        // Consulta SQL para marcar el token como usado por su ID
        String sql = "UPDATE Tokens_Recuperacion SET USADO = TRUE WHERE ID_TOKEN = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del token al parámetro de la consulta
            consulta.setInt(1, idToken);
            // Ejecutar el UPDATE y retornar true si afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error TokenRecuperacionDAO.marcarTokenUsado: " + excepcion.getMessage());
        }
        // Retornar false si el UPDATE no afectó ninguna fila o hubo un error
        return false;
    }

    /**
     * Genera el hash SHA-256 de un String y lo retorna en formato hexadecimal.
     * Se usa para nunca almacenar tokens de recuperación en texto plano en la BD.
     * @param input Texto a hashear (el token UUID en texto plano)
     * @return Hash SHA-256 en formato hexadecimal de 64 caracteres
     */
    private static String sha256(String input) {
        // Bloque try para capturar el error si el algoritmo SHA-256 no está disponible
        try {
            // Obtener la instancia del algoritmo de hash SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Aplicar el hash al texto convertido a bytes con codificación UTF-8
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // StringBuilder para construir la representación hexadecimal del hash
            StringBuilder hex = new StringBuilder();
            // Iterar cada byte del hash y convertirlo a dos caracteres hexadecimales
            for (byte b : hash) hex.append(String.format("%02x", b));
            // Retornar la representación hexadecimal completa del hash SHA-256
            return hex.toString();
        } catch (Exception e) {
            // Si SHA-256 no está disponible, lanzar excepción de tiempo de ejecución
            throw new RuntimeException("Error al hashear el token", e);
        }
    }
}
