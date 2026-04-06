# Flujo End-to-End — ¿Qué pasa desde que el Frontend envía datos hasta que recibe respuesta?

Este documento explica el recorrido completo de un request HTTP desde que sale del navegador hasta que el backend responde. Se usa **registrar una venta** (`POST /api/ventas`) como ejemplo concreto porque involucra todas las capas y varias validaciones.

---

## Tabla de contenidos

1. [El Frontend envía el request](#1-el-frontend-envía-el-request)
2. [El servidor recibe la conexión](#2-el-servidor-recibe-la-conexión)
3. [Router — ¿A quién le pertenece esta ruta?](#3-router--a-quién-le-pertenece-esta-ruta)
4. [AuthMiddleware — ¿Quién está haciendo la petición?](#4-authmiddleware--quién-está-haciendo-la-petición)
5. [Controller — Coordinador de la operación](#5-controller--coordinador-de-la-operación)
6. [Service — Validaciones y lógica de negocio](#6-service--validaciones-y-lógica-de-negocio)
7. [DAO — Ejecución del SQL](#7-dao--ejecución-del-sql)
8. [La respuesta sube de vuelta](#8-la-respuesta-sube-de-vuelta)
9. [Diagrama completo del flujo](#9-diagrama-completo-del-flujo)
10. [¿Qué pasa cuando algo falla?](#10-qué-pasa-cuando-algo-falla)

---

## 1. El Frontend envía el request

El frontend construye un objeto JSON con los datos de la venta y llama a `fetch` con el método `POST`:

```js
// js/api/client.js — wrapper central de fetch
const response = await fetch('http://localhost:8080/api/ventas', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9...'  // JWT desde localStorage
    },
    body: JSON.stringify({
        clienteId: 3,
        fecha: "2026-04-05",
        metodoPago: "Efectivo",
        items: [
            { productoId: 12, cantidad: 2 },
            { productoId: 7,  cantidad: 1 }
        ]
    })
});
```

**El token JWT** se adjunta automáticamente por `client.js` en cada request. El frontend lo lee de `localStorage` donde fue guardado al hacer login.

El body viaja como texto plano en formato JSON a través de la red hasta el puerto **8080** del servidor.

---

## 2. El servidor recibe la conexión

**Archivo:** `server/serverConnection.java`

`com.sun.net.httpserver.HttpServer` está escuchando constantemente en el puerto 8080. Al llegar la conexión TCP, el JDK la envuelve en un objeto `HttpExchange` y lo pasa al único contexto registrado:

```java
server.createContext("/", router);
```

Todo request, sin importar la ruta, llega primero al `Router`. Es el único punto de entrada.

---

## 3. Router — ¿A quién le pertenece esta ruta?

**Archivo:** `routes/Router.java`

El `Router` tiene internamente un mapa de rutas registradas:

```
POST /api/ventas      →  VentaController::crear
GET  /api/ventas      →  VentaController::listar
POST /api/auth/login  →  AuthController::login
...
```

Al recibir el `HttpExchange`, el Router hace tres cosas:

1. **Lee el método HTTP** del request → `POST`
2. **Lee la ruta** del request → `/api/ventas`
3. **Busca en el mapa** la combinación `POST + /api/ventas`

Si la encuentra, ejecuta el handler correspondiente. Si no existe la ruta, responde directamente con `404 Not Found`.

**Caso especial — OPTIONS (preflight CORS):**
Antes de cada POST/PUT/DELETE, el navegador envía automáticamente una petición OPTIONS para verificar que el servidor permite el request. El Router detecta el método OPTIONS y responde inmediatamente con `204 No Content` más los headers CORS, sin llegar al controller.

---

## 4. AuthMiddleware — ¿Quién está haciendo la petición?

**Archivo:** `middlewares/AuthMiddleware.java`

Antes de ejecutar el handler del controller, el Router pasa el request por el middleware de autenticación. Este middleware:

**Paso 1 — Extrae el token:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MiJ9...
```
Toma el string después de `"Bearer "`.

**Paso 2 — Valida el token con JwtHelper:**
- Verifica que la firma sea válida (usando `JWT_SECRET` del `.env`)
- Verifica que no haya expirado (los tokens duran 24 horas)

**Paso 3 — Extrae el userId y el rol:**
El payload del JWT contiene el `sub` (subject) con el `userId`. El middleware consulta el rol del usuario en la BD.

**Paso 4 — Inyecta los datos en el exchange:**
```java
exchange.setAttribute("userId", "42");       // Como String
exchange.setAttribute("rol", "ADMIN");
```
Estos atributos viajan con el request hasta el controller.

**Paso 5 — Verifica permisos:**
Compara el rol del usuario contra los roles permitidos para esa ruta. Si el rol no tiene acceso, responde `403 Forbidden` y el request **no llega al controller**.

**Si el token no existe o es inválido** → responde `401 Unauthorized` y el flujo termina aquí.

---

## 5. Controller — Coordinador de la operación

**Archivo:** `controllers/VentaController.java`

El controller tiene una sola responsabilidad: **coordinar**. No valida datos de negocio ni ejecuta SQL. Solo:

**1. Lee los datos del request:**
```java
// Leer el body JSON
JsonObject body = ApiRequest.getBody(exchange);

// Leer el userId que inyectó el AuthMiddleware
int userId = Integer.parseInt((String) exchange.getAttribute("userId"));
```

`ApiRequest.getBody()` lee el `InputStream` del `HttpExchange` y lo convierte a `JsonObject` usando **Gson**.

**2. Llama al service:**
```java
JsonObject resultado = ventaService.crear(body, userId);
```

**3. Extrae el status y envía la respuesta:**
```java
int status = resultado.get("status").getAsInt();
resultado.remove("status");  // El status no va en el body de la respuesta
ApiResponse.send(exchange, resultado.toString(), status);
```

El controller nunca sabe si la operación fue exitosa o falló — solo toma lo que el service retorna y lo envía tal cual.

---

## 6. Service — Validaciones y lógica de negocio

**Archivo:** `services/VentaService.java`

Esta es la capa más importante. Aquí se toman las decisiones. El service recibe el `JsonObject` crudo del frontend y hace lo siguiente:

### 6.1 Validación de campos requeridos

Verifica que el body traiga todos los campos necesarios y con el formato correcto:

```java
if (!body.has("clienteId") || body.get("clienteId").isJsonNull()) {
    // Retorna error 400 — el request no continúa
}
if (!body.has("items") || body.get("items").getAsJsonArray().isEmpty()) {
    // Retorna error 400
}
```

### 6.2 Validación de existencia en BD

Consulta que el cliente exista y esté activo. Si no existe, retorna `404`.

### 6.3 Validación de stock acumulado

Este es el paso más complejo. Antes de procesar cada ítem, agrupa las cantidades por producto para detectar si el mismo producto aparece en varias filas:

```java
Map<Integer, Integer> cantidadTotalPorProducto = new HashMap<>();
for (JsonElement elemento : items) {
    int productoId = elemento.getAsJsonObject().get("productoId").getAsInt();
    int cantidad   = elemento.getAsJsonObject().get("cantidad").getAsInt();
    cantidadTotalPorProducto.put(
        productoId,
        cantidadTotalPorProducto.getOrDefault(productoId, 0) + cantidad
    );
}
```

Luego valida que el stock disponible cubra el total solicitado por producto:

```java
if (producto.getStock() < cantidadTotalSolicitada) {
    // Retorna error 400 con mensaje descriptivo
}
```

### 6.4 Cálculo del total

Recorre los ítems y para cada uno **lee el precio desde la BD** (nunca desde el frontend, para evitar manipulación):

```java
double precioUnitario = producto.getPrecioVenta(); // Viene de la BD
double subtotal = precioUnitario * cantidad;
total += subtotal;
```

### 6.5 Ejecución de la transacción atómica

Si todas las validaciones pasan, ejecuta las operaciones en orden. Si cualquiera falla, ninguna se persiste:

```java
// 1. Insertar el encabezado de la venta
int ventaId = ventaDAO.crear(clienteId, fecha, metodoPago, total, userId);

// 2. Insertar cada línea de detalle + descontar stock
for (cada ítem) {
    detalleVentaDAO.crear(ventaId, productoId, cantidad, precioUnitario, subtotal);
    productoDAO.descontarStock(productoId, cantidad);
}

// 3. Registrar el movimiento financiero (tipo = 1 = Ingreso)
movimientoDAO.crear(ventaId, total, tipo_venta, userId);
```

### 6.6 Retorna el resultado al controller

```java
JsonObject respuesta = new JsonObject();
respuesta.addProperty("status", 201);
respuesta.addProperty("success", true);
respuesta.addProperty("message", "Venta registrada exitosamente");
respuesta.add("data", ventaJson);
return respuesta;
```

---

## 7. DAO — Ejecución del SQL

**Archivos:** `dao/VentaDAO.java`, `dao/DetalleVentaDAO.java`, `dao/MovimientoFinancieroDAO.java`

Cada DAO recibe los datos ya validados del service y ejecuta el SQL usando `PreparedStatement` (previene inyección SQL):

```java
// VentaDAO.crear()
String sql = "INSERT INTO Ventas (CLIENTE_ID, FECHA, METODO_PAGO, TOTAL, USUARIO_ID) " +
             "VALUES (?, ?, ?, ?, ?)";

try (Connection conexion = dbConnection.getConnection();
     PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

    ps.setInt(1, clienteId);
    ps.setString(2, fecha);
    ps.setString(3, metodoPago);
    ps.setDouble(4, total);
    ps.setInt(5, userId);
    ps.executeUpdate();

    // Retorna el ID generado por MySQL (AUTO_INCREMENT)
    ResultSet keys = ps.getGeneratedKeys();
    keys.next();
    return keys.getInt(1);  // → ventaId
}
```

El DAO no sabe nada de validaciones ni de otros DAOs. Solo ejecuta su SQL y retorna el resultado.

---

## 8. La respuesta sube de vuelta

Una vez que el DAO termina, el resultado sube por las capas en sentido inverso:

```
DAO       → retorna ventaId al Service
Service   → construye el JsonObject de respuesta con status 201
Controller → extrae el status, llama a ApiResponse.send()
ApiResponse → escribe el JSON en el OutputStream del HttpExchange
```

`ApiResponse.send()` agrega los headers CORS, el `Content-Type: application/json` y escribe la respuesta:

```json
HTTP/1.1 201 Created
Content-Type: application/json

{
    "success": true,
    "message": "Venta registrada exitosamente",
    "data": {
        "idVenta": 58,
        "fecha": "2026-04-05",
        "total": 145000.0,
        "metodoPago": "Efectivo"
    }
}
```

El frontend recibe el JSON, `client.js` lo parsea y `ventas.js` muestra la alerta de éxito.

---

## 9. Diagrama completo del flujo

```
FRONTEND (fetch POST /api/ventas)
│
│  body: { clienteId, fecha, metodoPago, items[] }
│  header: Authorization: Bearer <token>
│
▼
serverConnection.java
│  HttpServer escucha en :8080
│  Envuelve la conexión en HttpExchange
│
▼
Router.java
│  Lee método → POST
│  Lee ruta   → /api/ventas
│  Busca en el mapa de rutas
│  Encuentra → VentaController::crear
│
▼
AuthMiddleware.java
│  Extrae el token del header
│  Valida firma y expiración con JwtHelper
│  Extrae userId y rol del payload
│  Verifica que el rol tenga permiso
│  Inyecta userId y rol en el exchange
│  ─── Si falla → 401 o 403 (el flujo termina aquí)
│
▼
VentaController.java
│  Lee body con ApiRequest.getBody()   → JsonObject
│  Lee userId de exchange.getAttribute()
│  Llama a ventaService.crear(body, userId)
│
▼
VentaService.java
│  Valida campos requeridos (clienteId, fecha, items)
│  Valida que el cliente exista en BD
│  Agrupa cantidades por productoId
│  Valida stock acumulado por producto
│  Calcula total leyendo precios desde BD
│  ─── Si falla en cualquier punto → retorna error con status
│
▼
VentaDAO.java            →  INSERT INTO Ventas          → MySQL
DetalleVentaDAO.java     →  INSERT INTO Detalles_Ventas → MySQL
ProductoDAO.java         →  UPDATE stock (descuento)    → MySQL
MovimientoDAO.java       →  INSERT INTO Movimientos     → MySQL
│
▼
VentaService.java
│  Construye JsonObject { status: 201, success: true, data: {...} }
│
▼
VentaController.java
│  Extrae y elimina el campo "status"
│  Llama a ApiResponse.send(exchange, json, 201)
│
▼
ApiResponse.java
│  Agrega headers CORS y Content-Type
│  Escribe JSON en el OutputStream del HttpExchange
│
▼
FRONTEND (recibe respuesta 201)
   client.js parsea el JSON
   ventas.js muestra alerta de éxito
```

---

## 10. ¿Qué pasa cuando algo falla?

El flujo de error es igual al de éxito — siempre sube por las mismas capas. La diferencia es el código HTTP y el `success: false`.

| Dónde falla | Causa | Respuesta |
|---|---|---|
| `AuthMiddleware` | Token ausente o expirado | `401 Unauthorized` |
| `AuthMiddleware` | Rol sin permiso | `403 Forbidden` |
| `Router` | Ruta no registrada | `404 Not Found` |
| `VentaService` | Campo faltante en el body | `400 Bad Request` |
| `VentaService` | Cliente no existe | `404 Not Found` |
| `VentaService` | Stock insuficiente | `400 Bad Request` |
| `VentaDAO` | Error en MySQL | `500 Internal Server Error` |

En todos los casos el frontend recibe:

```json
{
    "success": false,
    "message": "Stock insuficiente para 'Camisa Negra'. Disponible: 3, solicitado: 5"
}
```

`client.js` detecta `success === false` o status >= 400, lanza un error que es capturado por `ventas.js`, y este muestra la alerta de error con el mensaje exacto del backend.