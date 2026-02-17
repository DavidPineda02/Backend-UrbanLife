package com.backend.config;
import java.sql.*;

public class dbConnection {
    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/urbanlife", "root", "Admin@2424.");
            return connection;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}