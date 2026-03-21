// Paquete de seeders para datos iniciales
package com.backend.seeders;

// Para obtener la conexión a la base de datos
import com.backend.config.dbConnection;
// Para generar hash BCrypt real de la contraseña
import com.backend.helpers.PasswordHelper;

// Para la conexión JDBC
import java.sql.Connection;
// Para las consultas parametrizadas
import java.sql.PreparedStatement;
// Para leer el resultado de la consulta de verificación
import java.sql.ResultSet;

/**
 * Seeder que inserta datos de demostración realistas para probar el sistema.
 * Todos los datos usan fechas de 2026, con actividad reciente en marzo
 * para que el dashboard muestre métricas desde el primer arranque.
 * Es idempotente: solo inserta datos si la tabla Categoria está vacía.
 * Inserta en orden de dependencia: usuarios, catálogo, transacciones.
 * Contraseña de todos los usuarios de prueba: "Admin123"
 * (hash BCrypt con cost 10).
 */
public class SeedDemoData {

    /** Hash BCrypt de "Admin123" generado dinámicamente con PasswordHelper */
    private static final String HASH_PASSWORD = PasswordHelper.hashPassword("Admin123");

    /** IDs generados de los 5 usuarios demo (Carlos, Laura, Andrés, Valentina, Santiago) */
    private static int[] idsUsuarios = new int[5];

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
            String sqlVerificacion = "SELECT COUNT(*) FROM Categorias";
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
            insertTelefonosUsuario(conexion);

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
        // SQL para insertar un usuario con nombre, apellido, contraseña y estado (sin correo, va en Correos_Usuario)
        String sql = "INSERT INTO Usuarios (NOMBRE_USUARIO, APELLIDO_USUARIO, CONTRASENA, ESTADO_USUARIO) VALUES (?, ?, ?, ?)";
        // SQL para insertar el correo principal en Correos_Usuario con ES_PRINCIPAL=TRUE
        String sqlCorreo = "INSERT INTO Correos_Usuarios (CORREO_USUARIO, ES_PRINCIPAL, USUARIO_ID) VALUES (?, TRUE, ?)";
        // Datos de los usuarios: [nombre, apellido, correo]
        String[][] usuarios = {
                {"Carlos",    "Martínez",  "carlos.martinez@urbanlife.com"},
                {"Laura",     "González",  "laura.gonzalez@urbanlife.com"},
                {"Andrés",    "Rodríguez", "andres.rodriguez@urbanlife.com"},
                {"Valentina", "López",     "valentina.lopez@urbanlife.com"},
                {"Santiago",  "Herrera",   "santiago.herrera@urbanlife.com"}
        };
        // Preparar consulta de inserción de usuario
        try (PreparedStatement consulta = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement consultaCorreo = conexion.prepareStatement(sqlCorreo)) {
            // Recorrer cada usuario definido en el arreglo
            for (int i = 0; i < usuarios.length; i++) {
                // Asignar el nombre del usuario
                consulta.setString(1, usuarios[i][0]);
                // Asignar el apellido del usuario
                consulta.setString(2, usuarios[i][1]);
                // Asignar la contraseña hasheada (Admin123)
                consulta.setString(3, HASH_PASSWORD);
                // Asignar el estado activo
                consulta.setBoolean(4, true);
                // Ejecutar INSERT del usuario
                consulta.executeUpdate();

                // Obtener el ID generado del usuario creado
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Verificar que se obtuvo el ID
                if (clavesGeneradas.next()) {
                    // Leer el ID del usuario creado
                    int idUsuario = clavesGeneradas.getInt(1);
                    // Almacenar el ID generado en el array de IDs de usuarios demo
                    idsUsuarios[i] = idUsuario;
                    // Asignar el correo principal del usuario
                    consultaCorreo.setString(1, usuarios[i][2]);
                    // Asignar el ID del usuario recién creado
                    consultaCorreo.setInt(2, idUsuario);
                    // Ejecutar INSERT del correo principal
                    consultaCorreo.executeUpdate();
                }
            }
        }
    }

    // ========================================================================
    // SECCIÓN 2: ASIGNACIÓN DE ROLES A USUARIOS
    // ========================================================================

    /**
     * Asigna roles a los usuarios de prueba.
     * Carlos[0] y Laura[1] = ADMIN(rol 2), Andrés[2], Valentina[3] y Santiago[4] = EMPLEADO(rol 3).
     * Usa los IDs generados dinámicamente en insertUsuarios().
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertUsuarioRol(Connection conexion) throws Exception {
        // SQL para insertar una relación usuario-rol
        String sql = "INSERT INTO Usuarios_Roles (USUARIO_ID, ROL_ID) VALUES (?, ?)";
        // Asignaciones: [índice en idsUsuarios, rolId] — Carlos y Laura son ADMIN, el resto EMPLEADO
        int[][] asignaciones = {
                {0, 2}, {1, 2}, {2, 3}, {3, 3}, {4, 3}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada asignación
            for (int[] asignacion : asignaciones) {
                // Asignar el ID del usuario usando el array de IDs generados
                consulta.setInt(1, idsUsuarios[asignacion[0]]);
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
        // SQL para insertar un correo adicional asociado a un usuario (ES_PRINCIPAL=NULL = secundario)
        String sql = "INSERT INTO Correos_Usuarios (CORREO_USUARIO, USUARIO_ID) VALUES (?, ?)";
        // Datos: [correo, índice en idsUsuarios]
        Object[][] correos = {
                {"carlos.personal@gmail.com",  0},
                {"laura.trabajo@outlook.com",  1},
                {"andres.dev@gmail.com",       2},
                {"vale.lopez@hotmail.com",     3},
                {"santi.herrera@gmail.com",    4}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada correo
            for (Object[] correo : correos) {
                // Asignar el correo adicional
                consulta.setString(1, (String) correo[0]);
                // Asignar el ID del usuario usando el array de IDs generados
                consulta.setInt(2, idsUsuarios[(int) correo[1]]);
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
    private static void insertTelefonosUsuario(Connection conexion) throws Exception {
        // SQL para insertar un teléfono asociado a un usuario
        String sql = "INSERT INTO Telefonos_Usuarios (TELEFONO_USUARIO, USUARIO_ID) VALUES (?, ?)";
        // Datos: [teléfono, índice en idsUsuarios]
        Object[][] telefonos = {
                {"3101234567", 0},
                {"3209876543", 1},
                {"3154567890", 2},
                {"3006543210", 3},
                {"3181112233", 4}
        };
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Recorrer cada teléfono
            for (Object[] telefono : telefonos) {
                // Asignar el número de teléfono
                consulta.setString(1, (String) telefono[0]);
                // Asignar el ID del usuario usando el array de IDs generados
                consulta.setInt(2, idsUsuarios[(int) telefono[1]]);
                // Ejecutar INSERT del teléfono
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
        String sql = "INSERT INTO Categorias (NOMBRE_CATEGORIA, DESCRIPCION_CATEGORIA, ESTADO_CATEGORIA) VALUES (?, ?, ?)";
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
     * El cliente ID 1 ("Administracion") ya fue creado por SeedClienteDefault.
     * Estos clientes serán IDs 2-6.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertClientes(Connection conexion) throws Exception {
        // SQL para insertar un cliente con todos sus campos
        String sql = "INSERT INTO Clientes (NOMBRE_CLIENTE, DOCUMENTO_CLIENTE, CORREO_CLIENTE, TELEFONO_CLIENTE, DIRECCION_CLIENTE, CIUDAD_CLIENTE, ESTADO_CLIENTE) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
        String sql = "INSERT INTO Proveedores (NOMBRE_PROVEEDOR, RAZON_SOCIAL, NIT, CORREO_PROVEEDOR, TELEFONO_PROVEEDOR, DIRECCION_PROVEEDOR, CIUDAD_PROVEEDOR, ESTADO_PROVEEDOR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
     * Inserta 8 productos de ropa distribuidos en las 5 categorías.
     * El stock refleja el estado actual después de compras y ventas demo.
     * Prod 1=Camiseta, 2=Polo, 3=Jean, 4=Jogger, 5=Tenis, 6=Cinturón, 7=Gorra, 8=Hoodie
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertProductos(Connection conexion) throws Exception {
        // SQL para insertar un producto con todos sus campos
        String sql = "INSERT INTO Productos (NOMBRE_PRODUCTO, DESCRIPCION_PRODUCTO, PRECIO_VENTA, COSTO_PROMEDIO, STOCK, ESTADO_PRODUCTO, CATEGORIA_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Datos: [nombre, descripción, precioVenta, costoPromedio, stock, categoríaId]
        // Stock = unidades compradas - unidades vendidas en los datos demo
        // Prod 1 (Camiseta): 50 compradas - 8 vendidas = 42
        // Prod 2 (Polo): 0 compradas - 0 vendidas = 30 (stock inicial directo)
        // Prod 3 (Jean): 35 compradas - 4 vendidas = 31
        // Prod 4 (Jogger): 0 compradas - 0 vendidas = 20 (stock inicial directo)
        // Prod 5 (Tenis): 25 compradas - 3 vendidas = 22
        // Prod 6 (Cinturón): 40 compradas - 7 vendidas = 33
        // Prod 7 (Gorra): 30 compradas - 0 vendidas = 30
        // Prod 8 (Hoodie): 30 compradas - 5 vendidas = 25
        Object[][] productos = {
                {"Camiseta Básica Algodón",     "Camiseta 100% algodón cuello redondo unisex",      45000.00, 22000.00, 42, 1},
                {"Polo Premium Piqué",          "Polo manga corta tejido piqué con botones",         62000.00, 30000.00, 30, 1},
                {"Jean Slim Fit Azul",          "Jean stretch slim fit color azul oscuro",            89000.00, 42000.00, 31, 2},
                {"Jogger Cargo Negro",          "Pantalón jogger cargo con bolsillos laterales",      75000.00, 35000.00, 20, 2},
                {"Tenis Deportivos Runner",     "Tenis para correr con suela amortiguada",          135000.00, 65000.00, 22, 3},
                {"Cinturón Cuero Negro",        "Cinturón 100% cuero con hebilla metálica",          38000.00, 15000.00, 33, 4},
                {"Gorra Snapback UrbanLife",    "Gorra plana ajustable con logo bordado",             32000.00, 12000.00, 30, 4},
                {"Hoodie Oversize Gris",        "Hoodie oversize algodón perchado gris melange",     78000.00, 35000.00, 25, 5}
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
                // Asignar el stock actual (post compras y ventas demo)
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
     * Inserta 6 compras a proveedores distribuidas entre enero y marzo 2026.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertCompras(Connection conexion) throws Exception {
        // SQL para insertar una compra con fecha, total, método de pago, usuario y proveedor
        String sql = "INSERT INTO Compras (FECHA_COMPRA, TOTAL_COMPRA, METODO_PAGO_COMPRA, USUARIO_ID, PROVEEDOR_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [fecha, total, metodoPago, índice en idsUsuarios, proveedorId]
        // Carlos[0] y Laura[1] registran las compras como ADMIN
        Object[][] compras = {
                {"2026-01-10", 1100000.00, "Transferencia", 0, 1},
                {"2026-01-18", 1470000.00, "Transferencia", 0, 2},
                {"2026-02-03", 1625000.00, "Efectivo",      0, 3},
                {"2026-02-15",  600000.00, "Transferencia", 1, 4},
                {"2026-02-25", 1050000.00, "Transferencia", 1, 5},
                {"2026-03-05",  360000.00, "Efectivo",      0, 1}
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
                // Asignar el ID del usuario usando el array de IDs generados
                consulta.setInt(4, idsUsuarios[(int) compra[3]]);
                // Asignar el ID del proveedor
                consulta.setInt(5, (int) compra[4]);
                // Ejecutar INSERT de la compra
                consulta.executeUpdate();
            }
        }
    }

    /**
     * Inserta los detalles de las 6 compras (1 detalle por compra).
     * Prod 1=Camiseta, 3=Jean, 5=Tenis, 6=Cinturón, 7=Gorra, 8=Hoodie
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertDetalleCompras(Connection conexion) throws Exception {
        // SQL para insertar un detalle de compra
        String sql = "INSERT INTO Detalles_Compras (CANTIDAD_COMPRA, COSTO_UNITARIO, SUBTOTAL_COMPRA, COMPRA_ID, PRODUCTO_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [cantidad, costoUnitario, subtotal, compraId, productoId]
        Object[][] detalles = {
                {50, 22000.00, 1100000.00, 1, 1},
                {35, 42000.00, 1470000.00, 2, 3},
                {25, 65000.00, 1625000.00, 3, 5},
                {40, 15000.00,  600000.00, 4, 6},
                {30, 35000.00, 1050000.00, 5, 8},
                {30, 12000.00,  360000.00, 6, 7}
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
     * Inserta 10 ventas a clientes distribuidas entre febrero y marzo 2026.
     * Las ventas 6-10 están en la última semana (12-18 marzo) para el dashboard.
     * La venta 10 es de hoy (18 marzo) para las tarjetas de "hoy".
     * Cliente ID 1 = Administracion (default), IDs 2-6 = clientes demo.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertVentas(Connection conexion) throws Exception {
        // SQL para insertar una venta con fecha, total, método de pago, usuario y cliente
        String sql = "INSERT INTO Ventas (FECHA_VENTA, TOTAL_VENTA, METODO_PAGO_VENTA, USUARIO_ID, CLIENTE_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [fecha, total, metodoPago, índice en idsUsuarios, clienteId]
        // Andrés[2] y Valentina[3] registran las ventas como EMPLEADO
        Object[][] ventas = {
                {"2026-02-10", 135000.00, "Efectivo",      2, 2},
                {"2026-02-18", 178000.00, "Transferencia", 2, 3},
                {"2026-02-25", 135000.00, "Efectivo",      3, 4},
                {"2026-03-03", 152000.00, "Transferencia", 3, 5},
                {"2026-03-07", 156000.00, "Efectivo",      2, 6},
                {"2026-03-12", 225000.00, "Transferencia", 3, 2},
                {"2026-03-14", 270000.00, "Efectivo",      2, 3},
                {"2026-03-15", 114000.00, "Transferencia", 3, 4},
                {"2026-03-17", 178000.00, "Efectivo",      2, 5},
                {"2026-03-18", 234000.00, "Transferencia", 3, 6}
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
                // Asignar el ID del usuario usando el array de IDs generados
                consulta.setInt(4, idsUsuarios[(int) venta[3]]);
                // Asignar el ID del cliente
                consulta.setInt(5, (int) venta[4]);
                // Ejecutar INSERT de la venta
                consulta.executeUpdate();
            }
        }
    }

    /**
     * Inserta los detalles de las 10 ventas (1 detalle por venta).
     * Los precios unitarios corresponden al PRECIO_VENTA de cada producto.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertDetalleVentas(Connection conexion) throws Exception {
        // SQL para insertar un detalle de venta
        String sql = "INSERT INTO Detalles_Ventas (CANTIDAD_VENTA, PRECIO_UNITARIO, SUBTOTAL_VENTA, VENTA_ID, PRODUCTO_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [cantidad, precioUnitario, subtotal, ventaId, productoId]
        Object[][] detalles = {
                {3,  45000.00,  135000.00,  1, 1},
                {2,  89000.00,  178000.00,  2, 3},
                {1, 135000.00,  135000.00,  3, 5},
                {4,  38000.00,  152000.00,  4, 6},
                {2,  78000.00,  156000.00,  5, 8},
                {5,  45000.00,  225000.00,  6, 1},
                {2, 135000.00,  270000.00,  7, 5},
                {3,  38000.00,  114000.00,  8, 6},
                {2,  89000.00,  178000.00,  9, 3},
                {3,  78000.00,  234000.00, 10, 8}
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
     * Inserta 6 gastos adicionales del negocio entre enero y marzo 2026.
     * El gasto 6 es de hoy (18 marzo) para las tarjetas de "hoy" del dashboard.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertGastosAdicionales(Connection conexion) throws Exception {
        // SQL para insertar un gasto adicional con USUARIO_ID
        String sql = "INSERT INTO Gastos_Adicionales (MONTO, DESCRIPCION_GASTO, FECHA_REGISTRO, METODO_PAGO_GASTO, USUARIO_ID) VALUES (?, ?, ?, ?, ?)";
        // Datos: [monto, descripción, fecha, metodoPago, índice en idsUsuarios]
        // Carlos[0] y Laura[1] registran los gastos como ADMIN
        Object[][] gastos = {
                {120000.00, "Flete envío TextilColombia Bogotá",     "2026-01-12", "Efectivo",      0},
                { 85000.00, "Flete envío CalzaExpress Medellín",     "2026-01-20", "Transferencia", 0},
                {350000.00, "IVA importación telas primer trimestre", "2026-02-05", "Transferencia", 1},
                { 60000.00, "Cajas y bolsas para empaque febrero",    "2026-02-12", "Efectivo",      0},
                { 95000.00, "Mantenimiento local comercial",          "2026-03-10", "Transferencia", 1},
                {180000.00, "Servicios públicos local marzo",         "2026-03-18", "Efectivo",      0}
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
                // Asignar el ID del usuario usando el array de IDs generados
                consulta.setInt(5, idsUsuarios[(int) gasto[4]]);
                // Ejecutar INSERT del gasto
                consulta.executeUpdate();
            }
        }
    }

    // ========================================================================
    // SECCIÓN 12: MOVIMIENTOS FINANCIEROS
    // ========================================================================

    /**
     * Inserta 22 movimientos financieros (6 compras + 10 ventas + 6 gastos).
     * Tipo: 1=Venta(Ingreso), 2=Compra(Egreso), 3=Gasto Adicional(Egreso).
     * Usa 3 FKs separadas (VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) para vincular al registro origen.
     * Las fechas y montos coinciden exactamente con las compras, ventas y gastos insertados.
     * @param conexion Conexión activa con transacción en curso
     */
    private static void insertMovimientosFinancieros(Connection conexion) throws Exception {
        // SQL para insertar un movimiento financiero con las 3 FKs separadas
        String sql = "INSERT INTO Movimientos_Financieros (CONCEPTO, MONTO, FECHA, TIPO_MOVIMIENTO_ID, VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Preparar consulta de inserción
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {

            // ----- Movimientos de COMPRAS (egreso, tipo 2, COMPRA_ID = compraId) -----
            // Datos: [concepto, monto, fecha, tipoId, ventaId, compraId, gastoId]
            Object[][] movCompras = {
                    {"Compra #1 - Camisetas Algodón x50",    1100000.00, "2026-01-10", 2, null, 1, null},
                    {"Compra #2 - Jeans Slim Fit x35",       1470000.00, "2026-01-18", 2, null, 2, null},
                    {"Compra #3 - Tenis Deportivos x25",     1625000.00, "2026-02-03", 2, null, 3, null},
                    {"Compra #4 - Cinturones Cuero x40",      600000.00, "2026-02-15", 2, null, 4, null},
                    {"Compra #5 - Hoodies Oversize x30",     1050000.00, "2026-02-25", 2, null, 5, null},
                    {"Compra #6 - Gorras Snapback x30",       360000.00, "2026-03-05", 2, null, 6, null}
            };
            // Insertar movimientos de compras
            insertMovimientos(consulta, movCompras);

            // ----- Movimientos de VENTAS (ingreso, tipo 1, VENTA_ID = ventaId) -----
            Object[][] movVentas = {
                    {"Venta #1 - 3x Camiseta Básica",        135000.00, "2026-02-10", 1, 1, null, null},
                    {"Venta #2 - 2x Jean Slim Fit",          178000.00, "2026-02-18", 1, 2, null, null},
                    {"Venta #3 - 1x Tenis Deportivos",       135000.00, "2026-02-25", 1, 3, null, null},
                    {"Venta #4 - 4x Cinturón Cuero",         152000.00, "2026-03-03", 1, 4, null, null},
                    {"Venta #5 - 2x Hoodie Oversize",        156000.00, "2026-03-07", 1, 5, null, null},
                    {"Venta #6 - 5x Camiseta Básica",        225000.00, "2026-03-12", 1, 6, null, null},
                    {"Venta #7 - 2x Tenis Deportivos",       270000.00, "2026-03-14", 1, 7, null, null},
                    {"Venta #8 - 3x Cinturón Cuero",         114000.00, "2026-03-15", 1, 8, null, null},
                    {"Venta #9 - 2x Jean Slim Fit",          178000.00, "2026-03-17", 1, 9, null, null},
                    {"Venta #10 - 3x Hoodie Oversize",       234000.00, "2026-03-18", 1, 10, null, null}
            };
            // Insertar movimientos de ventas
            insertMovimientos(consulta, movVentas);

            // ----- Movimientos de GASTOS ADICIONALES (egreso, tipo 3, GASTO_ADICIONAL_ID = gastoId) -----
            Object[][] movGastos = {
                    {"Gasto #1 - Flete TextilColombia",       120000.00, "2026-01-12", 3, null, null, 1},
                    {"Gasto #2 - Flete CalzaExpress",          85000.00, "2026-01-20", 3, null, null, 2},
                    {"Gasto #3 - IVA importación telas",      350000.00, "2026-02-05", 3, null, null, 3},
                    {"Gasto #4 - Empaque febrero",             60000.00, "2026-02-12", 3, null, null, 4},
                    {"Gasto #5 - Mantenimiento local",         95000.00, "2026-03-10", 3, null, null, 5},
                    {"Gasto #6 - Servicios públicos marzo",   180000.00, "2026-03-18", 3, null, null, 6}
            };
            // Insertar movimientos de gastos
            insertMovimientos(consulta, movGastos);
        }
    }

    /**
     * Inserta un lote de movimientos financieros usando un PreparedStatement compartido.
     * @param consulta PreparedStatement ya preparado para movimientos financieros
     * @param movimientos Arreglo de datos de movimientos a insertar [concepto, monto, fecha, tipoId, ventaId, compraId, gastoId]
     */
    private static void insertMovimientos(PreparedStatement consulta, Object[][] movimientos) throws Exception {
        // Recorrer cada movimiento del lote
        for (Object[] mov : movimientos) {
            // Asignar el concepto del movimiento
            consulta.setString(1, (String) mov[0]);
            // Asignar el monto del movimiento
            consulta.setDouble(2, (double) mov[1]);
            // Asignar la fecha del movimiento
            consulta.setString(3, (String) mov[2]);
            // Asignar el tipo de movimiento (1=Venta, 2=Compra, 3=Gasto)
            consulta.setInt(4, (int) mov[3]);
            // Asignar el ID de la venta (null si no es tipo Venta)
            if (mov[4] != null) consulta.setInt(5, (int) mov[4]); else consulta.setNull(5, java.sql.Types.INTEGER);
            // Asignar el ID de la compra (null si no es tipo Compra)
            if (mov[5] != null) consulta.setInt(6, (int) mov[5]); else consulta.setNull(6, java.sql.Types.INTEGER);
            // Asignar el ID del gasto adicional (null si no es tipo Gasto)
            if (mov[6] != null) consulta.setInt(7, (int) mov[6]); else consulta.setNull(7, java.sql.Types.INTEGER);
            // Ejecutar INSERT del movimiento
            consulta.executeUpdate();
        }
    }
}
