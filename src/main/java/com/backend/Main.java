package com.backend;
import com.backend.config.dbConnection;

public class Main {
    public static void main(String[] args) {
        dbConnection.getConnection();
        System.out.println("Conexion exitosa");
    }
}