package com.backend.config;
import java.sql.*;

public class dbConnection {
    public static Connection getConnection() {
        try {
            String URL = "jdbc:mysql://localhost:3306/urbanlife";
            String USER = "root";
            String PASS = "#Aprendiz2024";
            Connection connection = DriverManager.getConnection(URL, USER, PASS);
            return connection;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}           