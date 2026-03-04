package com.backend.config;

// Libreria para cargar variables de entorno desde el archivo .env
import io.github.cdimascio.dotenv.Dotenv;
// Clases necesarias para la conexion JDBC con MySQL
import java.sql.*;

public class dbConnection {

    // Cargar todas las variables del archivo .env al iniciar la clase
    private static final Dotenv dotenv = Dotenv.load();
    // URL de conexion a la BD (ej: jdbc:mysql://localhost:3306/urbanlife)
    private static final String URL = dotenv.get("DB_URL");
    // Usuario de la base de datos
    private static final String USER = dotenv.get("DB_USER");
    // Contrasena de la base de datos
    private static final String PASSWD = dotenv.get("DB_PASSWD");

    // Retorna una nueva conexion a la BD cada vez que se invoca
    public static Connection getConnection() throws SQLException {
        // Verificar que las tres variables de entorno esten definidas
        if (URL == null || USER == null || PASSWD == null) {
            // Mostrar error descriptivo si falta alguna variable y no conectar
            System.err.println("ERROR: Faltan variables de entorno (DB_URL, DB_USER, DB_PASSWD)");
            return null;
        }
        // Crear y retornar la conexion usando el driver JDBC de MySQL
        return DriverManager.getConnection(URL, USER, PASSWD);
    }
}