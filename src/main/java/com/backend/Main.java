package com.backend;

import com.backend.server.serverConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando UrbanLife Backend...");

        // Iniciar el servidor en puerto 8080
        serverConnection.startServer(8080);
    }
}