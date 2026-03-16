// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Para obtener la conexión a la base de datos
import com.backend.config.dbConnection;

// Para la conexión JDBC
import java.sql.Connection;
// Para las consultas parametrizadas
import java.sql.PreparedStatement;
// Para leer el resultado de la consulta de verificación
import java.sql.ResultSet;

/**
 * Seeder que inserta datos de demostración para probar el sistema.
 * Es idempotente: solo inserta datos si la tabla Categoria está vacía.
 * Inserta en orden de dependencia: usuarios, catálogo, transacciones.
 * Contraseña de todos los usuarios de prueba: "Admin123"
 * (hash BCrypt con cost 10).
 */
public class SeedDemoData {

    /** Hash BCrypt de "Admin123" para todos los usuarios demo */
    private static final String HASH_PASSWORD = "$2a$10$LpOVs3G0xHFn8zOQXE3p5u9JKbT0W6D4s1mR3YgF5vN7cXq2kB8Wy";

    /**
     * Inserta todos los datos de demostración solo si no existen previamente.
     * Usa la tabla Categoria como indicador: si ya tiene datos, omite todo.
     */
    public static void insertDemoData() {
        // Declarar la conexión fuera del try para manejar la transacción
        Connection conexion = null;
        try {
            // Obtener conexión a la base de datos
            conexion = dbConnection.getConnection();

            // Verificar si ya existen datos demo usando Categoria como indicador
            String sqlVerificacion = "SELECT COUNT(*) FROM Categoria";
            // Preparar consulta y ejecutar verificación
            try (PreparedStatement consultaVerificacion = conexion.prepareStatement(sqlVerificacion);
                 ResultSet resultado = consultaVerificacion.executeQuery()) {
                // Si ya hay datos demo, omitir toda la inserción
                if (resultado.next() && resultado.getInt(1) > 0) {
                    // Log de omisión
                    System.out.println("  [DemoData] Ya existen datos -> omitido");
                    // Salir del método
                    return;
                }
            }

            // Desactivar auto-commit para manejar toda la inserción como una transacción
            conexion.setAutoCommit(false);

            // ===== SECCIÓN 1: USUARIOS DE PRUEBA =====
            insertUsuarios(conexion);

            // ===== SECCIÓN 2: ASIGNACIÓN DE ROLES A USUARIOS =====
            insertUsuarioRol(conexion);

            // ===== SECCIÓN 3: CORREOS ADICIONALES DE USUARIOS =====
            insertCorreosUsuario(conexion);

            // ===== SECCIÓN 4: TELÉFONOS DE USUARIOS =====
            insertNumerosUsuario(conexion);

            // ===== SECCIÓN 5: CATEGORÍAS DE PRODUCTOS =====
            insertCategorias(conexion);

            // ===== SECCIÓN 6: CLIENTES =====
            insertClientes(conexion);

            // ===== SECCIÓN 7: PROVEEDORES =====
            insertProveedores(conexion);

            // ===== SECCIÓN 8: PRODUCTOS =====
            insertProductos(conexion);

            // ===== SECCIÓN 9: COMPRAS Y DETALLES =====
            insertCompras(conexion);
            insertDetalleCompras(conexion);

            // ===== SECCIÓN 10: VENTAS Y DETALLES =====
            insertVentas(conexion);
            insertDetalleVentas(conexion);

            // ===== SECCIÓN 11: GASTOS ADICIONALES =====
            insertGastosAdicionales(conexion);

            // ===== SECCIÓN 12: MOVIMIENTOS FINANCIEROS =====
            insertMovimientosFinancieros(conexion);

            // Confirmar todos los cambios de la transacción
            conexion.commit();
            // Log de confirmación
            System.out.println("  [DemoData] Datos de demostración insertados exitosamente");

        } catch (Exception excepcion) {
            // Log de error
            System.err.println("Error SeedDemoData: " + excepcion.getMessage());
            // Intentar revertir la transacción si hubo error
            if (conexion != null) {
                try {
                    // Hacer rollback para no dejar datos parciales
                    conexion.rollback();
                } catch (Exception errorRollback) {
                    // Log de error del rollback
                    System.err.println("Error en rollback SeedDemoData: " + errorRollback.getMessage());
                }
            }
        } finally {
            // Restaurar auto-commit y cerrar conexión
            if (conexion != null) {
                try {
                    // Restaurar auto-commit
                    conexion.setAutoCommit(true);
                    // Cerrar la conexión
                    conexion.close();
                } catch (Exception errorCierre) {
                    // Log de error de cierre
                    System.err.println("Error al cerrar conexión SeedDemoData: " + errorCierre.getMessage());
                }
            }
        }
    }

