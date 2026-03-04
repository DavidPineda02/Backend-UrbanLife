// Paquete principal de la aplicación backend
package com.backend;

// Importar los seeders que insertan datos iniciales en la base de datos
import com.backend.seeders.SeedRoles;
// Seeder para insertar permisos del sistema
import com.backend.seeders.SeedPermisos;
// Seeder para insertar tipos de movimientos financieros
import com.backend.seeders.SeedTipoMovimientos;
// Seeder para insertar tipos de gastos adicionales
import com.backend.seeders.SeedTipoGasto;
// Importar la clase que inicia el servidor HTTP
import com.backend.server.serverConnection;

/**
 * Clase principal que inicia la aplicación UrbanLife Backend.
 * Ejecuta los seeders para inicializar datos base y arranca el servidor HTTP.
 * Es el punto de entrada de toda la aplicación.
 */
public class Main {

    /**
     * Método principal que inicia la aplicación.
     * Ejecuta los seeders para poblar la base de datos con datos iniciales
     * y luego inicia el servidor HTTP en el puerto 8080.
     * @param args Argumentos de línea de comandos (no utilizados en esta aplicación)
     */
    public static void main(String[] args) {
        // Mensaje de inicio para confirmar que la aplicacion arranco correctamente
        System.out.println("Iniciando UrbanLife Backend...");

        // Ejecutar seeders al arrancar para garantizar datos base en la BD
        System.out.println("\nEjecutando seeders...");
        // Insertar roles del sistema si no existen
        SeedRoles.insertRoles();
        // Insertar permisos del sistema si no existen
        SeedPermisos.insertPermisos();
        // Insertar tipos de movimiento si no existen
        SeedTipoMovimientos.insertTipoMovimientos();
        // Insertar tipos de gasto si no existen
        SeedTipoGasto.insertTipoGasto();
        System.out.println("Seeders finalizados.\n");

        // Iniciar el servidor HTTP escuchando en el puerto 8080
        serverConnection.startServer(8080);
    }
}
