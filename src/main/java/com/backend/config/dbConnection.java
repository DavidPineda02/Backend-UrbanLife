// Paquete de configuración de la aplicación que contiene clases para manejar conexiones y configuraciones globales
package com.backend.config;

// Importación de la librería dotenv que permite cargar variables de entorno desde un archivo .env
import io.github.cdimascio.dotenv.Dotenv;
// Importación de todas las clases del paquete java.sql necesarias para trabajar con bases de datos JDBC
import java.sql.*;

/**
 * Clase de configuración centralizada para establecer conexiones a la base de datos MySQL.
 * Implementa el patrón Singleton para gestionar las credenciales de forma segura mediante variables de entorno.
 * Proporciona un método estático que retorna nuevas conexiones JDBC para ser utilizadas en los DAOs.
 * 
 * @author UrbanLife Backend Team
 * @version 1.0
 * @since 2024
 */
public class dbConnection {

    /**
     * Instancia estática y final de Dotenv que carga las variables de entorno desde el archivo .env.
     * Se inicializa una sola vez al cargar la clase (patón eager initialization).
     * El archivo .env debe contener: DB_URL, DB_USER, DB_PASSWD, JWT_SECRET, GOOGLE_CLIENT_ID, EMAIL_USER, EMAIL_PASS
     */
    private static final Dotenv dotenv = Dotenv.load();
    
    /**
     * URL de conexión JDBC a la base de datos MySQL obtenida desde las variables de entorno.
     * Formato típico: "jdbc:mysql://localhost:3306/urbanlife?useSSL=false&serverTimezone=UTC"
     * Es final porque no cambia durante la ejecución del programa.
     */
    private static final String URL = dotenv.get("DB_URL");
    
    /**
     * Nombre de usuario de la base de datos obtenido desde variables de entorno.
     * Generalmente es "root" para desarrollo local o un usuario específico en producción.
     * Es final para mantener la inmutabilidad de las credenciales.
     */
    private static final String USER = dotenv.get("DB_USER");
    
    /**
     * Contraseña del usuario de la base de datos obtenida desde variables de entorno.
     * Se mantiene privada y final por seguridad, nunca se expone en logs o respuestas HTTP.
     */
    private static final String PASSWD = dotenv.get("DB_PASSWD");

    /**
     * Método estático que establece y retorna una nueva conexión a la base de datos MySQL.
     * Realiza validaciones previas de las credenciales antes de intentar la conexión.
     * Cada invocación crea una conexión nueva (no usa pool de conexiones).
     * 
     * @return Objeto Connection activo y listo para ejecutar consultas SQL, o null si faltan credenciales
     * @throws SQLException Si ocurre un error de conexión (BD caída, credenciales incorrectas, red, etc.)
     */
    public static Connection getConnection() throws SQLException {
        // Validación de seguridad: verifica que las tres variables de entorno esenciales estén definidas
        if (URL == null || USER == null || PASSWD == null) {
            // Envía mensaje de error a la salida estándar de errores (consola/terminal)
            System.err.println("ERROR: Faltan variables de entorno (DB_URL, DB_USER, DB_PASSWD)");
            // Retorna null en lugar de lanzar excepción para permitir manejo graceful en los DAOs
            return null;
        }
        // Establece la conexión JDBC usando el DriverManager de MySQL
        return DriverManager.getConnection(URL, USER, PASSWD);
    }
}
