-- ============================================
-- 1. CREAR BASE DE DATOS
-- ============================================
CREATE DATABASE IF NOT EXISTS UrbanLife; 
USE UrbanLife;

-- ============================================
-- 2. TABLAS INDEPENDIENTES
-- ============================================

CREATE TABLE Usuarios (
    ID_USUARIO INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    APELLIDO VARCHAR(100) NOT NULL,
    CORREO VARCHAR(120) NOT NULL UNIQUE,
    GOOGLE_ID VARCHAR(255) NULL UNIQUE,
    CONTRASENA VARCHAR(255) NULL,
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE Roles (
    ID_ROLES INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    DESCRIPCION VARCHAR(255) NOT NULL
);

CREATE TABLE Permisos (
    ID_PERMISOS INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    DESCRIPCION VARCHAR(255) NOT NULL
);

CREATE TABLE Categoria (
    ID_CATEGORIA INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    DESCRIPCION VARCHAR(255),
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE Tipo_Movimientos (
    ID_TIPO_MOVIMIENTOS INT AUTO_INCREMENT PRIMARY KEY,
    MOVIMIENTO ENUM('Venta', 'Compra', 'Gasto Adicional') NOT NULL,
    NATURALEZA ENUM('Ingreso', 'Egreso') NOT NULL
);

CREATE TABLE Tipo_Gasto (
    ID_TIPO_GASTO INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    DESCRIPCION VARCHAR(255) NULL
);

CREATE TABLE Clientes (
    ID_CLIENTE INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(150) NOT NULL,
    DOCUMENTO BIGINT NULL,
    CORREO VARCHAR(120) NULL,
    TELEFONO VARCHAR(50) NULL,
    DIRECCION VARCHAR(200) NULL,
    CIUDAD VARCHAR(100) NULL,
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE Proveedores (
    ID_PROVEEDOR INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(150) NOT NULL,
    RAZON_SOCIAL VARCHAR(150) NULL,
    NIT VARCHAR(50) NULL,
    CORREO VARCHAR(120) NULL,
    TELEFONO VARCHAR(50) NULL,
    DIRECCION VARCHAR(200) NULL,
    CIUDAD VARCHAR(100) NULL,
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- 3. TABLAS DEPENDIENTES
-- ============================================

CREATE TABLE Usuario_Rol (
    ID_USUARIO_ROL INT AUTO_INCREMENT PRIMARY KEY,
    USUARIO_ID INT NOT NULL,
    ROL_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO),
    FOREIGN KEY (ROL_ID) REFERENCES Roles(ID_ROLES),
    UNIQUE (USUARIO_ID, ROL_ID)
);

CREATE TABLE Rol_Permisos (
    ID_ROL_PERMISO INT AUTO_INCREMENT PRIMARY KEY,
    ROL_ID INT NOT NULL,
    PERMISOS_ID INT NOT NULL,
    FOREIGN KEY (ROL_ID) REFERENCES Roles(ID_ROLES),
    FOREIGN KEY (PERMISOS_ID) REFERENCES Permisos(ID_PERMISOS),
    UNIQUE (ROL_ID, PERMISOS_ID)
);

CREATE TABLE Correos_Usuario (
    ID_CORREO INT AUTO_INCREMENT PRIMARY KEY,
    CORREO VARCHAR(120) NOT NULL,
    USUARIO_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO) ON DELETE CASCADE
);

CREATE TABLE Numeros_Usuario (
    ID_NUMERO INT AUTO_INCREMENT PRIMARY KEY,
    NUMERO VARCHAR(50) NOT NULL,
    USUARIO_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO) ON DELETE CASCADE
);

-- ============================================
-- 4. TABLAS DE PRODUCTOS, VENTAS Y MOVIMIENTOS
-- ============================================

CREATE TABLE Producto (
    ID_PRODUCTO INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(150) NOT NULL,
    DESCRIPCION VARCHAR(255),
    PRECIO_VENTA DECIMAL(10,2) NOT NULL,
    COSTO_PROMEDIO DECIMAL(10,2) NOT NULL,
    STOCK INT NOT NULL DEFAULT 0,
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE,
    CATEGORIA_ID INT NOT NULL,
    FOREIGN KEY (CATEGORIA_ID) REFERENCES Categoria(ID_CATEGORIA)
);

CREATE TABLE Imagenes_Producto (
    IMAGEN_PRODUCTO INT AUTO_INCREMENT PRIMARY KEY,
    URL VARCHAR(255) NOT NULL,
    FECHA_REGISTRO DATE NOT NULL,
    PRODUCTO_ID INT NOT NULL,
    FOREIGN KEY (PRODUCTO_ID) REFERENCES Producto(ID_PRODUCTO)
);

CREATE TABLE Venta (
    ID_VENTA INT AUTO_INCREMENT PRIMARY KEY,
    FECHA_VENTA DATE NOT NULL,
    TOTAL_VENTA DECIMAL(10,2) NOT NULL,
    METODO_PAGO ENUM('Transferencia','Efectivo') NOT NULL,
    USUARIO_ID INT NOT NULL,
    CLIENTE_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO),
    FOREIGN KEY (CLIENTE_ID) REFERENCES Clientes(ID_CLIENTE)
);