    // ========================================================================
    // SECCIÓN 1: USUARIOS DE PRUEBA
    // ========================================================================

    /**
     * Inserta 5 usuarios de prueba en la tabla Usuarios.
     * Carlos y Laura son ADMIN, Andrés, Valentina y Santiago son EMPLEADO.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertUsuarios(Connection conexion) throws Exception {
        // SQL para insertar un usuario con nombre, apellido, correo, contraseña y estado
        String sql = "INSERT INTO Usuarios (NOMBRE, APELLIDO, CORREO, CONTRASENA, ESTADO) VALUES (?, ?, ?, ?, ?)";
        // Datos de los usuarios: [nombre, apellido, correo]
        String[][] usuarios = {
                {"Carlos",    "Martínez",  "carlos.martinez@urbanlife.com"},
                {"Laura",     "González",  "laura.gonzalez@urbanlife.com"},
                {"Andrés",    "Rodríguez", "andres.rodriguez@urbanlife.com"},
                {"Valentina", "López",     "valentina.lopez@urbanlife.com"},
                {"Santiago",  "Herrera",   "santiago.herrera@urbanlife.com"}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada usuario definido en el arreglo
            for (String[] usuario : usuarios) {
                // Asignar el nombre del usuario
                consulta.setString(1, usuario[0]);
                // Asignar el apellido del usuario
                consulta.setString(2, usuario[1]);
                // Asignar el correo del usuario
                consulta.setString(3, usuario[2]);
                // Asignar la contraseña hasheada (Admin123)
                consulta.setString(4, HASH_PASSWORD);
                // Asignar el estado activo
                consulta.setBoolean(5, true);
                // Ejecutar INSERT del usuario
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 2: ASIGNACIÓN DE ROLES A USUARIOS
    // ========================================================================

    /**
     * Asigna roles a los usuarios de prueba.
     * Carlos(2) y Laura(3) = ADMIN(rol 2), Andrés(4), Valentina(5) y Santiago(6) = EMPLEADO(rol 3).
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertUsuarioRol(Connection conexion) throws Exception {
        // SQL para insertar una relación usuario-rol
        String sql = "INSERT INTO Usuario_Rol (USUARIO_ID, ROL_ID) VALUES (?, ?)";
        // Asignaciones: [usuarioId, rolId]
        int[][] asignaciones = {
                {2, 2}, {3, 2}, {4, 3}, {5, 3}, {6, 3}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada asignación
            for (int[] asignacion : asignaciones) {
                // Asignar el ID del usuario
                consulta.setInt(1, asignacion[0]);
                // Asignar el ID del rol
                consulta.setInt(2, asignacion[1]);
                // Ejecutar INSERT de la relación
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 3: CORREOS ADICIONALES DE USUARIOS
    // ========================================================================

    /**
     * Inserta correos adicionales para los usuarios de prueba.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertCorreosUsuario(Connection conexion) throws Exception {
        // SQL para insertar un correo adicional asociado a un usuario
        String sql = "INSERT INTO Correos_Usuario (CORREO, USUARIO_ID) VALUES (?, ?)";
        // Datos: [correo, usuarioId]
        Object[][] correos = {
                {"carlos.personal@gmail.com",  2},
                {"laura.trabajo@outlook.com",  3},
                {"andres.dev@gmail.com",       4},
                {"vale.lopez@hotmail.com",     5},
                {"santi.herrera@gmail.com",    6}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada correo
            for (Object[] correo : correos) {
                // Asignar el correo adicional
                consulta.setString(1, (String) correo[0]);
                // Asignar el ID del usuario dueño del correo
                consulta.setInt(2, (int) correo[1]);
                // Ejecutar INSERT del correo
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 4: TELÉFONOS DE USUARIOS
    // ========================================================================

    /**
     * Inserta números de teléfono para los usuarios de prueba.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertNumerosUsuario(Connection conexion) throws Exception {
        // SQL para insertar un número de teléfono asociado a un usuario
        String sql = "INSERT INTO Numeros_Usuario (NUMERO, USUARIO_ID) VALUES (?, ?)";
        // Datos: [número, usuarioId]
        Object[][] numeros = {
                {"3101234567", 2},
                {"3209876543", 3},
                {"3154567890", 4},
                {"3006543210", 5},
                {"3181112233", 6}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada número
            for (Object[] numero : numeros) {
                // Asignar el número de teléfono
                consulta.setString(1, (String) numero[0]);
                // Asignar el ID del usuario dueño del número
                consulta.setInt(2, (int) numero[1]);
                // Ejecutar INSERT del número
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 5: CATEGORÍAS DE PRODUCTOS
    // ========================================================================

    /**
     * Inserta 5 categorías de productos para la tienda de ropa.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertCategorias(Connection conexion) throws Exception {
        // SQL para insertar una categoría con nombre, descripción y estado
        String sql = "INSERT INTO Categoria (NOMBRE, DESCRIPCION, ESTADO) VALUES (?, ?, ?)";
        // Datos: [nombre, descripción]
        String[][] categorias = {
                {"Camisetas",  "Camisetas casuales y deportivas para hombre y mujer"},
                {"Pantalones", "Jeans, joggers y pantalones formales"},
                {"Zapatos",    "Calzado deportivo, casual y formal"},
                {"Accesorios", "Gorras, cinturones, relojes y billeteras"},
                {"Chaquetas",  "Chaquetas, buzos y hoodies para clima frío"}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada categoría
            for (String[] categoria : categorias) {
                // Asignar el nombre de la categoría
                consulta.setString(1, categoria[0]);
                // Asignar la descripción de la categoría
                consulta.setString(2, categoria[1]);
                // Asignar el estado activo
                consulta.setBoolean(3, true);
                // Ejecutar INSERT de la categoría
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 6: CLIENTES
    // ========================================================================

    /**
     * Inserta 5 clientes con datos colombianos.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertClientes(Connection conexion) throws Exception {
        // SQL para insertar un cliente con todos sus campos
        String sql = "INSERT INTO Clientes (NOMBRE, DOCUMENTO, CORREO, TELEFONO, DIRECCION, CIUDAD, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Datos: [nombre, documento, correo, teléfono, dirección, ciudad]
        Object[][] clientes = {
                {"María Fernanda Ruiz",  1032456789L, "maria.ruiz@gmail.com",     "3112345678", "Cra 15 #45-20",   "Bogotá"},
                {"Juan David Ospina",    1098765432L, "juan.ospina@hotmail.com",   "3001234567", "Calle 50 #30-15", "Medellín"},
                {"Camila Andrea Torres", 1054321987L, "camila.torres@gmail.com",   "3156789012", "Av 6N #25-40",    "Cali"},
                {"Daniel Felipe Vargas", 1076543210L, "daniel.vargas@outlook.com", "3187654321", "Cra 27 #36-10",   "Bucaramanga"},
                {"Sofía Alejandra Peña", 1023456780L, "sofia.pena@gmail.com",      "3209871234", "Calle 72 #10-05", "Barranquilla"}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada cliente
            for (Object[] cliente : clientes) {
                // Asignar el nombre del cliente
                consulta.setString(1, (String) cliente[0]);
                // Asignar el documento (cédula colombiana)
                consulta.setLong(2, (long) cliente[1]);
                // Asignar el correo electrónico
                consulta.setString(3, (String) cliente[2]);
                // Asignar el teléfono
                consulta.setString(4, (String) cliente[3]);
                // Asignar la dirección
                consulta.setString(5, (String) cliente[4]);
                // Asignar la ciudad
                consulta.setString(6, (String) cliente[5]);
                // Asignar el estado activo
                consulta.setBoolean(7, true);
                // Ejecutar INSERT del cliente
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 7: PROVEEDORES
    // ========================================================================

    /**
     * Inserta 5 proveedores con datos colombianos y NIT.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertProveedores(Connection conexion) throws Exception {
        // SQL para insertar un proveedor con todos sus campos
        String sql = "INSERT INTO Proveedores (NOMBRE, RAZON_SOCIAL, NIT, CORREO, TELEFONO, DIRECCION, CIUDAD, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // Datos: [nombre, razónSocial, nit, correo, teléfono, dirección, ciudad]
        String[][] proveedores = {
                {"TextilColombia", "Textil Colombia S.A.S.",      "900123456-1", "ventas@textilcolombia.co",  "6012345678", "Zona Industrial Km 5",    "Bogotá"},
                {"CalzaExpress",   "Calza Express Ltda.",          "800987654-3", "pedidos@calzaexpress.co",   "6049876543", "Calle 10 #45-30 Itagüí", "Medellín"},
                {"ModaUrbana",     "Moda Urbana Colombia S.A.",    "901234567-8", "contacto@modaurbana.co",    "6025551234", "Av 3N #50-12",            "Cali"},
                {"AccesoriosPlus", "Accesorios Plus S.A.S.",       "900654321-5", "info@accesoriosplus.co",    "6076543210", "Cra 33 #52-80",           "Bucaramanga"},
                {"ImportaTextil",  "Importaciones Textiles Ltda.", "800111222-9", "compras@importatextil.co",  "6053214567", "Zona Franca Lote 8",      "Barranquilla"}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada proveedor
            for (String[] proveedor : proveedores) {
                // Asignar el nombre del contacto
                consulta.setString(1, proveedor[0]);
                // Asignar la razón social
                consulta.setString(2, proveedor[1]);
                // Asignar el NIT colombiano
                consulta.setString(3, proveedor[2]);
                // Asignar el correo electrónico
                consulta.setString(4, proveedor[3]);
                // Asignar el teléfono
                consulta.setString(5, proveedor[4]);
                // Asignar la dirección
                consulta.setString(6, proveedor[5]);
                // Asignar la ciudad
                consulta.setString(7, proveedor[6]);
                // Asignar el estado activo
                consulta.setBoolean(8, true);
                // Ejecutar INSERT del proveedor
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 8: PRODUCTOS
    // ========================================================================

    /**
     * Inserta 5 productos de ropa, uno por categoría.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertProductos(Connection conexion) throws Exception {
        // SQL para insertar un producto con todos sus campos
        String sql = "INSERT INTO Producto (NOMBRE, DESCRIPCION, PRECIO_VENTA, COSTO_PROMEDIO, STOCK, ESTADO, CATEGORIA_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Datos: [nombre, descripción, precioVenta, costoPromedio, stock, categoríaId]
        Object[][] productos = {
                {"Camiseta Básica Algodón",  "Camiseta 100% algodón cuello redondo",           45000.00, 22000.00, 50, 1},
                {"Jean Slim Fit Azul",       "Jean stretch slim fit color azul oscuro",         89000.00, 42000.00, 35, 2},
                {"Tenis Deportivos Runner",  "Tenis para correr con suela amortiguada",       135000.00, 65000.00, 25, 3},
                {"Cinturón Cuero Negro",     "Cinturón 100% cuero con hebilla metálica",       38000.00, 15000.00, 40, 4},
                {"Hoodie Oversize Gris",     "Hoodie oversize algodón perchado gris melange",  78000.00, 35000.00, 30, 5}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada producto
            for (Object[] producto : productos) {
                // Asignar el nombre del producto
                consulta.setString(1, (String) producto[0]);
                // Asignar la descripción del producto
                consulta.setString(2, (String) producto[1]);
                // Asignar el precio de venta
                consulta.setDouble(3, (double) producto[2]);
                // Asignar el costo promedio
                consulta.setDouble(4, (double) producto[3]);
                // Asignar el stock inicial
                consulta.setInt(5, (int) producto[4]);
                // Asignar el estado activo
                consulta.setBoolean(6, true);
                // Asignar el ID de la categoría
                consulta.setInt(7, (int) producto[5]);
                // Ejecutar INSERT del producto
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 9: COMPRAS Y DETALLES
    // ========================================================================

    /**
     * Inserta 5 compras a proveedores.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertCompras(Connection conexion) throws Exception {
        // SQL para insertar una compra con fecha, total, método de pago, usuario y proveedor
        String sql = "INSERT INTO Compra (FECHA_COMPRA, TOTAL_COMPRA, METODO_PAGO, USUARIO_ID, PROVEEDOR_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [fecha, total, metodoPago, usuarioId, proveedorId]
        Object[][] compras = {
                {"2025-01-15", 1100000.00, "Transferencia", 2, 1},
                {"2025-01-20", 2100000.00, "Transferencia", 2, 2},
                {"2025-02-05", 1625000.00, "Efectivo",      2, 3},
                {"2025-02-18",  750000.00, "Transferencia", 3, 4},
                {"2025-03-01", 1750000.00, "Transferencia", 3, 5}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada compra
            for (Object[] compra : compras) {
                // Asignar la fecha de compra
                consulta.setString(1, (String) compra[0]);
                // Asignar el total de la compra
                consulta.setDouble(2, (double) compra[1]);
                // Asignar el método de pago
                consulta.setString(3, (String) compra[2]);
                // Asignar el ID del usuario que registró la compra
                consulta.setInt(4, (int) compra[3]);
                // Asignar el ID del proveedor
                consulta.setInt(5, (int) compra[4]);
                // Ejecutar INSERT de la compra
                consulta.executeUpdate();
            }
        }
    }

    /**
     * Inserta los detalles de las 5 compras (1 detalle por compra).
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertDetalleCompras(Connection conexion) throws Exception {
        // SQL para insertar un detalle de compra
        String sql = "INSERT INTO Detalle_Compra (CANTIDAD, COSTO_UNITARIO, SUBTOTAL, COMPRA_ID, PRODUCTO_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [cantidad, costoUnitario, subtotal, compraId, productoId]
        Object[][] detalles = {
                {50, 22000.00, 1100000.00, 1, 1},
                {50, 42000.00, 2100000.00, 2, 2},
                {25, 65000.00, 1625000.00, 3, 3},
                {50, 15000.00,  750000.00, 4, 4},
                {50, 35000.00, 1750000.00, 5, 5}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada detalle
            for (Object[] detalle : detalles) {
                // Asignar la cantidad comprada
                consulta.setInt(1, (int) detalle[0]);
                // Asignar el costo unitario
                consulta.setDouble(2, (double) detalle[1]);
                // Asignar el subtotal (cantidad * costoUnitario)
                consulta.setDouble(3, (double) detalle[2]);
                // Asignar el ID de la compra padre
                consulta.setInt(4, (int) detalle[3]);
                // Asignar el ID del producto comprado
                consulta.setInt(5, (int) detalle[4]);
                // Ejecutar INSERT del detalle
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 10: VENTAS Y DETALLES
    // ========================================================================

    /**
     * Inserta 5 ventas a clientes.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertVentas(Connection conexion) throws Exception {
        // SQL para insertar una venta con fecha, total, método de pago, usuario y cliente
        String sql = "INSERT INTO Venta (FECHA_VENTA, TOTAL_VENTA, METODO_PAGO, USUARIO_ID, CLIENTE_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [fecha, total, metodoPago, usuarioId, clienteId]
        Object[][] ventas = {
                {"2025-02-10", 225000.00, "Efectivo",      4, 1},
                {"2025-02-14", 178000.00, "Transferencia", 4, 2},
                {"2025-02-20", 270000.00, "Efectivo",      5, 3},
                {"2025-03-01", 135000.00, "Transferencia", 5, 4},
                {"2025-03-05", 156000.00, "Efectivo",      4, 5}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada venta
            for (Object[] venta : ventas) {
                // Asignar la fecha de venta
                consulta.setString(1, (String) venta[0]);
                // Asignar el total de la venta
                consulta.setDouble(2, (double) venta[1]);
                // Asignar el método de pago
                consulta.setString(3, (String) venta[2]);
                // Asignar el ID del usuario que registró la venta
                consulta.setInt(4, (int) venta[3]);
                // Asignar el ID del cliente
                consulta.setInt(5, (int) venta[4]);
                // Ejecutar INSERT de la venta
                consulta.executeUpdate();
            }
        }
    }

    /**
     * Inserta los detalles de las 5 ventas (1 detalle por venta).
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertDetalleVentas(Connection conexion) throws Exception {
        // SQL para insertar un detalle de venta
        String sql = "INSERT INTO Detalle_Venta (CANTIDAD, PRECIO_UNITARIO, SUBTOTAL, VENTA_ID, PRODUCTO_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [cantidad, precioUnitario, subtotal, ventaId, productoId]
        Object[][] detalles = {
                {5,  45000.00,  225000.00, 1, 1},
                {2,  89000.00,  178000.00, 2, 2},
                {2, 135000.00,  270000.00, 3, 3},
                {1, 135000.00,  135000.00, 4, 3},
                {2,  78000.00,  156000.00, 5, 5}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada detalle
            for (Object[] detalle : detalles) {
                // Asignar la cantidad vendida
                consulta.setInt(1, (int) detalle[0]);
                // Asignar el precio unitario
                consulta.setDouble(2, (double) detalle[1]);
                // Asignar el subtotal (cantidad * precioUnitario)
                consulta.setDouble(3, (double) detalle[2]);
                // Asignar el ID de la venta padre
                consulta.setInt(4, (int) detalle[3]);
                // Asignar el ID del producto vendido
                consulta.setInt(5, (int) detalle[4]);
                // Ejecutar INSERT del detalle
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 11: GASTOS ADICIONALES
    // ========================================================================

    /**
     * Inserta 5 gastos adicionales del negocio.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertGastosAdicionales(Connection conexion) throws Exception {
        // SQL para insertar un gasto adicional
        String sql = "INSERT INTO Gastos_Adicionales (MONTO, DESCRIPCION, FECHA_REGISTRO, METODO_PAGO) VALUES (?, ?, ?, ?)";
        // Datos: [monto, descripción, fecha, metodoPago]
        Object[][] gastos = {
                {120000.00, "Flete envío TextilColombia Bogotá",      "2025-01-16", "Efectivo"},
                { 85000.00, "Flete envío CalzaExpress Medellín",      "2025-01-21", "Transferencia"},
                {350000.00, "IVA importación telas primer trimestre",  "2025-02-01", "Transferencia"},
                { 60000.00, "Cajas y bolsas para empaque febrero",     "2025-02-10", "Efectivo"},
                { 45000.00, "Papelería y útiles de oficina",           "2025-03-01", "Efectivo"}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada gasto
            for (Object[] gasto : gastos) {
                // Asignar el monto del gasto
                consulta.setDouble(1, (double) gasto[0]);
                // Asignar la descripción del gasto
                consulta.setString(2, (String) gasto[1]);
                // Asignar la fecha de registro
                consulta.setString(3, (String) gasto[2]);
                // Asignar el método de pago
                consulta.setString(4, (String) gasto[3]);
                // Ejecutar INSERT del gasto
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 12: MOVIMIENTOS FINANCIEROS
    // ========================================================================

    /**
     * Inserta 15 movimientos financieros (5 compras + 5 ventas + 5 gastos).
     * Tipo: 1=Venta(Ingreso), 2=Compra(Egreso), 3=Gasto Adicional(Egreso).
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertMovimientosFinancieros(Connection conexion) throws Exception {
        // SQL para insertar un movimiento financiero con todos sus campos
        String sql = "INSERT INTO Movimientos_Financieros (FECHA_MOVIMIENTO, CONCEPTO, MONTO, METODO_PAGO, TIPO_MOVIMIENTO_ID, USUARIO_ID, VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            // ----- Movimientos de COMPRAS (egreso, tipo 2) -----
            // Datos: [fecha, concepto, monto, metodoPago, tipoId, usuarioId, ventaId, compraId, gastoId]
            Object[][] movCompras = {
                    {"2025-01-15", "Compra camisetas - TextilColombia",  1100000.00, "Transferencia", 2, 2, null, 1,    null},
                    {"2025-01-20", "Compra jeans - CalzaExpress",         2100000.00, "Transferencia", 2, 2, null, 2,    null},
                    {"2025-02-05", "Compra tenis - ModaUrbana",           1625000.00, "Efectivo",      2, 2, null, 3,    null},
                    {"2025-02-18", "Compra cinturones - AccesoriosPlus",   750000.00, "Transferencia", 2, 3, null, 4,    null},
                    {"2025-03-01", "Compra hoodies - ImportaTextil",      1750000.00, "Transferencia", 2, 3, null, 5,    null}
            };
            // Insertar movimientos de compras
            insertMovimientos(consulta, movCompras);

            // ----- Movimientos de VENTAS (ingreso, tipo 1) -----
            Object[][] movVentas = {
                    {"2025-02-10", "Venta camisetas - María Fernanda Ruiz",  225000.00, "Efectivo",      1, 4, 1,    null, null},
                    {"2025-02-14", "Venta jeans - Juan David Ospina",         178000.00, "Transferencia", 1, 4, 2,    null, null},
                    {"2025-02-20", "Venta tenis - Camila Andrea Torres",      270000.00, "Efectivo",      1, 5, 3,    null, null},
                    {"2025-03-01", "Venta tenis - Daniel Felipe Vargas",      135000.00, "Transferencia", 1, 5, 4,    null, null},
                    {"2025-03-05", "Venta hoodies - Sofía Alejandra Peña",    156000.00, "Efectivo",      1, 4, 5,    null, null}
            };
            // Insertar movimientos de ventas
            insertMovimientos(consulta, movVentas);

            // ----- Movimientos de GASTOS ADICIONALES (egreso, tipo 3) -----
            Object[][] movGastos = {
                    {"2025-01-16", "Flete envío TextilColombia Bogotá",       120000.00, "Efectivo",      3, 2, null, null, 1},
                    {"2025-01-21", "Flete envío CalzaExpress Medellín",        85000.00, "Transferencia", 3, 2, null, null, 2},
                    {"2025-02-01", "IVA importación telas primer trimestre",  350000.00, "Transferencia", 3, 3, null, null, 3},
                    {"2025-02-10", "Cajas y bolsas para empaque febrero",      60000.00, "Efectivo",      3, 2, null, null, 4},
                    {"2025-03-01", "Papelería y útiles de oficina",            45000.00, "Efectivo",      3, 3, null, null, 5}
            };
            // Insertar movimientos de gastos
            insertMovimientos(consulta, movGastos);
        }
    }

    /**
     * Inserta un lote de movimientos financieros usando un PreparedStatement compartido.
     * Maneja los campos nullable (ventaId, compraId, gastoAdicionalId) correctamente.
     * @param consulta PreparedStatement ya preparado para movimientos financieros
     * @param movimientos Arreglo de datos de movimientos a insertar
     */
    private static void insertMovimientos(PreparedStatement consulta, Object[][] movimientos) throws Exception {
        // Recorrer cada movimiento del lote
        for (Object[] mov : movimientos) {
            // Asignar la fecha del movimiento
            consulta.setString(1, (String) mov[0]);
            // Asignar el concepto del movimiento
            consulta.setString(2, (String) mov[1]);
            // Asignar el monto del movimiento
            consulta.setDouble(3, (double) mov[2]);
            // Asignar el método de pago
            consulta.setString(4, (String) mov[3]);
            // Asignar el tipo de movimiento (1=Venta, 2=Compra, 3=Gasto)
            consulta.setInt(5, (int) mov[4]);
            // Asignar el ID del usuario que registró el movimiento
            consulta.setInt(6, (int) mov[5]);
            // Asignar el ID de la venta (null si no aplica)
            if (mov[6] != null) {
                // Asignar el ID de la venta asociada
                consulta.setInt(7, (int) mov[6]);
            } else {
                // Asignar NULL al campo venta_id
                consulta.setNull(7, java.sql.Types.INTEGER);
            }
            // Asignar el ID de la compra (null si no aplica)
            if (mov[7] != null) {
                // Asignar el ID de la compra asociada
                consulta.setInt(8, (int) mov[7]);
            } else {
                // Asignar NULL al campo compra_id
                consulta.setNull(8, java.sql.Types.INTEGER);
            }
            // Asignar el ID del gasto adicional (null si no aplica)
            if (mov[8] != null) {
                // Asignar el ID del gasto asociado
                consulta.setInt(9, (int) mov[8]);
            } else {
                // Asignar NULL al campo gasto_adicional_id
                consulta.setNull(9, java.sql.Types.INTEGER);
            }
            // Ejecutar INSERT del movimiento
            consulta.executeUpdate();
        }
    }
}
