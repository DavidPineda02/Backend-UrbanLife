# 🏙️ UrbanLife — Backend

API REST del sistema de gestión empresarial UrbanLife. Construida en Java 17 puro sin frameworks, con servidor HTTP nativo del JDK, MySQL y autenticación JWT.

---

## 📋 Tabla de Contenidos

- [🌟 Alcance del Proyecto](#-alcance-del-proyecto)
- [🚀 Stack Tecnológico](#-stack-tecnológico)
- [🏗️ Arquitectura](#️-arquitectura)
- [📋 Requisitos Previos](#-requisitos-previos)
- [🛠️ Instalación y Configuración](#️-instalación-y-configuración)
  - [1. Clonar el Repositorio](#1-clonar-el-repositorio)
  - [2. Configurar la Base de Datos](#2-configurar-la-base-de-datos)
  - [3. Configurar Variables de Entorno](#3-configurar-variables-de-entorno)
  - [4. Ejecutar el Servidor](#4-ejecutar-el-servidor)
- [📂 Estructura del Proyecto](#-estructura-del-proyecto)
- [🗄️ Base de Datos](#️-base-de-datos)
  - [Diagrama de Tablas](#diagrama-de-tablas)
  - [Descripción de Tablas](#descripción-de-tablas)
- [📡 API Reference](#-api-reference)
  - [Autenticación](#autenticación)
  - [Usuarios](#usuarios)
  - [Categorías](#categorías)
  - [Productos](#productos)
  - [Clientes](#clientes)
  - [Proveedores](#proveedores)
  - [Ventas](#ventas)
  - [Compras](#compras)
  - [Gastos Adicionales](#gastos-adicionales)
  - [Movimientos Financieros](#movimientos-financieros)
  - [Perfil de Usuario](#perfil-de-usuario)
  - [Dashboard](#dashboard)
- [🔐 Sistema de Roles y Permisos](#-sistema-de-roles-y-permisos)
- [🔒 Autenticación y Seguridad](#-autenticación-y-seguridad)
- [🌱 Seeders](#-seeders)
- [🔧 Dependencias](#-dependencias)
- [🎯 Decisiones de Diseño](#-decisiones-de-diseño)
- [🙌 Contribuidores](#-contribuidores)
- [📝 Licencia](#-licencia)
- [📧 Contacto](#-contacto)

---

## 🌟 Alcance del Proyecto

UrbanLife Backend es la capa de servidor y datos del sistema de gestión empresarial. Cubre los siguientes módulos:

- **🔐 Autenticación:** Login tradicional con JWT, inicio de sesión con Google OAuth 2.0 y recuperación de contraseña por correo electrónico.
- **👥 Usuarios y Roles:** Gestión de cuentas con tres niveles de acceso: `SUPER_ADMIN`, `ADMIN` y `EMPLEADO`.
- **📦 Inventario:** CRUD completo de productos y categorías con soft delete, control de stock y soporte para imágenes.
- **🛒 Ventas:** Registro de ventas con múltiples productos en una transacción atómica que descuenta stock y genera movimiento financiero.
- **🏪 Compras:** Registro de compras a proveedores que aumenta stock y recalcula el costo promedio ponderado automáticamente.
- **💸 Gastos Adicionales:** Control de egresos operativos (arriendos, servicios, etc.) con generación automática de movimiento financiero.
- **📒 Contabilidad:** Libro mayor unificado de movimientos financieros (ingresos y egresos) generado automáticamente por cada transacción.
- **📊 Dashboard:** 5 endpoints de analítica con resúmenes diarios/mensuales, gráficos de ventas semanales, stock por categoría y top de productos rentables.
- **📋 Directorio:** Gestión de clientes y proveedores con sus datos de contacto.
- **👤 Perfil:** Administración de correos y teléfonos adicionales por usuario.

---

## 🚀 Stack Tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Maven | 3.x | Gestión de dependencias y build |
| `com.sun.net.httpserver` | JDK built-in | Servidor HTTP (sin Spring) |
| MySQL | 8.0.33 | Base de datos relacional |
| Gson | 2.11.0 | Serialización/deserialización JSON |
| JJWT | 0.12.6 | JSON Web Tokens |
| jBCrypt | 0.4 | Hash de contraseñas |
| JavaMail | 1.6.2 | Envío de correos (recuperación de contraseña) |
| Google OAuth 2.0 | — | Inicio de sesión con Google |
| dotenv-java | 3.0.0 | Variables de entorno desde `.env` |

---

## 🏗️ Arquitectura

El backend sigue el patrón **MVC en 4 capas**. No usa Spring ni ningún framework; el servidor HTTP es implementado directamente con las APIs del JDK.

```
Request HTTP
     │
     ▼
AuthMiddleware          ← Valida JWT y extrae userId/rol
     │
     ▼
Controller              ← Parsea el request, llama al service, envía respuesta
     │
     ▼
Service                 ← Validaciones de negocio, cálculos, orquestación
     │
     ▼
DAO                     ← Queries SQL con JDBC
     │
     ▼
MySQL 8.0
```

**Flujo de respuesta estándar:**
- El Service retorna un `JsonObject` con un campo `"status"` (código HTTP).
- El Controller extrae ese campo, lo elimina del objeto y lo usa para la respuesta HTTP.
- Las respuestas siempre tienen la forma: `{ "success": boolean, "message": string, "data": ... }`

---

## 📋 Requisitos Previos

- **Java 17** o superior
- **Apache Maven 3.x**
- **MySQL 8.0**
- **Git**

Verificar instalaciones:

```bash
java --version
mvn --version
mysql --version
```

---

## 🛠️ Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/Backend-UrbanLife.git
cd Backend-UrbanLife
```

### 2. Configurar la Base de Datos

Inicia sesión en MySQL y ejecuta el script del esquema:

```bash
mysql -u root -p < src/main/java/com/backend/db/UrbanLife.sql
```

Esto crea la base de datos `UrbanLife` con las 20 tablas necesarias. Los datos iniciales (roles, permisos, tipos de movimiento, super admin) se insertan automáticamente al iniciar el servidor por primera vez mediante los Seeders.

### 3. Configurar Variables de Entorno

Crea el archivo `.env` en la raíz del proyecto:

```env
# Base de Datos
DB_URL=jdbc:mysql://localhost:3306/UrbanLife
DB_USER=tu_usuario_mysql
DB_PASSWD=tu_contraseña_mysql

# JWT — cadena larga y aleatoria
JWT_SECRET=UrbanLife_SecretKey_2024_Backend!

# Google OAuth 2.0 — obtén tu Client ID en Google Cloud Console
GOOGLE_CLIENT_ID=tu_google_client_id.apps.googleusercontent.com

# Email para recuperación de contraseña (Gmail con contraseña de aplicación)
EMAIL_USER=tu_correo@gmail.com
EMAIL_PASS=tu_contraseña_de_aplicacion
```

> **⚠️ Nota sobre `EMAIL_PASS`:** Genera una [contraseña de aplicación de Google](https://myaccount.google.com/apppasswords). No uses tu contraseña de Gmail directamente.

> **⚠️ Importante:** Agrega `.env` a tu `.gitignore` para no exponer credenciales.

### 4. Ejecutar el Servidor

```bash
mvn compile exec:java
```

El servidor arrancará en `http://localhost:8080`. Verás en consola:

```
Iniciando UrbanLife Backend...

Ejecutando seeders...
Seeders finalizados.

Servidor iniciado en el puerto 8080
```

Para generar el JAR ejecutable:

```bash
mvn package
java -jar target/urbanlife-1.0-SNAPSHOT.jar
```

---

## 📂 Estructura del Proyecto

```
Backend-UrbanLife/
├── .env                                        # Variables de entorno (NO subir a git)
├── pom.xml                                     # Dependencias y configuración Maven
├── uploads/                                    # Imágenes de productos subidas al servidor
└── src/main/java/com/backend/
    │
    ├── Main.java                               # Punto de entrada — ejecuta seeders e inicia servidor
    │
    ├── config/
    │   └── dbConnection.java                   # Conexión JDBC a MySQL (lee .env)
    │
    ├── server/
    │   ├── serverConnection.java               # Inicia el servidor HTTP en puerto 8080
    │   ├── StaticFileHandler.java              # Sirve archivos estáticos (/uploads)
    │   └── http/
    │       ├── ApiRequest.java                 # Wrapper de HttpExchange para leer body/params
    │       └── ApiResponse.java                # Wrapper para enviar JSON con código HTTP
    │
    ├── routes/
    │   ├── Routes.java                         # Registro de todos los endpoints (50+)
    │   └── Router.java                         # Dispatcher: ruta → método del controller
    │
    ├── middlewares/
    │   └── AuthMiddleware.java                 # Valida JWT, extrae userId, controla roles
    │
    ├── controllers/                            # 16 controladores — manejan HTTP
    │   ├── AuthController.java
    │   ├── GoogleAuthController.java
    │   ├── PasswordResetController.java
    │   ├── UserController.java
    │   ├── CategoriaController.java
    │   ├── ProductoController.java
    │   ├── ClienteController.java
    │   ├── ProveedorController.java
    │   ├── VentaController.java
    │   ├── CompraController.java
    │   ├── GastoAdicionalController.java
    │   ├── CorreoUsuarioController.java
    │   ├── TelefonoUsuarioController.java
    │   ├── ImagenProductoController.java
    │   ├── MovimientoFinancieroController.java
    │   └── DashboardController.java
    │
    ├── services/                               # 17 servicios — lógica de negocio y validaciones
    │   ├── AuthService.java
    │   ├── GoogleAuthService.java
    │   ├── PasswordResetService.java
    │   ├── EmailService.java                   # Envío de correos con JavaMail
    │   ├── UserService.java
    │   ├── CategoriaService.java
    │   ├── ProductoService.java
    │   ├── ClienteService.java
    │   ├── ProveedorService.java
    │   ├── VentaService.java
    │   ├── CompraService.java
    │   ├── GastoAdicionalService.java
    │   ├── CorreoUsuarioService.java
    │   ├── TelefonoUsuarioService.java
    │   ├── ImagenProductoService.java
    │   ├── MovimientoFinancieroService.java
    │   └── DashboardService.java
    │
    ├── dao/                                    # 21 DAOs — acceso directo a MySQL via JDBC
    │   ├── UsuarioDAO.java
    │   ├── ProductoDAO.java
    │   ├── CategoriaDAO.java
    │   ├── ClienteDAO.java
    │   ├── ProveedorDAO.java
    │   ├── VentaDAO.java
    │   ├── CompraDAO.java
    │   ├── DetalleVentaDAO.java
    │   ├── DetalleCompraDAO.java
    │   ├── GastoAdicionalDAO.java
    │   ├── CorreoUsuarioDAO.java
    │   ├── TelefonoUsuarioDAO.java
    │   ├── ImagenProductoDAO.java
    │   ├── MovimientoFinancieroDAO.java
    │   ├── DashboardDAO.java
    │   ├── UsuarioRolDAO.java
    │   ├── RolDAO.java
    │   ├── RolPermisoDAO.java
    │   ├── PermisoDAO.java
    │   ├── TipoMovimientoDAO.java
    │   └── TokenRecuperacionDAO.java
    │
    ├── models/                                 # 20 entidades del dominio
    │   ├── Usuario.java
    │   ├── Producto.java
    │   ├── Categoria.java
    │   ├── Cliente.java
    │   ├── Proveedor.java
    │   ├── Venta.java
    │   ├── Compra.java
    │   ├── DetalleVenta.java
    │   ├── DetalleCompra.java
    │   ├── GastoAdicional.java
    │   ├── CorreoUsuario.java
    │   ├── TelefonoUsuario.java
    │   ├── ImagenProducto.java
    │   ├── MovimientoFinanciero.java
    │   ├── Rol.java
    │   ├── RolPermiso.java
    │   ├── UsuarioRol.java
    │   ├── Permiso.java
    │   ├── TipoMovimiento.java
    │   └── TokenRecuperacion.java
    │
    ├── dto/                                    # Data Transfer Objects
    │   ├── CreateUserRequest.java
    │   ├── LoginRequest.java
    │   ├── LoginResponse.java
    │   └── UpdateUserRequest.java
    │
    ├── helpers/
    │   ├── JwtHelper.java                      # Generación y validación de tokens JWT
    │   ├── PasswordHelper.java                 # BCrypt hash y verificación
    │   ├── ValidationHelper.java               # Regex centralizadas (EMAIL, NIT, TELEFONO...)
    │   └── JsonHelper.java                     # Utilidades de serialización Gson
    │
    ├── seeders/                                # Datos iniciales — se ejecutan al arrancar
    │   ├── SeedRoles.java                      # Roles: SUPER_ADMIN, ADMIN, EMPLEADO
    │   ├── SeedPermisos.java                   # Permisos del sistema
    │   ├── SeedRolPermisos.java                # Asignación permisos → roles
    │   ├── SeedTipoMovimientos.java            # Tipos: Venta, Compra, Gasto Adicional
    │   ├── SeedSuperAdmin.java                 # Cuenta de super administrador
    │   ├── SeedClienteDefault.java             # Cliente genérico "Consumidor Final"
    │   └── SeedDemoData.java                   # Datos de ejemplo para desarrollo
    │
    └── db/
        └── UrbanLife.sql                       # Esquema completo de la base de datos
```

---

## 🗄️ Base de Datos

### Diagrama de Tablas

```
Usuarios ──────────────── Usuarios_Roles ──── Roles ──── Roles_Permisos ──── Permisos
    │
    ├── Correos_Usuarios
    ├── Telefonos_Usuarios
    ├── Tokens_Recuperacion
    │
    ├── Ventas ─────────────── Detalles_Ventas ──── Productos ──── Categorias
    │       └──────────────── Movimientos_Financieros ──── Tipos_Movimientos
    │
    ├── Compras ────────────── Detalles_Compras ──── Productos
    │       └──────────────── Movimientos_Financieros
    │
    └── Gastos_Adicionales ─── Movimientos_Financieros

Clientes ──── Ventas
Proveedores ── Compras
Productos ───── Imagenes_Productos
```

### Descripción de Tablas

**Tablas independientes:**

| Tabla | Descripción |
|---|---|
| `Usuarios` | Cuentas del sistema con soporte BCrypt y Google OAuth (`GOOGLE_ID`) |
| `Roles` | `SUPER_ADMIN`, `ADMIN`, `EMPLEADO` |
| `Permisos` | Permisos granulares del sistema |
| `Categorias` | Categorías de productos (soft delete con `ESTADO`) |
| `Tipos_Movimientos` | `Venta` (Ingreso), `Compra` (Egreso), `Gasto Adicional` (Egreso) |
| `Clientes` | Directorio de clientes (no tienen acceso al sistema) |
| `Proveedores` | Directorio de proveedores |

**Tablas relacionales:**

| Tabla | Descripción |
|---|---|
| `Usuarios_Roles` | M:N entre usuarios y roles |
| `Roles_Permisos` | M:N entre roles y permisos |
| `Correos_Usuarios` | Correos adicionales por perfil de usuario |
| `Telefonos_Usuarios` | Teléfonos adicionales por perfil de usuario |

**Tablas de negocio:**

| Tabla | Descripción |
|---|---|
| `Productos` | Inventario: `PRECIO_VENTA`, `COSTO_PROMEDIO`, `STOCK`, `ESTADO` |
| `Imagenes_Productos` | Rutas de imágenes por producto |
| `Ventas` | Encabezado de venta: fecha, total, método de pago, cliente |
| `Detalles_Ventas` | Líneas de cada venta: cantidad, precio unitario, subtotal |
| `Compras` | Encabezado de compra: fecha, total, método de pago, proveedor |
| `Detalles_Compras` | Líneas de cada compra: cantidad, costo unitario, subtotal |
| `Gastos_Adicionales` | Egresos operativos: monto, descripción, fecha, método de pago |
| `Movimientos_Financieros` | Libro mayor unificado — generado automáticamente por las transacciones |
| `Tokens_Recuperacion` | Tokens temporales (1 hora) para recuperación de contraseña |

---

## 📡 API Reference

**URL base:** `http://localhost:8080/api`

**Autenticación requerida:** Header `Authorization: Bearer <token_jwt>` en todos los endpoints protegidos.

**Formato de respuesta estándar:**
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": { ... }
}
```

---

### Autenticación

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/auth/login` | Login con email y contraseña | No |
| `POST` | `/auth/register` | Registro de usuario (rol EMPLEADO) | No |
| `POST` | `/auth/google` | Login con cuenta de Google | No |
| `GET` | `/auth/me` | Datos del usuario autenticado | Sí |
| `POST` | `/auth/forgot-password` | Solicitar correo de recuperación | No |
| `GET` | `/auth/reset-password/validate` | Validar token de recuperación | No |
| `POST` | `/auth/reset-password` | Cambiar contraseña con token | No |

**POST `/auth/login`**
```json
// Request
{ "correo": "admin@empresa.com", "contrasena": "MiPassword123" }

// Response 200
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "data": { "idUsuario": 1, "nombre": "Juan", "apellido": "Pérez", "rol": "ADMIN" }
}
```

---

### Usuarios

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/users` | Listar todos los usuarios | SUPER_ADMIN, ADMIN |
| `GET` | `/users/id?id={id}` | Obtener usuario por ID | SUPER_ADMIN, ADMIN, EMPLEADO (propio) |
| `PUT` | `/users/id?id={id}` | Actualizar usuario | SUPER_ADMIN, ADMIN |
| `PATCH` | `/users/id?id={id}` | Activar / desactivar usuario | SUPER_ADMIN, ADMIN |

---

### Categorías

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/categorias` | Listar categorías | Todos |
| `POST` | `/categorias` | Crear categoría | SUPER_ADMIN, ADMIN |
| `GET` | `/categorias/id?id={id}` | Obtener por ID | Todos |
| `PUT` | `/categorias/id?id={id}` | Actualizar | SUPER_ADMIN, ADMIN |
| `PATCH` | `/categorias/id?id={id}` | Toggle activo/inactivo | SUPER_ADMIN, ADMIN |

**POST `/categorias`**
```json
{ "nombre": "Ropa", "descripcion": "Prendas de vestir" }
```

---

### Productos

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/productos` | Listar productos | Todos |
| `POST` | `/productos` | Crear producto | SUPER_ADMIN, ADMIN |
| `GET` | `/productos/id?id={id}` | Obtener por ID | Todos |
| `PUT` | `/productos/id?id={id}` | Actualizar producto | SUPER_ADMIN, ADMIN |
| `PATCH` | `/productos/id?id={id}` | Toggle activo/inactivo | SUPER_ADMIN, ADMIN |
| `POST` | `/productos/imagen` | Subir imagen | SUPER_ADMIN, ADMIN |
| `GET` | `/productos/imagen?id={id}` | Obtener imágenes del producto | Todos |
| `DELETE` | `/productos/imagen?id={id}` | Eliminar imagen | SUPER_ADMIN, ADMIN |

**POST `/productos`**
```json
{
  "nombre": "Camiseta Básica",
  "descripcion": "Algodón 100%",
  "precioVenta": 45000,
  "costoPromedio": 25000,
  "stock": 50,
  "categoriaId": 1
}
```

> `stock` es opcional al crear — por defecto es `0` en la base de datos.

---

### Clientes

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/clientes` | Listar clientes | Todos |
| `POST` | `/clientes` | Crear cliente | Todos |
| `GET` | `/clientes/id?id={id}` | Obtener por ID | Todos |
| `PUT` | `/clientes/id?id={id}` | Actualizar cliente | SUPER_ADMIN, ADMIN |
| `PATCH` | `/clientes/id?id={id}` | Toggle activo/inactivo | SUPER_ADMIN, ADMIN |

**POST `/clientes`**
```json
{
  "nombre": "Ana García",
  "documento": 1234567890,
  "correo": "ana@email.com",
  "telefono": "3001234567",
  "direccion": "Cra 45 #32-10",
  "ciudad": "Bogotá"
}
```

> Validaciones: cédula colombiana 6–10 dígitos, teléfono 7–10 dígitos (solo números).

---

### Proveedores

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/proveedores` | Listar proveedores | Todos |
| `POST` | `/proveedores` | Crear proveedor | SUPER_ADMIN, ADMIN |
| `GET` | `/proveedores/id?id={id}` | Obtener por ID | Todos |
| `PUT` | `/proveedores/id?id={id}` | Actualizar proveedor | SUPER_ADMIN, ADMIN |
| `PATCH` | `/proveedores/id?id={id}` | Toggle activo/inactivo | SUPER_ADMIN, ADMIN |

**POST `/proveedores`**
```json
{
  "nombre": "Textiles S.A.",
  "razonSocial": "Textiles y Confecciones S.A.",
  "nit": "900123456-1",
  "correo": "ventas@textiles.com",
  "telefono": "6011234567",
  "direccion": "Zona Industrial Cll 80",
  "ciudad": "Medellín"
}
```

---

### Ventas

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/ventas` | Listar ventas | Todos |
| `POST` | `/ventas` | Registrar venta | Todos |
| `GET` | `/ventas/id?id={id}` | Obtener venta con detalles | Todos |

**POST `/ventas`**
```json
{
  "fechaVenta": "2026-03-29",
  "metodoPago": "Efectivo",
  "clienteId": 1,
  "items": [
    { "productoId": 3, "cantidad": 2, "precioUnitario": 45000 },
    { "productoId": 7, "cantidad": 1, "precioUnitario": 80000 }
  ]
}
```

> **🔒 Seguridad:** `precioUnitario` es ignorado — el backend siempre lee el precio de la BD.
> **📦 Stock:** Se valida que la suma total de un mismo producto en todas las filas no exceda el stock disponible.
> **🔒 Inmutabilidad:** Las ventas no tienen UPDATE ni DELETE para preservar la integridad contable.

**Transacción atómica al crear una venta:**
1. `INSERT` en `Ventas`
2. `INSERT` en `Detalles_Ventas` por cada ítem
3. `UPDATE Productos SET STOCK = STOCK - cantidad`
4. `INSERT` en `Movimientos_Financieros` (tipo 1 — Ingreso)

---

### Compras

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/compras` | Listar compras | Todos |
| `POST` | `/compras` | Registrar compra | SUPER_ADMIN, ADMIN |
| `GET` | `/compras/id?id={id}` | Obtener compra con detalles | Todos |

**POST `/compras`**
```json
{
  "fechaCompra": "2026-03-29",
  "metodoPago": "Transferencia",
  "proveedorId": 2,
  "items": [
    { "productoId": 3, "cantidad": 100, "costoUnitario": 22000 }
  ]
}
```

**Transacción atómica al crear una compra:**
1. `INSERT` en `Compras`
2. `INSERT` en `Detalles_Compras` por cada ítem
3. `UPDATE Productos SET STOCK = STOCK + cantidad` + recálculo del **costo promedio ponderado**
4. `INSERT` en `Movimientos_Financieros` (tipo 2 — Egreso)

**Fórmula del costo promedio ponderado:**
```
nuevo_costo = (stock_actual × costo_actual + cantidad_nueva × costo_unitario)
              ────────────────────────────────────────────────────────────────
                              stock_actual + cantidad_nueva
```

---

### Gastos Adicionales

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/gastos` | Listar gastos | Todos |
| `POST` | `/gastos` | Registrar gasto | SUPER_ADMIN, ADMIN |
| `GET` | `/gastos/id?id={id}` | Obtener gasto por ID | Todos |

**POST `/gastos`**
```json
{
  "monto": 250000,
  "descripcion": "Pago de arriendo local",
  "fechaRegistro": "2026-03-29",
  "metodoPago": "Transferencia"
}
```

---

### Movimientos Financieros

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| `GET` | `/movimientos-financieros` | Listar todos los movimientos | Todos |

Solo lectura. Se generan automáticamente al registrar ventas, compras o gastos.

| `tipo_movimiento_id` | Tipo | Naturaleza |
|---|---|---|
| `1` | Venta | Ingreso |
| `2` | Compra | Egreso |
| `3` | Gasto Adicional | Egreso |

---

### Perfil de Usuario

**Correos adicionales:**

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/correos-usuario` | Correos del usuario autenticado |
| `POST` | `/correos-usuario` | Agregar correo adicional |
| `DELETE` | `/correos-usuario?id={id}` | Eliminar correo adicional |

**Teléfonos adicionales:**

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/telefonos-usuario` | Teléfonos del usuario autenticado |
| `POST` | `/telefonos-usuario` | Agregar teléfono |
| `DELETE` | `/telefonos-usuario?id={id}` | Eliminar teléfono |

---

### Dashboard

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/dashboard/resumen` | Tarjetas: ventas/compras/gastos/ganancia del día y del mes |
| `GET` | `/dashboard/ventas-semanales` | Ventas de los últimos 7 días (gráfico de barras) |
| `GET` | `/dashboard/resumen-semanal` | Ingresos, egresos y ganancia agrupados por día |
| `GET` | `/dashboard/stock-categorias` | Stock total agrupado por categoría |
| `GET` | `/dashboard/productos-rentables` | Top 10 productos por margen de ganancia |

---

## 🔐 Sistema de Roles y Permisos

RBAC (Role-Based Access Control) con tres roles:

| Rol | Descripción |
|---|---|
| `SUPER_ADMIN` | Acceso total sin restricciones — asignado por seeder |
| `ADMIN` | Dueño del negocio — gestión completa |
| `EMPLEADO` | Operador — acceso limitado a operaciones del día a día |

**Comparativa ADMIN vs EMPLEADO:**

| Acción | ADMIN | EMPLEADO |
|---|---|---|
| Ver ventas / compras / gastos | ✅ | ✅ |
| Registrar ventas | ✅ | ✅ |
| Registrar compras | ✅ | ❌ |
| Registrar gastos adicionales | ✅ | ❌ |
| Crear / editar productos | ✅ | ❌ |
| Crear / editar categorías | ✅ | ❌ |
| Gestionar usuarios | ✅ | ❌ |
| Ver dashboard | ✅ | ✅ |

El `AuthMiddleware` valida el JWT en cada request y verifica que el rol del usuario tenga permiso para el endpoint solicitado. El `userId` se extrae del token y se pasa al controller via `exchange.setAttribute("userId", ...)`.

---

## 🔒 Autenticación y Seguridad

### JWT

- Se genera al hacer login y tiene expiración de 24 horas.
- El header esperado es: `Authorization: Bearer <token>`
- El `JwtHelper` firma el token con `HMAC-SHA256` usando el `JWT_SECRET` del `.env`.

### Contraseñas

- Hash con **BCrypt** (jBCrypt) antes de almacenar en BD.
- Requisitos: 8–20 caracteres, al menos una mayúscula, una minúscula y un dígito.
- Los usuarios de Google OAuth tienen `CONTRASENA = NULL` en la BD.

### Google OAuth 2.0

- El frontend obtiene el `id_token` con Google Identity Services.
- El backend verifica el `id_token` contra los servidores de Google.
- Si el correo ya existe en el sistema, se vincula el `GOOGLE_ID` al usuario existente.

### Recuperación de Contraseña

1. `POST /auth/forgot-password` — genera token único con expiración de 1 hora y envía correo.
2. `GET /auth/reset-password/validate?token=...` — verifica que el token exista, no esté usado y no haya expirado.
3. `POST /auth/reset-password` — cambia la contraseña y marca el token como `USADO = true`.

---

## 🌱 Seeders

Los seeders se ejecutan automáticamente al iniciar el servidor. Son idempotentes: verifican si los datos ya existen antes de insertar.

| Seeder | Datos que inserta |
|---|---|
| `SeedRoles` | Roles: `SUPER_ADMIN`, `ADMIN`, `EMPLEADO` |
| `SeedPermisos` | Permisos del sistema |
| `SeedRolPermisos` | Asignación de permisos a cada rol |
| `SeedTipoMovimientos` | Tipos: Venta (Ingreso), Compra (Egreso), Gasto Adicional (Egreso) |
| `SeedSuperAdmin` | Cuenta inicial del desarrollador |
| `SeedClienteDefault` | Cliente "Consumidor Final" para ventas sin cliente identificado |
| `SeedDemoData` | Categorías, productos, clientes y proveedores de ejemplo (solo en desarrollo) |

---

## 🔧 Dependencias

Definidas en `pom.xml`:

| Dependencia | Versión | Descripción |
|---|---|---|
| `mysql:mysql-connector-java` | `8.0.33` | Conector JDBC para MySQL |
| `com.google.code.gson:gson` | `2.11.0` | Serialización/deserialización JSON |
| `io.github.cdimascio:dotenv-java` | `3.0.0` | Carga de variables desde archivo `.env` |
| `org.mindrot:jbcrypt` | `0.4` | Hash de contraseñas con BCrypt |
| `com.sun.mail:javax.mail` | `1.6.2` | Envío de correos electrónicos (SMTP) |
| `io.jsonwebtoken:jjwt-api` | `0.12.6` | API de JSON Web Tokens |
| `io.jsonwebtoken:jjwt-impl` | `0.12.6` | Implementación de JJWT (runtime) |
| `io.jsonwebtoken:jjwt-gson` | `0.12.6` | Integración JJWT con Gson (runtime) |

---

## 🎯 Decisiones de Diseño

**¿Por qué sin Spring?**
Se usa `com.sun.net.httpserver` para mantener control total sobre el servidor, evitar "magia" de frameworks y hacer explícito cada componente. Facilita entender el ciclo de vida de cada request.

**¿Por qué Ventas/Compras/Gastos son inmutables?**
La integridad contable requiere que los registros históricos no se modifiquen. Si hay un error, se registra una corrección como una nueva transacción (principio de *append-only ledger*).

**¿Por qué el precio de venta se lee de la BD?**
Previene que el frontend envíe precios manipulados. El backend ignora el `precioUnitario` del request y usa siempre el valor de `PRECIO_VENTA` del producto en la BD.

**¿Por qué las fechas son String y no LocalDate?**
`Gson` serializa `LocalDate` como `{"year":2026,"monthValue":3,"dayOfMonth":29}` en lugar de `"2026-03-29"`. Se usa `String` para mantener el JSON legible y compatible con el frontend sin configuración extra.

**Soft Delete**
Clientes, proveedores, productos, categorías y usuarios se desactivan con `PATCH` (campo `ESTADO = false`) en lugar de eliminarse físicamente, preservando la integridad referencial y el historial.

**Validación de stock acumulado en ventas**
Si un mismo producto aparece en varias filas de una venta, el backend agrupa las cantidades por `productoId` y valida la suma total contra el stock disponible antes de procesar cualquier ítem.

---

## 🙌 Contribuidores

¡Gracias a quien hizo posible este proyecto! 👏🎉

- **Yedher David Pineda** — [GitHub](https://github.com/DavidPineda02) 🚀

---

## 📝 Licencia

Este proyecto está bajo una Licencia de Software Propietario. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

## 📧 Contacto

Si tienes preguntas, sugerencias o encontraste un bug, no dudes en contactarme:

- **Yedher David Pineda** — daxpa.02@gmail.com

---

¡Disfruta el proyecto! 😄