CREATE TABLE Compra (
    ID_COMPRA INT AUTO_INCREMENT PRIMARY KEY,
    FECHA_COMPRA DATE NOT NULL,
    TOTAL_COMPRA DECIMAL(10,2) NOT NULL,
    METODO_PAGO ENUM('Transferencia','Efectivo') NOT NULL,
    USUARIO_ID INT NOT NULL,
    PROVEEDOR_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO),
    FOREIGN KEY (PROVEEDOR_ID) REFERENCES Proveedores(ID_PROVEEDOR)
);

CREATE TABLE Detalle_Compra (
    ID_DET_COMPRA INT AUTO_INCREMENT PRIMARY KEY,
    CANTIDAD INT NOT NULL,
    COSTO_UNITARIO DECIMAL(10,2) NOT NULL,
    SUBTOTAL DECIMAL(10,2) NOT NULL,
    COMPRA_ID INT NOT NULL,
    PRODUCTO_ID INT NOT NULL,
    FOREIGN KEY (COMPRA_ID) REFERENCES Compra(ID_COMPRA),
    FOREIGN KEY (PRODUCTO_ID) REFERENCES Producto(ID_PRODUCTO)
);

CREATE TABLE Detalle_Venta (
    ID_DET_VENTA INT AUTO_INCREMENT PRIMARY KEY,
    CANTIDAD INT NOT NULL,
    PRECIO_UNITARIO DECIMAL(10,2) NOT NULL,
    SUBTOTAL DECIMAL(10,2) NOT NULL,
    VENTA_ID INT NOT NULL,
    PRODUCTO_ID INT NOT NULL,
    FOREIGN KEY (VENTA_ID) REFERENCES Venta(ID_VENTA),
    FOREIGN KEY (PRODUCTO_ID) REFERENCES Producto(ID_PRODUCTO)
);

CREATE TABLE Gastos_Adicionales (
    ID_GASTOS_ADIC INT AUTO_INCREMENT PRIMARY KEY,
    MONTO DECIMAL(10,2) NOT NULL,
    DESCRIPCION VARCHAR(255),
    FECHA_REGISTRO DATE NOT NULL,
    METODO_PAGO ENUM('Transferencia', 'Efectivo') NOT NULL,
    COMPRA_ID INT NULL,
    TIPO_GASTO_ID INT NOT NULL,
    USUARIO_ID INT NOT NULL,
    FOREIGN KEY (COMPRA_ID) REFERENCES Compra(ID_COMPRA),
    FOREIGN KEY (TIPO_GASTO_ID) REFERENCES Tipo_Gasto(ID_TIPO_GASTO),
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO)
);

