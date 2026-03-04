package com.backend;

// Importar los seeders que insertan datos iniciales en la base de datos
import com.backend.seeders.SeedRoles;
import com.backend.seeders.SeedPermisos;
import com.backend.seeders.SeedTipoMovimientos;
import com.backend.seeders.SeedTipoGasto;
// Importar la clase que inicia el servidor HTTP
import com.backend.server.serverConnection;

public class Main {
    public static void main(String[] args) {
        // Mensaje de inicio para confirmar que la aplicacion arranco
        System.out.println("Iniciando UrbanLife Backend...");

        // Ejecutar seeders al arrancar para garantizar datos base en la BD
        System.out.println("\nEjecutando seeders...");
        SeedRoles.insertRoles();              // Insertar roles del sistema si no existen
        SeedPermisos.insertPermisos();        // Insertar permisos del sistema si no existen
        SeedTipoMovimientos.insertTipoMovimientos(); // Insertar tipos de movimiento si no existen
        SeedTipoGasto.insertTipoGasto();      // Insertar tipos de gasto si no existen
        System.out.println("Seeders finalizados.\n");

        // Iniciar el servidor HTTP escuchando en el puerto 8080
        serverConnection.startServer(8080);
    }
}
