package com.backend;

import com.backend.seeders.SeedRoles;
import com.backend.seeders.SeedPermisos;
import com.backend.seeders.SeedTipoMovimientos;
import com.backend.seeders.SeedTipoGasto;
import com.backend.server.serverConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando UrbanLife Backend...");

        // Ejecutar seeders
        System.out.println("\nEjecutando seeders...");
        SeedRoles.insertRoles();
        SeedPermisos.insertPermisos();
        SeedTipoMovimientos.insertTipoMovimientos();
        SeedTipoGasto.insertTipoGasto();
        System.out.println("Seeders finalizados.\n");

        // Iniciar el servidor en puerto 8080
        serverConnection.startServer(8080);
    }
}