CREATE TABLE Movimientos_Financieros (
    ID_MOVS_FINANCIEROS INT AUTO_INCREMENT PRIMARY KEY,
    FECHA_MOVIMIENTO DATE NOT NULL,
    CONCEPTO VARCHAR(200) NOT NULL,
    MONTO DECIMAL(10,2) NOT NULL,
    METODO_PAGO ENUM('Transferencia', 'Efectivo') NOT NULL,
    TIPO_MOVIMIENTO_ID INT NOT NULL,
    USUARIO_ID INT NOT NULL,
    VENTA_ID INT NULL,
    COMPRA_ID INT NULL,
    GASTO_ADICIONAL_ID INT NULL,
    FOREIGN KEY (TIPO_MOVIMIENTO_ID) REFERENCES Tipo_Movimientos(ID_TIPO_MOVIMIENTOS),
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO),
    FOREIGN KEY (VENTA_ID) REFERENCES Venta(ID_VENTA),
    FOREIGN KEY (COMPRA_ID) REFERENCES Compra(ID_COMPRA),
    FOREIGN KEY (GASTO_ADICIONAL_ID) REFERENCES Gastos_Adicionales(ID_GASTOS_ADIC)
);

-- ============================================
-- 5. RECUPERACION DE CONTRASENA
-- ============================================

CREATE TABLE Token_Recuperacion (
    ID_TOKEN INT AUTO_INCREMENT PRIMARY KEY,
    USUARIO_ID INT NOT NULL,
    TOKEN VARCHAR(255) NOT NULL UNIQUE,
    FECHA_EXPIRACION DATETIME NOT NULL,
    USADO BOOLEAN DEFAULT FALSE,
    FECHA_CREACION DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO) ON DELETE CASCADE
);

-- ============================================
-- 6. DATOS DE PRUEBA (DEMO)
-- ============================================
-- NOTA: Los seeders ya insertan: Roles, Permisos, Tipo_Movimientos, Tipo_Gasto y SUPER_ADMIN (ID=1).
-- Estos inserts agregan datos de prueba para las tablas restantes.
-- Contraseña de todos los usuarios de prueba: "Admin123" (hash BCrypt con cost 10)

-- ----- Usuarios (5 usuarios de prueba) -----
-- ID 1 = SUPER_ADMIN (ya existe por el seeder), nuevos usuarios empiezan en ID 2
INSERT INTO Usuarios (NOMBRE, APELLIDO, CORREO, CONTRASENA, ESTADO) VALUES
('Carlos',    'Martínez',  'carlos.martinez@urbanlife.com',  '$2a$10$LpOVs3G0xHFn8zOQXE3p5u9JKbT0W6D4s1mR3YgF5vN7cXq2kB8Wy', TRUE),
('Laura',     'González',  'laura.gonzalez@urbanlife.com',   '$2a$10$LpOVs3G0xHFn8zOQXE3p5u9JKbT0W6D4s1mR3YgF5vN7cXq2kB8Wy', TRUE),
('Andrés',    'Rodríguez', 'andres.rodriguez@urbanlife.com', '$2a$10$LpOVs3G0xHFn8zOQXE3p5u9JKbT0W6D4s1mR3YgF5vN7cXq2kB8Wy', TRUE),
('Valentina', 'López',     'valentina.lopez@urbanlife.com',  '$2a$10$LpOVs3G0xHFn8zOQXE3p5u9JKbT0W6D4s1mR3YgF5vN7cXq2kB8Wy', TRUE),
('Santiago',  'Herrera',   'santiago.herrera@urbanlife.com',  '$2a$10$LpOVs3G0xHFn8zOQXE3p5u9JKbT0W6D4s1mR3YgF5vN7cXq2kB8Wy', TRUE);

-- ----- Usuario_Rol (asignar roles) -----
-- Carlos y Laura = ADMIN (rol 2), Andrés, Valentina y Santiago = EMPLEADO (rol 3)
INSERT INTO Usuario_Rol (USUARIO_ID, ROL_ID) VALUES
(2, 2), (3, 2), (4, 3), (5, 3), (6, 3);

