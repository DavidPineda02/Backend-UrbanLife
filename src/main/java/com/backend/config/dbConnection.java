// Paquete de configuración de la aplicación
package com.backend.config;

// Libreria para cargar variables de entorno desde el archivo .env
import io.github.cdimascio.dotenv.Dotenv;
// Clases necesarias para la conexion JDBC con MySQL
import java.sql.*;

/**
 * Clase de configuración para la conexión a la base de datos MySQL.
 * Utiliza variables de entorno para mantener seguros los credenciales.
 * Proporciona un método estático para obtener conexiones a la BD.
 */
public class dbConnection {

    // Cargar todas las variables del archivo .env al iniciar la clase
    private static final Dotenv dotenv = Dotenv.load();
    // URL de conexion a la BD (ej: jdbc:mysql://localhost:3306/urbanlife)
    private static final String URL = dotenv.get("DB_URL");
    // Usuario de la base de datos
    private static final String USER = dotenv.get("DB_USER");
    // Contrasena de la base de datos
    private static final String PASSWD = dotenv.get("DB_PASSWD");

    /**
     * Retorna una nueva conexión a la BD cada vez que se invoca.
     * Verifica que las variables de entorno estén configuradas antes de conectar.
     * @return Connection objeto de conexión a la base de datos MySQL
     * @throws SQLException Si ocurre un error al establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        // Verificar que las tres variables de entorno esten definidas
        if (URL == null || USER == null || PASSWD == null) {
            // Mostrar error descriptivo si falta alguna variable y no conectar
            System.err.println("ERROR: Faltan variables de entorno (DB_URL, DB_USER, DB_PASSWD)");
            // Retornar null si faltan credenciales
            return null;
        }
        // Crear y retornar la conexion usando el driver JDBC de MySQL
        return DriverManager.getConnection(URL, USER, PASSWD);
    }
}
