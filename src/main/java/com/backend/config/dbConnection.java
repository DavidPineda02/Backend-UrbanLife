package com.backend.config; // Paquete de configuración de la aplicación

// Libreria para cargar variables de entorno desde el archivo .env
import io.github.cdimascio.dotenv.Dotenv; // Manejo de variables de entorno
// Clases necesarias para la conexion JDBC con MySQL
import java.sql.*; // Todas las clases SQL para conexión y manejo de BD

/**
 * Clase de configuración para la conexión a la base de datos MySQL.
 * Utiliza variables de entorno para mantener seguros los credenciales.
 * Proporciona un método estático para obtener conexiones a la BD.
 */
public class dbConnection {

    // Cargar todas las variables del archivo .env al iniciar la clase
    private static final Dotenv dotenv = Dotenv.load(); // Instancia para leer .env
    // URL de conexion a la BD (ej: jdbc:mysql://localhost:3306/urbanlife)
    private static final String URL = dotenv.get("DB_URL"); // URL completa de conexión
    // Usuario de la base de datos
    private static final String USER = dotenv.get("DB_USER"); // Nombre de usuario de BD
    // Contrasena de la base de datos
    private static final String PASSWD = dotenv.get("DB_PASSWD"); // Contraseña de BD

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
            return null; // Retornar null si faltan credenciales
        }
        // Crear y retornar la conexion usando el driver JDBC de MySQL
        return DriverManager.getConnection(URL, USER, PASSWD); // Establecer conexión
    }
}