-- ----- Rol_Permisos (asignar permisos a roles) -----
-- Permisos: 1=Gestionar Usuarios, 2=Gestionar Roles, 3=Gestionar Productos,
-- 4=Gestionar Categorias, 5=Gestionar Ventas, 6=Gestionar Compras,
-- 7=Gestionar Movimientos, 8=Gestionar Gastos, 9=Ver Reportes, 10=Gestionar Perfil
-- SUPER_ADMIN (rol 1) → todos los permisos
INSERT INTO Rol_Permisos (ROL_ID, PERMISOS_ID) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- ADMIN (rol 2) → todo excepto Gestionar Usuarios y Gestionar Roles
(2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10),
-- EMPLEADO (rol 3) → solo operaciones del día a día
(3, 3), (3, 5), (3, 7), (3, 10);

-- ----- Correos_Usuario (correos adicionales) -----
INSERT INTO Correos_Usuario (CORREO, USUARIO_ID) VALUES
('carlos.personal@gmail.com',  2),
('laura.trabajo@outlook.com',  3),
('andres.dev@gmail.com',       4),
('vale.lopez@hotmail.com',     5),
('santi.herrera@gmail.com',    6);

-- ----- Numeros_Usuario (teléfonos) -----
INSERT INTO Numeros_Usuario (NUMERO, USUARIO_ID) VALUES
('3101234567', 2),
('3209876543', 3),
('3154567890', 4),
('3006543210', 5),
('3181112233', 6);

-- ----- Categorías (5) -----
INSERT INTO Categoria (NOMBRE, DESCRIPCION, ESTADO) VALUES
('Camisetas',  'Camisetas casuales y deportivas para hombre y mujer',  TRUE),
('Pantalones', 'Jeans, joggers y pantalones formales',                 TRUE),
('Zapatos',    'Calzado deportivo, casual y formal',                   TRUE),
('Accesorios', 'Gorras, cinturones, relojes y billeteras',             TRUE),
('Chaquetas',  'Chaquetas, buzos y hoodies para clima frío',           TRUE);

-- ----- Clientes (5 — datos colombianos) -----
INSERT INTO Clientes (NOMBRE, DOCUMENTO, CORREO, TELEFONO, DIRECCION, CIUDAD, ESTADO) VALUES
('María Fernanda Ruiz',  1032456789, 'maria.ruiz@gmail.com',     '3112345678', 'Cra 15 #45-20',   'Bogotá',       TRUE),
('Juan David Ospina',    1098765432, 'juan.ospina@hotmail.com',   '3001234567', 'Calle 50 #30-15', 'Medellín',     TRUE),
('Camila Andrea Torres', 1054321987, 'camila.torres@gmail.com',   '3156789012', 'Av 6N #25-40',    'Cali',         TRUE),
('Daniel Felipe Vargas', 1076543210, 'daniel.vargas@outlook.com', '3187654321', 'Cra 27 #36-10',   'Bucaramanga',  TRUE),
('Sofía Alejandra Peña', 1023456780, 'sofia.pena@gmail.com',      '3209871234', 'Calle 72 #10-05', 'Barranquilla', TRUE);

-- ----- Proveedores (5 — datos colombianos con NIT) -----
INSERT INTO Proveedores (NOMBRE, RAZON_SOCIAL, NIT, CORREO, TELEFONO, DIRECCION, CIUDAD, ESTADO) VALUES
('TextilColombia', 'Textil Colombia S.A.S.',     '900123456-1', 'ventas@textilcolombia.co',  '6012345678', 'Zona Industrial Km 5',    'Bogotá',       TRUE),
('CalzaExpress',   'Calza Express Ltda.',         '800987654-3', 'pedidos@calzaexpress.co',   '6049876543', 'Calle 10 #45-30 Itagüí', 'Medellín',     TRUE),
('ModaUrbana',     'Moda Urbana Colombia S.A.',   '901234567-8', 'contacto@modaurbana.co',    '6025551234', 'Av 3N #50-12',            'Cali',         TRUE),
('AccesoriosPlus', 'Accesorios Plus S.A.S.',      '900654321-5', 'info@accesoriosplus.co',    '6076543210', 'Cra 33 #52-80',           'Bucaramanga',  TRUE),
('ImportaTextil',  'Importaciones Textiles Ltda.', '800111222-9', 'compras@importatextil.co', '6053214567', 'Zona Franca Lote 8',      'Barranquilla', TRUE);

