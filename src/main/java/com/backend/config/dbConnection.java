package com.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;

public class dbConnection {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWD = dotenv.get("DB_PASSWD");

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWD == null) {
            System.err.println("ERROR: Faltan variables de entorno (DB_URL, DB_USER, DB_PASSWD)");
            return null;
        }
        return DriverManager.getConnection(URL, USER, PASSWD);
    }
}