-- ----- Productos (5 — uno por categoría) -----
INSERT INTO Producto (NOMBRE, DESCRIPCION, PRECIO_VENTA, COSTO_PROMEDIO, STOCK, ESTADO, CATEGORIA_ID) VALUES
('Camiseta Básica Algodón',  'Camiseta 100% algodón cuello redondo',           45000.00, 22000.00, 50, TRUE, 1),
('Jean Slim Fit Azul',       'Jean stretch slim fit color azul oscuro',         89000.00, 42000.00, 35, TRUE, 2),
('Tenis Deportivos Runner',  'Tenis para correr con suela amortiguada',       135000.00, 65000.00, 25, TRUE, 3),
('Cinturón Cuero Negro',     'Cinturón 100% cuero con hebilla metálica',       38000.00, 15000.00, 40, TRUE, 4),
('Hoodie Oversize Gris',     'Hoodie oversize algodón perchado gris melange',  78000.00, 35000.00, 30, TRUE, 5);

-- ----- Compras (5 — una por proveedor) -----
INSERT INTO Compra (FECHA_COMPRA, TOTAL_COMPRA, METODO_PAGO, USUARIO_ID, PROVEEDOR_ID) VALUES
('2025-01-15', 1100000.00, 'Transferencia', 2, 1),
('2025-01-20', 2100000.00, 'Transferencia', 2, 2),
('2025-02-05', 1625000.00, 'Efectivo',      2, 3),
('2025-02-18',  750000.00, 'Transferencia', 3, 4),
('2025-03-01', 1750000.00, 'Transferencia', 3, 5);

-- ----- Detalle_Compra (1 detalle por compra) -----
INSERT INTO Detalle_Compra (CANTIDAD, COSTO_UNITARIO, SUBTOTAL, COMPRA_ID, PRODUCTO_ID) VALUES
(50, 22000.00, 1100000.00, 1, 1),
(50, 42000.00, 2100000.00, 2, 2),
(25, 65000.00, 1625000.00, 3, 3),
(50, 15000.00,  750000.00, 4, 4),
(50, 35000.00, 1750000.00, 5, 5);

-- ----- Ventas (5 — una por cliente) -----
INSERT INTO Venta (FECHA_VENTA, TOTAL_VENTA, METODO_PAGO, USUARIO_ID, CLIENTE_ID) VALUES
('2025-02-10', 225000.00, 'Efectivo',      4, 1),
('2025-02-14', 178000.00, 'Transferencia', 4, 2),
('2025-02-20', 270000.00, 'Efectivo',      5, 3),
('2025-03-01', 135000.00, 'Transferencia', 5, 4),
('2025-03-05', 156000.00, 'Efectivo',      4, 5);

-- ----- Detalle_Venta (1 detalle por venta) -----
INSERT INTO Detalle_Venta (CANTIDAD, PRECIO_UNITARIO, SUBTOTAL, VENTA_ID, PRODUCTO_ID) VALUES
(5,  45000.00,  225000.00, 1, 1),
(2,  89000.00,  178000.00, 2, 2),
(2, 135000.00,  270000.00, 3, 3),
(1, 135000.00,  135000.00, 4, 3),
(2,  78000.00,  156000.00, 5, 5);

-- ----- Gastos_Adicionales (5 — diferentes tipos de gasto) -----
INSERT INTO Gastos_Adicionales (MONTO, DESCRIPCION, FECHA_REGISTRO, METODO_PAGO, COMPRA_ID, TIPO_GASTO_ID, USUARIO_ID) VALUES
(120000.00, 'Flete envío TextilColombia Bogotá',      '2025-01-16', 'Efectivo',      1,    1, 2),
( 85000.00, 'Flete envío CalzaExpress Medellín',      '2025-01-21', 'Transferencia', 2,    1, 2),
(350000.00, 'IVA importación telas primer trimestre',  '2025-02-01', 'Transferencia', NULL, 2, 3),
( 60000.00, 'Cajas y bolsas para empaque febrero',     '2025-02-10', 'Efectivo',      NULL, 4, 2),
( 45000.00, 'Papelería y útiles de oficina',           '2025-03-01', 'Efectivo',      NULL, 5, 3);

-- ----- Movimientos_Financieros (15 — uno por cada compra, venta y gasto) -----
-- Tipo: 1=Venta(Ingreso), 2=Compra(Egreso), 3=Gasto Adicional(Egreso)
-- Compras (egreso)
INSERT INTO Movimientos_Financieros (FECHA_MOVIMIENTO, CONCEPTO, MONTO, METODO_PAGO, TIPO_MOVIMIENTO_ID, USUARIO_ID, VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) VALUES
('2025-01-15', 'Compra camisetas - TextilColombia',      1100000.00, 'Transferencia', 2, 2, NULL, 1,    NULL),
('2025-01-20', 'Compra jeans - CalzaExpress',             2100000.00, 'Transferencia', 2, 2, NULL, 2,    NULL),
('2025-02-05', 'Compra tenis - ModaUrbana',               1625000.00, 'Efectivo',      2, 2, NULL, 3,    NULL),
('2025-02-18', 'Compra cinturones - AccesoriosPlus',       750000.00, 'Transferencia', 2, 3, NULL, 4,    NULL),
('2025-03-01', 'Compra hoodies - ImportaTextil',          1750000.00, 'Transferencia', 2, 3, NULL, 5,    NULL);
-- Ventas (ingreso)
INSERT INTO Movimientos_Financieros (FECHA_MOVIMIENTO, CONCEPTO, MONTO, METODO_PAGO, TIPO_MOVIMIENTO_ID, USUARIO_ID, VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) VALUES
('2025-02-10', 'Venta camisetas - María Fernanda Ruiz',   225000.00, 'Efectivo',      1, 4, 1,    NULL, NULL),
('2025-02-14', 'Venta jeans - Juan David Ospina',          178000.00, 'Transferencia', 1, 4, 2,    NULL, NULL),
('2025-02-20', 'Venta tenis - Camila Andrea Torres',       270000.00, 'Efectivo',      1, 5, 3,    NULL, NULL),
('2025-03-01', 'Venta tenis - Daniel Felipe Vargas',       135000.00, 'Transferencia', 1, 5, 4,    NULL, NULL),
('2025-03-05', 'Venta hoodies - Sofía Alejandra Peña',     156000.00, 'Efectivo',      1, 4, 5,    NULL, NULL);
-- Gastos adicionales (egreso)
INSERT INTO Movimientos_Financieros (FECHA_MOVIMIENTO, CONCEPTO, MONTO, METODO_PAGO, TIPO_MOVIMIENTO_ID, USUARIO_ID, VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) VALUES
('2025-01-16', 'Flete envío TextilColombia Bogotá',        120000.00, 'Efectivo',      3, 2, NULL, NULL, 1),
('2025-01-21', 'Flete envío CalzaExpress Medellín',         85000.00, 'Transferencia', 3, 2, NULL, NULL, 2),
('2025-02-01', 'IVA importación telas primer trimestre',   350000.00, 'Transferencia', 3, 3, NULL, NULL, 3),
('2025-02-10', 'Cajas y bolsas para empaque febrero',       60000.00, 'Efectivo',      3, 2, NULL, NULL, 4),
('2025-03-01', 'Papelería y útiles de oficina',             45000.00, 'Efectivo',      3, 3, NULL, NULL, 5);
