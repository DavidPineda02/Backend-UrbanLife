# Documentacion Paso a Paso — Backend UrbanLife (Modulo de Usuarios y Login)

Esta documentacion explica paso a paso como se construyo el backend del proyecto UrbanLife desde cero. Esta pensada para alguien que nunca ha hecho un proyecto en Java y necesita entender que se hizo, por que se hizo, y en que orden.

> **Alcance:** Solo cubre el modulo de **Usuarios** (CRUD) y **Autenticacion** (Login + JWT). Los demas modulos se documentaran por separado.

---

## Antes de empezar: ¿Que es este proyecto?

UrbanLife es un sistema de gestion contable para una tienda de ropa. El **backend** es la parte que:
- Recibe peticiones del frontend (navegador web)
- Procesa la logica (validaciones, calculos, reglas de negocio)
- Se comunica con la base de datos (MySQL)
- Devuelve respuestas en formato JSON

**Tecnologias utilizadas:**

| Tecnologia | Para que sirve |
|------------|---------------|
| **Java 17** | Lenguaje de programacion principal |
| **Maven** | Gestiona las librerias (dependencias) y compila el proyecto |
| **MySQL** | Base de datos donde se guardan usuarios, productos, ventas, etc. |
| **HttpServer (JDK)** | Servidor HTTP incluido en Java, no necesita frameworks como Spring |
| **Gson** | Convierte objetos Java a JSON y viceversa |
| **dotenv-java** | Lee variables secretas desde un archivo `.env` |
| **jBCrypt** | Encripta contrasenas de forma segura |
| **JJWT** | Genera y valida tokens JWT para la autenticacion |

**Arquitectura del proyecto (patron por capas):**

Cada peticion que llega del frontend pasa por estas capas en orden:

```
Peticion HTTP → Router → Controller → Service → DAO → Base de Datos
                                                         ↓
Respuesta JSON ← Controller ← Service ← DAO ← Resultado de la BD
```

Cada capa tiene una responsabilidad unica:
- **Router**: Decide a que Controller enviar la peticion segun la ruta y el metodo HTTP
- **Controller**: Recibe la peticion, extrae los datos y delega al Service
- **Service**: Contiene TODA la logica de validacion y reglas de negocio
- **DAO**: Es el UNICO que escribe SQL y habla con la base de datos

---

## Estructura de carpetas del proyecto

```
src/main/java/com/backend/
│
├── Main.java                          → Punto de entrada (arranca todo)
│
├── config/
│   └── dbConnection.java             → Conexion a MySQL usando .env
│
├── server/
│   ├── serverConnection.java         → Crea y arranca el servidor HTTP
│   └── http/
│       ├── ApiRequest.java           → Lee el cuerpo (body) de las peticiones
│       └── ApiResponse.java          → Envia respuestas JSON estandarizadas
│
├── routes/
│   ├── Router.java                   → Motor de ruteo (busca que handler ejecutar)
│   └── Routes.java                   → Archivo donde se registran TODAS las rutas
│
├── models/
│   └── Usuario.java                  → Clase que representa la tabla Usuarios de la BD
│
├── dao/
│   └── UsuarioDAO.java               → Consultas SQL para la tabla Usuarios (JDBC)
│
├── services/
│   ├── UserService.java              → Validaciones y logica para el CRUD de usuarios
│   └── AuthService.java              → Validaciones y logica para el login
│
├── controllers/
│   ├── UserController.java           → Recibe peticiones HTTP del CRUD de usuarios
│   └── AuthController.java           → Recibe peticiones HTTP de autenticacion
│
├── helpers/
│   ├── PasswordHelper.java           → Encripta y verifica contrasenas con BCrypt
│   ├── JwtHelper.java                → Genera y valida tokens JWT
│   └── JsonHelper.java               → Utilidad para convertir JSON a objetos Java
│
├── middlewares/
│   └── AuthMiddleware.java           → Protege rutas verificando el token JWT
│
├── seeders/                           → Datos iniciales que se insertan al arrancar
│   ├── SeedRoles.java
│   ├── SeedPermisos.java
│   ├── SeedTipoMovimientos.java
│   └── SeedTipoGasto.java
│
└── db/
    └── UrbanLife.sql                  → Script completo de la base de datos
```

---

## Paso 1: Crear el proyecto Maven y agregar dependencias

**Archivo:** `pom.xml` (en la raiz del proyecto)

Lo primero al crear un proyecto Java es configurar **Maven**. Maven es una herramienta que descarga automaticamente las librerias que necesitamos, compila el codigo y ejecuta el proyecto.

En el `pom.xml` se definen las dependencias (librerias externas):

```xml
<dependencies>
    <!-- Conectar Java con MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- Convertir objetos Java a JSON y viceversa -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.11.0</version>
    </dependency>

    <!-- Leer variables de entorno desde archivo .env -->
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>3.0.0</version>
    </dependency>

    <!-- Encriptar contrasenas de forma segura -->
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>

    <!-- Tokens JWT para autenticacion (3 artefactos necesarios) -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-gson</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

Tambien se configuro el plugin `exec-maven-plugin` para ejecutar el proyecto desde la terminal:

```bash
mvn clean compile exec:java
```

- `clean` → Borra compilaciones anteriores
- `compile` → Compila todo el codigo Java
- `exec:java` → Ejecuta la clase `Main.java`

---

## Paso 2: Variables de entorno (.env)

**Archivo:** `.env` (en la raiz del proyecto, NUNCA se sube al repositorio)

Antes de escribir codigo, necesitamos un lugar seguro para guardar credenciales:

```env
DB_URL=jdbc:mysql://localhost:3306/UrbanLife
DB_USER=root
DB_PASSWD=tu_contrasena
JWT_SECRET=una_clave_secreta_de_al_menos_32_caracteres
```

**¿Por que usamos .env en vez de escribir las credenciales directo en el codigo?**
- Si el codigo se sube a GitHub, las credenciales quedarian expuestas publicamente
- Cada desarrollador puede tener credenciales diferentes
- El `.env` se agrega al `.gitignore` para que Git lo ignore

La libreria `dotenv-java` lee este archivo y pone esas variables disponibles en el codigo.

---

## Paso 3: Conexion a la base de datos

**Archivo:** `config/dbConnection.java`

Este es el primer archivo funcional del proyecto. Sin conexion a la base de datos, no podemos hacer nada.

```java
package com.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;

public class dbConnection {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWD = dotenv.get("DB_PASSWD");

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWD == null) {
            System.err.println("ERROR: Faltan variables de entorno (DB_URL, DB_USER, DB_PASSWD)");
            return null;
        }
        return DriverManager.getConnection(URL, USER, PASSWD);
    }
}
```

**¿Que hace paso a paso?**

1. `Dotenv.load()` → Lee el archivo `.env` y carga las variables
2. `dotenv.get("DB_URL")` → Obtiene la URL de la base de datos
3. `dotenv.get("DB_USER")` → Obtiene el usuario de MySQL
4. `dotenv.get("DB_PASSWD")` → Obtiene la contrasena de MySQL
5. `getConnection()` → Crea y retorna una conexion JDBC a MySQL

**Validacion:** Si alguna variable falta (es `null`), imprime un error y retorna `null`.

**¿Como se usa desde otros archivos?**
```java
Connection conn = dbConnection.getConnection();
// Ahora puedo ejecutar SQL con esta conexion
```

---

## Paso 4: Crear la base de datos (SQL)

**Archivo:** `db/UrbanLife.sql`

Se ejecuta este script en MySQL para crear todas las tablas. Para el modulo de usuarios, las tablas relevantes son:

```sql
CREATE TABLE Usuarios (
    ID_USUARIO INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    CORREO VARCHAR(120) NOT NULL UNIQUE,
    CONTRASENA VARCHAR(255) NOT NULL,
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE Roles (
    ID_ROLES INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(80) NOT NULL UNIQUE,
    DESCRIPCION VARCHAR(200)
);

CREATE TABLE Usuario_Rol (
    ID_USUARIO_ROL INT AUTO_INCREMENT PRIMARY KEY,
    USUARIO_ID INT NOT NULL,
    ROL_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO),
    FOREIGN KEY (ROL_ID) REFERENCES Roles(ID_ROLES),
    UNIQUE (USUARIO_ID, ROL_ID)
);
```

**Conceptos clave:**

- `AUTO_INCREMENT` → MySQL genera el ID automaticamente al insertar
- `NOT NULL` → El campo es obligatorio
- `UNIQUE` → No puede haber duplicados (ej: dos usuarios con el mismo correo)
- `FOREIGN KEY` → Vincula una tabla con otra (ej: `USUARIO_ID` apunta a `Usuarios`)
- `DEFAULT TRUE` → Si no se especifica, el estado sera `true` (activo)
- `Usuario_Rol` es una **tabla intermedia** que conecta usuarios con roles. El `UNIQUE (USUARIO_ID, ROL_ID)` evita asignar el mismo rol dos veces

---

## Paso 5: Crear el servidor HTTP

**Archivo:** `server/serverConnection.java`

En vez de usar Spring Boot, usamos el servidor HTTP incluido en Java (`com.sun.net.httpserver.HttpServer`). Esto hace el proyecto mas ligero.

```java
package com.backend.server;

import com.backend.routes.Routes;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class serverConnection {

    private static HttpServer server;

    public static void startServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            Routes routes = new Routes();
            HttpHandler router = routes.configureRoutes();

            server.createContext("/", router);
            server.setExecutor(null);
            server.start();

            System.out.println("UrbanLife Backend corriendo en: http://localhost:" + port);
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }
}
```

**¿Que hace paso a paso?**

1. `HttpServer.create(new InetSocketAddress(port), 0)` → Crea un servidor en el puerto 8080
2. `new Routes()` → Crea la instancia donde se configuran todas las rutas de la API
3. `routes.configureRoutes()` → Registra las rutas y retorna el Router
4. `server.createContext("/", router)` → Todas las peticiones van al router
5. `server.setExecutor(null)` → Usa un hilo por cada peticion
6. `server.start()` → Inicia el servidor, listo para recibir peticiones

---

## Paso 6: Motor de ruteo (Router)

**Archivo:** `routes/Router.java`

Cuando el frontend envia una peticion como `GET /api/users` o `POST /api/auth/login`, el servidor necesita saber **que codigo ejecutar**. Eso es lo que hace el Router.

```java
package com.backend.routes;

import com.backend.server.http.ApiResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router implements HttpHandler {

    Map<String, Map<String, HttpHandler>> routes = new HashMap<>();

    public void addRoute(String method, String path, HttpHandler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public void get(String path, HttpHandler handler)    { addRoute("GET", path, handler); }
    public void post(String path, HttpHandler handler)   { addRoute("POST", path, handler); }
    public void put(String path, HttpHandler handler)    { addRoute("PUT", path, handler); }
    public void patch(String path, HttpHandler handler)  { addRoute("PATCH", path, handler); }
    public void delete(String path, HttpHandler handler) { addRoute("DELETE", path, handler); }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            ApiResponse.handleCors(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        Map<String, HttpHandler> methodRoutes = routes.get(method);

        if (methodRoutes != null) {
            HttpHandler handler = methodRoutes.get(path);
            if (handler != null) {
                handler.handle(exchange);
                return;
            }
        }

        ApiResponse.error(exchange, 404, "Ruta no encontrada");
    }
}
```

**¿Como funciona internamente?**

El Router guarda las rutas en un `Map` anidado (un mapa dentro de otro mapa):

```
{
  "GET": {
    "/api/users"    → ejecuta UserController.listAll(),
    "/api/users/id" → ejecuta UserController.getById(),
    "/api/auth/me"  → ejecuta AuthMiddleware → AuthController.me()
  },
  "POST": {
    "/api/users"       → ejecuta UserController.create(),
    "/api/auth/login"  → ejecuta AuthController.login()
  },
  "PUT":   { "/api/users/id" → ejecuta UserController.update() },
  "PATCH": { "/api/users/id" → ejecuta UserController.patch() }
}
```

**Cuando llega una peticion:**

1. Lee el metodo HTTP (ej: `GET`, `POST`)
2. Si es `OPTIONS` → responde con headers CORS y termina (el navegador lo necesita para peticiones cross-origin)
3. Lee la ruta (ej: `/api/users`)
4. Busca en el Map: primero por metodo, luego por ruta
5. Si encuentra un handler → lo ejecuta
6. Si no encuentra nada → responde `404 Ruta no encontrada`

**¿Que es CORS?**

Cuando el frontend (`localhost:5500`) hace peticiones al backend (`localhost:8080`), el navegador las bloquea porque son origenes diferentes. Los headers CORS le dicen al navegador: "permite peticiones desde cualquier origen". Antes de cada peticion real, el navegador envia una peticion `OPTIONS` para pedir permiso. El Router la detecta y responde automaticamente.

---

## Paso 7: Registro de rutas

**Archivo:** `routes/Routes.java`

Aqui se define que ruta ejecuta que handler. Es como un "directorio telefonico" de la API:

```java
package com.backend.routes;

import com.backend.controllers.AuthController;
import com.backend.controllers.UserController;
import com.backend.middlewares.AuthMiddleware;
import com.sun.net.httpserver.HttpHandler;

public class Routes {

    Router router = new Router();

    public HttpHandler configureRoutes() {
        AuthMiddleware auth = new AuthMiddleware();

        // ========== RUTAS DE AUTH ==========
        router.post("/api/auth/login", AuthController.login());
        router.get("/api/auth/me", auth.protect(AuthController.me()));

        // ========== RUTAS DE USUARIOS ==========
        router.get("/api/users", UserController.listAll());
        router.post("/api/users", UserController.create());
        router.get("/api/users/id", UserController.getById());
        router.put("/api/users/id", UserController.update());
        router.patch("/api/users/id", UserController.patch());

        return router;
    }
}
```

**¿Que hace cada linea?**

- `router.post("/api/auth/login", AuthController.login())` → Cuando llegue un `POST` a `/api/auth/login`, ejecuta el metodo `login()` del `AuthController`
- `router.get("/api/auth/me", auth.protect(AuthController.me()))` → **Primero** pasa por el middleware de autenticacion, y **solo si el token es valido**, ejecuta `me()`
- Las rutas de usuarios siguen el mismo patron: metodo HTTP + ruta + handler

**Nota sobre `auth.protect()`:** Esta funcion "envuelve" al handler. Antes de ejecutar el handler real, verifica el token JWT. Si no es valido, responde con error 401 y el handler nunca se ejecuta.

---

## Paso 8: Utilidades HTTP — ApiRequest y ApiResponse

Antes de crear los controllers, necesitamos herramientas para **leer peticiones** y **enviar respuestas**.

### ApiRequest.java — Leer el cuerpo de la peticion

**Archivo:** `server/http/ApiRequest.java`

Cuando el frontend envia datos en un `POST`, vienen en el **body** de la peticion. Esta clase los lee:

```java
package com.backend.server.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ApiRequest {
    public HttpExchange exchange;

    public ApiRequest(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String readBody() throws IOException {
        InputStream bodyStream = exchange.getRequestBody();
        return new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
```

**¿Como se usa?**
```java
ApiRequest request = new ApiRequest(exchange);
String body = request.readBody();
// body = {"nombre":"David","correo":"d@mail.com","contrasena":"123"}
```

### ApiResponse.java — Enviar respuestas JSON

**Archivo:** `server/http/ApiResponse.java`

Todas las respuestas del backend siguen un formato estandar:

```java
package com.backend.server.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ApiResponse {

    private static final Gson gson = new Gson();

    public static void send(HttpExchange exchange, String body, int statusCode) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, bodyBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bodyBytes);
        }
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        send(exchange, gson.toJson(data), statusCode);
    }

    public static void success(HttpExchange exchange, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("message", message);
        send(exchange, json.toString(), 200);
    }

    public static void error(HttpExchange exchange, int code, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("message", message);
        send(exchange, json.toString(), code);
    }

    public static void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(204, -1);
    }
}
```

**¿Que metodo usar y cuando?**

| Metodo | Cuando usarlo | Ejemplo de respuesta |
|--------|--------------|----------------------|
| `send(exchange, body, code)` | Cuando ya tienes un String JSON armado | Respuestas de los Services |
| `sendJson(exchange, code, data)` | Cuando tienes un objeto Java | Listas de usuarios |
| `success(exchange, message)` | Respuesta exitosa rapida | `{"success":true,"message":"OK"}` |
| `error(exchange, code, message)` | Respuesta de error | `{"success":false,"message":"Error"}` |
| `handleCors(exchange)` | Peticiones OPTIONS del navegador | Solo headers CORS + 204 |

Todos los metodos agregan automaticamente headers `Content-Type: application/json` y headers CORS.

---

## Paso 9: Helpers — Utilidades reutilizables

Antes de escribir la logica de negocio, creamos herramientas que se usaran en muchas partes del proyecto.

### PasswordHelper.java — Encriptacion de contrasenas

**Archivo:** `helpers/PasswordHelper.java`

Las contrasenas NUNCA se guardan en texto plano en la base de datos. Se guardan como un **hash** (version encriptada irreversible).

```java
package com.backend.helpers;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHelper {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
```

**¿Que hace cada metodo?**

- `hashPassword("miClave123")` → Genera un hash como `$2a$12$xRz7kL...` (60 caracteres). Este hash es lo que se guarda en la BD.
- `checkPassword("miClave123", "$2a$12$xRz7kL...")` → Compara la contrasena contra el hash. Retorna `true` si coinciden.

**¿Por que BCrypt y no SHA-256 u otro?**
- BCrypt agrega un **salt** (valor aleatorio) a cada hash, entonces la misma contrasena genera hashes diferentes cada vez
- Es **lento a proposito** (factor 12 tarda ~250ms), lo que hace practicamente imposible un ataque de fuerza bruta
- Es **irreversible**: no se puede obtener la contrasena original desde el hash

### JwtHelper.java — Tokens JWT para autenticacion

**Archivo:** `helpers/JwtHelper.java`

JWT (JSON Web Token) es un estandar para autenticacion. Cuando un usuario hace login, el backend le entrega un **token**. En cada peticion siguiente, el frontend envia ese token para demostrar que esta autenticado.

```java
package com.backend.helpers;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtHelper {

    private static final String SECRET = Dotenv.load().get("JWT_SECRET");
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24 horas en milisegundos

    public static String generateToken(int userId, String correo, String rol) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("correo", correo)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

**¿Que hace cada metodo?**

- `generateToken(1, "david@mail.com", "ADMIN")` → Crea un token como `eyJhbGciOi...` que contiene:
  - `subject` → El ID del usuario (1)
  - `correo` → "david@mail.com"
  - `rol` → "ADMIN"
  - `issuedAt` → Fecha de creacion
  - `expiration` → Fecha de expiracion (24 horas despues)
  - Firmado con la clave secreta del `.env`

- `validateToken("eyJhbGciOi...")` → Verifica que el token sea valido y no haya expirado. Si es valido, retorna los datos (`Claims`). Si no, lanza una excepcion.

**¿Como se usa el token en la practica?**
```
1. Usuario hace login → Backend genera token y lo envia
2. Frontend guarda el token (ej: localStorage)
3. Frontend envia el token en cada peticion: Header "Authorization: Bearer eyJhbG..."
4. Backend valida el token y sabe quien es el usuario sin consultar la BD
```

### JsonHelper.java — Conversion JSON

**Archivo:** `helpers/JsonHelper.java`

```java
package com.backend.helpers;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonHelper {

    private static final Gson gson = new Gson();

    public static <T> T fromJson(InputStream body, Class<T> clazz) {
        return gson.fromJson(new InputStreamReader(body, StandardCharsets.UTF_8), clazz);
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
```

- `fromJson(inputStream, Usuario.class)` → Lee JSON y lo convierte a un objeto Java
- `toJson(usuario)` → Convierte un objeto Java a String JSON

---

## Paso 10: Modelo de Usuario

**Archivo:** `models/Usuario.java`

Un **modelo** es una clase Java que representa exactamente una tabla de la base de datos. Cada campo de la clase corresponde a una columna de la tabla.

```java
package com.backend.models;

public class Usuario {
    private int idUsuario;       // ID_USUARIO INT AUTO_INCREMENT PRIMARY KEY
    private String nombre;       // NOMBRE VARCHAR(100) NOT NULL
    private String correo;       // CORREO VARCHAR(120) NOT NULL UNIQUE
    private String contrasena;   // CONTRASENA VARCHAR(255) NOT NULL
    private boolean estado;      // ESTADO BOOLEAN NOT NULL DEFAULT TRUE

    // Constructor vacio (necesario para que Gson pueda crear objetos)
    public Usuario() {}

    // Constructor completo (cuando leemos de la BD, ya tiene ID)
    public Usuario(int idUsuario, String nombre, String correo, String contrasena, boolean estado) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    // Constructor sin ID (para crear usuario nuevo, MySQL genera el ID)
    public Usuario(String nombre, String correo, String contrasena, boolean estado) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
}
```

**¿Por que dos constructores?**

- **Con ID:** Cuando leemos un usuario de la BD, MySQL ya le asigno un `idUsuario`. Usamos este constructor en el DAO al mapear el `ResultSet`.
- **Sin ID:** Cuando creamos un usuario nuevo, todavia no tiene ID (MySQL lo genera con `AUTO_INCREMENT`). Usamos este constructor en el Service.

**¿Por que getters y setters?**

Los campos son `private` (solo accesibles dentro de la clase). Los getters y setters permiten acceder a ellos desde fuera de forma controlada. Ademas:
- `setContrasena(null)` → Se usa para **ocultar la contrasena** antes de enviar el usuario al frontend
- Gson usa los getters para convertir el objeto a JSON

---

## Paso 11: DAO — Acceso a la Base de Datos

**Archivo:** `dao/UsuarioDAO.java`

El DAO (Data Access Object) es la **unica clase** que ejecuta SQL contra la base de datos. Ningun otro archivo escribe SQL.

```java
package com.backend.dao;

import com.backend.config.dbConnection;
import com.backend.models.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // ============ BUSCAR POR CORREO ============
    // Usado en: Login y validar correo duplicado al crear
    public static Usuario findByCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findByCorreo: " + e.getMessage());
        }
        return null;
    }

    // ============ BUSCAR POR ID ============
    // Usado en: Obtener usuario, Actualizar usuario
    public static Usuario findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findById: " + e.getMessage());
        }
        return null;
    }

    // ============ LISTAR TODOS ============
    // Usado en: GET /api/users
    public static List<Usuario> findAll() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findAll: " + e.getMessage());
        }
        return lista;
    }

    // ============ CREAR USUARIO ============
    // Usado en: POST /api/users
    public static Usuario create(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getCorreo());
            stmt.setString(3, u.getContrasena());
            stmt.setBoolean(4, u.isEstado());
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) u.setIdUsuario(keys.getInt(1));
                return u;
            }
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.create: " + e.getMessage());
        }
        return null;
    }

    // ============ ACTUALIZAR USUARIO ============
    // Usado en: PUT y PATCH /api/users/id
    public static boolean update(Usuario u) {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, estado = ? WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getCorreo());
            stmt.setBoolean(3, u.isEstado());
            stmt.setInt(4, u.getIdUsuario());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.update: " + e.getMessage());
        }
        return false;
    }

    // ============ ACTUALIZAR CONTRASENA ============
    // Usado en: PUT y PATCH cuando se envia contrasena nueva
    public static boolean updatePassword(int id, String hashedPassword) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.updatePassword: " + e.getMessage());
        }
        return false;
    }

    // ============ OBTENER ROL DEL USUARIO ============
    // Usado en: Login (para incluir el rol en el token JWT)
    public static String findRolByUsuarioId(int usuarioId) {
        String sql = """
                SELECT r.nombre FROM roles r
                INNER JOIN usuarios_roles ur ON r.id_roles = ur.rol_id
                WHERE ur.usuario_id = ? LIMIT 1
                """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("nombre");
        } catch (Exception e) {
            System.out.println("Error UsuarioDAO.findRolByUsuarioId: " + e.getMessage());
        }
        return null;
    }

    // ============ MAPEAR FILA DE BD A OBJETO JAVA ============
    private static Usuario mapRow(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("contrasena"),
                rs.getBoolean("estado"));
    }
}
```

**Conceptos importantes:**

**¿Que es `PreparedStatement` y por que usamos `?`?**
```java
String sql = "SELECT * FROM usuarios WHERE correo = ?";
stmt.setString(1, correo);  // El 1 se refiere al primer ?
```
El `?` es un **placeholder**. En vez de concatenar el valor directo en el SQL (peligroso), usamos `setString()` para que Java lo inserte de forma segura. Esto **previene inyeccion SQL** (un ataque donde alguien envia SQL malicioso como input).

**¿Que es `try-with-resources`?**
```java
try (Connection conn = dbConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    // usar conn y stmt
}
// conn y stmt se cierran AUTOMATICAMENTE aqui, incluso si hay error
```
Al usar `try()` con parentesis, Java cierra automaticamente la conexion y el statement al terminar. Sin esto, las conexiones quedarian abiertas y la BD dejaria de aceptar nuevas.

**¿Que es `Statement.RETURN_GENERATED_KEYS`?**

En `create()`, al hacer un `INSERT`, MySQL genera un ID automatico. Para obtener ese ID usamos `RETURN_GENERATED_KEYS` y luego `getGeneratedKeys()`. Asi devolvemos el usuario creado con su ID.

**¿Que es `mapRow()`?**

Metodo privado que toma una fila del resultado (`ResultSet`) y la convierte a un objeto `Usuario`. Se reutiliza en todos los metodos que leen de la BD.

---

## Paso 12: Services — Logica de negocio y validaciones

Los Services son la capa mas importante. Contienen TODAS las validaciones y reglas de negocio. Los Controllers NO validan, solo delegan al Service.

### AuthService.java — Logica del Login

**Archivo:** `services/AuthService.java`

```java
package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.helpers.JwtHelper;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;
import com.google.gson.JsonObject;

public class AuthService {

    public static JsonObject validateLogin(String correo, String contrasena) {
        JsonObject response = new JsonObject();

        // 1. Validar que los campos no esten vacios
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            response.addProperty("success", false);
            response.addProperty("message", "Correo y contrasena son requeridos");
            response.addProperty("status", 400);
            return response;
        }

        // 2. Buscar usuario por correo en la BD
        Usuario usuario = UsuarioDAO.findByCorreo(correo);
        if (usuario == null) {
            response.addProperty("success", false);
            response.addProperty("message", "Credenciales invalidas");
            response.addProperty("status", 401);
            return response;
        }

        // 3. Verificar contrasena con BCrypt
        if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
            response.addProperty("success", false);
            response.addProperty("message", "Credenciales invalidas");
            response.addProperty("status", 401);
            return response;
        }

        // 4. Verificar que el usuario este activo
        if (!usuario.isEstado()) {
            response.addProperty("success", false);
            response.addProperty("message", "Usuario inactivo. Contacte al administrador");
            response.addProperty("status", 403);
            return response;
        }

        // 5. Obtener el rol del usuario
        String rol = UsuarioDAO.findRolByUsuarioId(usuario.getIdUsuario());
        if (rol == null) rol = "Sin rol";

        // 6. Generar token JWT
        String token = JwtHelper.generateToken(usuario.getIdUsuario(), usuario.getCorreo(), rol);

        // 7. Armar respuesta exitosa
        response.addProperty("success", true);
        response.addProperty("message", "Login exitoso");
        response.addProperty("token", token);
        response.addProperty("nombre", usuario.getNombre());
        response.addProperty("correo", usuario.getCorreo());
        response.addProperty("rol", rol);
        response.addProperty("status", 200);

        return response;
    }
}
```

**Flujo visual del login:**

```
correo + contrasena
       ↓
   ¿Estan vacios? ──────────── Si → 400 "Correo y contrasena son requeridos"
       ↓ No
   ¿Existe en la BD? ─────── No → 401 "Credenciales invalidas"
       ↓ Si
   ¿Contrasena coincide? ──── No → 401 "Credenciales invalidas"
       ↓ Si
   ¿Usuario activo? ─────── No → 403 "Usuario inactivo"
       ↓ Si
   Obtener rol → Generar JWT
       ↓
   200 + token + datos del usuario
```

**Nota de seguridad:** En los pasos 2 y 3 se responde el MISMO mensaje ("Credenciales invalidas"). Esto es a proposito: si dijera "correo no encontrado" vs "contrasena incorrecta", un atacante podria saber si un correo esta registrado.

### UserService.java — Logica del CRUD de Usuarios

**Archivo:** `services/UserService.java`

#### Crear usuario — `validateAndCreate()`

```java
public static JsonObject validateAndCreate(String nombre, String correo,
                                            String contrasena, boolean estado) {
    JsonObject response = new JsonObject();

    // Validar campos obligatorios
    if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()
            || contrasena == null || contrasena.isBlank()) {
        response.addProperty("success", false);
        response.addProperty("message", "Nombre, correo y contrasena son requeridos");
        response.addProperty("status", 400);
        return response;
    }

    // Verificar que el correo no este registrado
    if (UsuarioDAO.findByCorreo(correo) != null) {
        response.addProperty("success", false);
        response.addProperty("message", "El correo ya esta registrado");
        response.addProperty("status", 409);
        return response;
    }

    // Crear usuario con contrasena encriptada
    Usuario nuevo = new Usuario(nombre, correo,
            PasswordHelper.hashPassword(contrasena), estado);
    Usuario creado = UsuarioDAO.create(nuevo);

    if (creado != null) {
        creado.setContrasena(null);  // Ocultar contrasena en la respuesta
        response.addProperty("success", true);
        response.addProperty("message", "Usuario creado exitosamente");
        response.add("data", gson.toJsonTree(creado));
        response.addProperty("status", 201);
    } else {
        response.addProperty("success", false);
        response.addProperty("message", "Error al crear el usuario");
        response.addProperty("status", 500);
    }
    return response;
}
```

#### Actualizar completo — `validateAndUpdate()` (PUT)

```java
public static JsonObject validateAndUpdate(int id, String nombre, String correo,
                                            String contrasena, boolean estado) {
    // 1. Verificar que el usuario existe → 404 si no
    // 2. Validar nombre y correo obligatorios → 400
    // 3. Si cambio el correo, verificar duplicado → 409
    // 4. Actualizar nombre, correo, estado via UsuarioDAO.update()
    // 5. Si envio contrasena, actualizarla via UsuarioDAO.updatePassword()
    // 6. Retornar usuario actualizado → 200
}
```

#### Actualizar parcial — `partialUpdate()` (PATCH)

```java
public static JsonObject partialUpdate(int id, String nombre, String correo,
                                        String contrasena, Boolean estado) {
    // 1. Verificar que el usuario existe → 404
    // 2. Si envio correo nuevo, verificar duplicado → 409
    // 3. Solo actualizar campos que NO son null
    // 4. Si envio contrasena, actualizarla aparte
    // 5. Retornar usuario actualizado → 200
}
```

**Diferencia entre PUT y PATCH:**

| Aspecto | PUT (validateAndUpdate) | PATCH (partialUpdate) |
|---------|------------------------|----------------------|
| Campos | Nombre y correo OBLIGATORIOS | Todos opcionales |
| Estado | `boolean estado` (primitivo) | `Boolean estado` (objeto, puede ser `null`) |
| Logica | Reemplaza TODOS los campos | Solo modifica los enviados |
| Uso | Formulario de edicion completo | Cambiar solo un campo |

**¿Por que `Boolean` (objeto) en PATCH y `boolean` (primitivo) en PUT?**

`boolean` solo puede ser `true` o `false`. `Boolean` puede ser `true`, `false` o `null`. En PATCH necesitamos saber si el frontend envio el campo o no: si envio `estado: false` → actualizamos; si no envio `estado` → lo dejamos como esta. Con `boolean` no podriamos distinguir "envio false" de "no envio nada".

**Tabla de validaciones:**

| Operacion | Caso | HTTP | Mensaje |
|-----------|------|------|---------|
| Crear | Campos vacios | 400 | "Nombre, correo y contrasena son requeridos" |
| Crear | Correo duplicado | 409 | "El correo ya esta registrado" |
| Crear | Error de BD | 500 | "Error al crear el usuario" |
| Crear | Exito | 201 | "Usuario creado exitosamente" |
| PUT | No existe | 404 | "Usuario no encontrado" |
| PUT | Campos vacios | 400 | "Nombre y correo son obligatorios en PUT" |
| PUT | Correo en uso | 409 | "El correo ya esta en uso por otro usuario" |
| PUT | Exito | 200 | "Usuario actualizado exitosamente" |
| PATCH | No existe | 404 | "Usuario no encontrado" |
| PATCH | Correo en uso | 409 | "El correo ya esta en uso por otro usuario" |
| PATCH | Exito | 200 | "Usuario actualizado parcialmente" |

---

## Paso 13: Controllers — Recepcion de peticiones HTTP

Los Controllers son el **punto de entrada** de cada peticion. Su responsabilidad es simple:
1. Leer los datos de la peticion (body JSON, parametros de URL)
2. Delegar al Service correspondiente
3. Enviar la respuesta al frontend

### AuthController.java

**Archivo:** `controllers/AuthController.java`

#### Login — `POST /api/auth/login`

```java
public static HttpHandler login() {
    return exchange -> {
        ApiRequest request = new ApiRequest(exchange);
        String body = request.readBody();

        if (body.isEmpty()) {
            ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
            return;
        }

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);

        String correo = json.has("correo") ? json.get("correo").getAsString() : "";
        String contrasena = json.has("contrasena") ? json.get("contrasena").getAsString() : "";

        JsonObject response = AuthService.validateLogin(correo, contrasena);
        int code = response.get("status").getAsInt();
        response.remove("status");

        ApiResponse.send(exchange, response.toString(), code);
    };
}
```

**Flujo:** Leer body → Parsear JSON → Extraer correo y contrasena → AuthService valida → Retorna token JWT

#### Obtener usuario autenticado — `GET /api/auth/me` (ruta protegida)

```java
public static HttpHandler me() {
    return exchange -> {
        // El middleware ya valido el token y puso los datos en el exchange
        String userId = (String) exchange.getAttribute("userId");
        String correo = (String) exchange.getAttribute("correo");
        String rol = (String) exchange.getAttribute("rol");

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("userId", userId);
        response.addProperty("correo", correo);
        response.addProperty("rol", rol);

        ApiResponse.send(exchange, response.toString(), 200);
    };
}
```

**¿Como llegan los datos al `exchange.getAttribute()`?** El middleware de autenticacion los puso ahi despues de validar el token JWT. El controller solo los lee.

### UserController.java

**Archivo:** `controllers/UserController.java`

#### Listar todos — `GET /api/users`

```java
public static HttpHandler listAll() {
    return exchange -> {
        List<Usuario> lista = UsuarioDAO.findAll();
        lista.forEach(u -> u.setContrasena(null));  // Ocultar contrasenas
        ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
    };
}
```

#### Obtener por ID — `GET /api/users/id?id=5`

```java
public static HttpHandler getById() {
    return exchange -> {
        String query = exchange.getRequestURI().getQuery();  // Obtiene "id=5"
        if (query == null || !query.matches("id=\\d+")) {
            ApiResponse.error(exchange, 400, "Parametro id requerido (ej: ?id=5)");
            return;
        }
        int id = Integer.parseInt(query.split("=")[1]);

        Usuario u = UsuarioDAO.findById(id);
        if (u == null) {
            ApiResponse.error(exchange, 404, "Usuario no encontrado");
            return;
        }
        u.setContrasena(null);
        ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", u));
    };
}
```

#### Crear — `POST /api/users`

```java
public static HttpHandler create() {
    return exchange -> {
        ApiRequest request = new ApiRequest(exchange);
        String body = request.readBody();

        if (body.isEmpty()) {
            ApiResponse.error(exchange, 400, "El cuerpo de la peticion esta vacio");
            return;
        }

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);

        String nombre = json.has("nombre") ? json.get("nombre").getAsString() : "";
        String correo = json.has("correo") ? json.get("correo").getAsString() : "";
        String contrasena = json.has("contrasena") ? json.get("contrasena").getAsString() : "";
        boolean estado = json.has("estado") ? json.get("estado").getAsBoolean() : true;

        JsonObject response = UserService.validateAndCreate(nombre, correo, contrasena, estado);
        int code = response.get("status").getAsInt();
        response.remove("status");

        ApiResponse.send(exchange, response.toString(), code);
    };
}
```

#### Actualizar completo — `PUT /api/users/id?id=5`

Mismo patron: extraer ID del query string → leer body → parsear JSON → delegar a `UserService.validateAndUpdate()`.

#### Actualizar parcial — `PATCH /api/users/id?id=5`

Mismo patron pero usa `null` como valor por defecto cuando el campo no viene en el JSON, y delega a `UserService.partialUpdate()`.

**¿Que es un `HttpHandler` y por que se retorna con `return exchange -> { ... }`?**

`HttpHandler` es una interfaz de Java con un solo metodo: `handle(HttpExchange exchange)`. En vez de crear una clase que la implemente, usamos una **expresion lambda** (`exchange -> { ... }`) que es una forma corta de escribir lo mismo.

**¿Por que se hace `response.remove("status")`?**

El Service agrega un campo `"status"` al JsonObject para indicar el codigo HTTP (200, 400, 404, etc.). Ese campo es para uso interno — no lo enviamos al frontend. Lo extraemos, lo usamos como status code de la respuesta HTTP, y lo removemos del JSON.

---

## Paso 14: Middleware de Autenticacion

**Archivo:** `middlewares/AuthMiddleware.java`

El middleware es una capa que se ejecuta **ANTES** del controller. Se usa para proteger rutas que requieren autenticacion.

```java
package com.backend.middlewares;

import com.backend.helpers.JwtHelper;
import com.backend.server.http.ApiResponse;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

public class AuthMiddleware {

    public HttpHandler protect(HttpHandler next, String... allowedRoles) {
        return exchange -> {
            try {
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    ApiResponse.handleCors(exchange);
                    return;
                }

                // 1. Buscar el header "Authorization"
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    ApiResponse.error(exchange, 401, "Token de autenticacion requerido");
                    return;
                }

                // 2. Extraer y validar el token JWT
                Claims claims = JwtHelper.validateToken(authHeader.substring(7));

                // 3. Guardar datos del usuario en el exchange
                exchange.setAttribute("userId", claims.getSubject());
                exchange.setAttribute("correo", claims.get("correo", String.class));
                exchange.setAttribute("rol", claims.get("rol", String.class));

                // 4. Si se especificaron roles, verificar autorizacion
                if (allowedRoles.length > 0) {
                    String userRol = claims.get("rol", String.class);
                    boolean authorized = false;
                    for (String rol : allowedRoles) {
                        if (rol.equalsIgnoreCase(userRol)) {
                            authorized = true;
                            break;
                        }
                    }
                    if (!authorized) {
                        ApiResponse.error(exchange, 403, "No tiene permiso para esta accion");
                        return;
                    }
                }

                // 5. Todo bien → ejecutar el handler original
                next.handle(exchange);

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                ApiResponse.error(exchange, 401, "Token expirado. Inicie sesion nuevamente");
            } catch (io.jsonwebtoken.JwtException e) {
                ApiResponse.error(exchange, 401, "Token invalido");
            } catch (Exception e) {
                ApiResponse.error(exchange, 500, "Error interno del servidor");
            }
        };
    }
}
```

**¿Como se usa?**
```java
// Cualquier usuario autenticado puede acceder
auth.protect(AuthController.me())

// Solo SUPER_ADMIN y ADMIN pueden acceder
auth.protect(UserController.delete(), "SUPER_ADMIN", "ADMIN")
```

**Flujo de ejecucion:**

```
Peticion llega con Header: "Authorization: Bearer eyJhbG..."
       ↓
   ¿Header existe y empieza con "Bearer "? ── No → 401 "Token requerido"
       ↓ Si
   Extraer token (quitar "Bearer ")
       ↓
   ¿Token valido? ── Expiro → 401 "Token expirado"
                  ── Invalido → 401 "Token invalido"
       ↓ Valido
   Guardar userId, correo, rol en exchange
       ↓
   ¿Se especificaron roles permitidos?
       ↓ Si                    ↓ No
   ¿Rol del usuario esta      Ejecutar handler
    en la lista?               (controller)
       ↓ No         ↓ Si
   403 "No tiene    Ejecutar handler
    permiso"        (controller)
```

---

## Paso 15: Seeders — Datos iniciales

**Carpeta:** `seeders/`

Los seeders insertan datos que el sistema necesita para funcionar desde el primer momento. Se ejecutan automaticamente al arrancar la aplicacion.

**Todos siguen el mismo patron:**
1. Verificar si la tabla ya tiene datos (`SELECT COUNT(*)`)
2. Si tiene datos → omitir (evita duplicados al reiniciar)
3. Si esta vacia → insertar los registros

### SeedRoles.java

```java
private static final String[][] roles = {
    {"SUPER_ADMIN", "Acceso total al sistema incluyendo configuracion tecnica"},
    {"ADMIN", "Gestion completa del negocio: ventas, inventario, compras y reportes"},
    {"EMPLEADO", "Acceso operativo limitado a funciones del dia a dia"}
};
```

### SeedPermisos.java

Inserta 10 permisos: Gestionar Usuarios, Gestionar Roles, Gestionar Productos, Gestionar Categorias, Gestionar Ventas, Gestionar Compras, Gestionar Movimientos, Gestionar Gastos, Ver Reportes, Gestionar Perfil.

### SeedTipoMovimientos.java

```
Venta           → Ingreso
Compra          → Egreso
Gasto Adicional → Egreso
```

### SeedTipoGasto.java

Transporte, Impuestos, Almacenamiento, Embalaje, Otros.

---

## Paso 16: Punto de entrada — Main.java

**Archivo:** `Main.java`

Es el archivo que arranca todo el sistema:

```java
package com.backend;

import com.backend.seeders.SeedRoles;
import com.backend.seeders.SeedPermisos;
import com.backend.seeders.SeedTipoMovimientos;
import com.backend.seeders.SeedTipoGasto;
import com.backend.server.serverConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando UrbanLife Backend...");

        // 1. Ejecutar seeders (datos iniciales)
        SeedRoles.insertRoles();
        SeedPermisos.insertPermisos();
        SeedTipoMovimientos.insertTipoMovimientos();
        SeedTipoGasto.insertTipoGasto();

        // 2. Iniciar el servidor HTTP en puerto 8080
        serverConnection.startServer(8080);
    }
}
```

**Orden de ejecucion completo al iniciar la aplicacion:**

```
1. Se ejecuta Main.main()
2. Seeders verifican e insertan datos iniciales en la BD
   - Roles: SUPER_ADMIN, ADMIN, EMPLEADO
   - Permisos: 10 permisos del sistema
   - Tipos de movimiento: Venta (Ingreso), Compra (Egreso), Gasto (Egreso)
   - Tipos de gasto: Transporte, Impuestos, Almacenamiento, Embalaje, Otros
3. serverConnection.startServer(8080) crea el HttpServer
4. Routes.configureRoutes() registra todas las rutas en el Router
5. El servidor inicia en http://localhost:8080
6. Listo para recibir peticiones del frontend
```

---

## Flujos completos (de principio a fin)

### Flujo: Crear un usuario (POST /api/users)

```
Frontend envia POST /api/users
Body: {"nombre":"David","correo":"d@mail.com","contrasena":"123"}
    ↓
Router.handle() busca: routes["POST"]["/api/users"]
    ↓ Encuentra → UserController.create()
    ↓
Controller:
    ├── Lee el body JSON
    ├── Extrae nombre, correo, contrasena, estado
    └── Llama a UserService.validateAndCreate(...)
    ↓
Service:
    ├── ¿Campos vacios? → No → Continuar
    ├── ¿Correo existe? → UsuarioDAO.findByCorreo("d@mail.com") → null → No existe
    ├── Encriptar contrasena: PasswordHelper.hashPassword("123") → "$2a$12$..."
    └── Insertar: UsuarioDAO.create(nuevo) → INSERT INTO usuarios → Retorna usuario con ID
    ↓
Controller: Extrae status code → Envia respuesta JSON
    ↓
Frontend recibe: 201 {"success":true,"message":"Usuario creado","data":{...}}
```

### Flujo: Login (POST /api/auth/login)

```
Frontend envia POST /api/auth/login
Body: {"correo":"d@mail.com","contrasena":"123"}
    ↓
Router.handle() busca: routes["POST"]["/api/auth/login"]
    ↓ Encuentra → AuthController.login()
    ↓
Controller:
    ├── Lee el body JSON
    ├── Extrae correo y contrasena
    └── Llama a AuthService.validateLogin(...)
    ↓
Service:
    ├── UsuarioDAO.findByCorreo("d@mail.com") → Usuario encontrado
    ├── PasswordHelper.checkPassword("123", "$2a$12$...") → true
    ├── usuario.isEstado() → true (activo)
    ├── UsuarioDAO.findRolByUsuarioId(1) → "ADMIN"
    └── JwtHelper.generateToken(1, "d@mail.com", "ADMIN") → "eyJhbG..."
    ↓
Frontend recibe: 200 {"success":true,"token":"eyJhbG...","nombre":"David","rol":"ADMIN"}
```

### Flujo: Ruta protegida (GET /api/auth/me)

```
Frontend envia GET /api/auth/me
Header: "Authorization: Bearer eyJhbG..."
    ↓
Router.handle() busca: routes["GET"]["/api/auth/me"]
    ↓ Encuentra → auth.protect(AuthController.me())
    ↓
AuthMiddleware.protect():
    ├── Extrae token del header "Authorization"
    ├── JwtHelper.validateToken("eyJhbG...") → Claims: {sub:"1", correo:"d@mail.com", rol:"ADMIN"}
    ├── exchange.setAttribute("userId", "1")
    ├── exchange.setAttribute("correo", "d@mail.com")
    ├── exchange.setAttribute("rol", "ADMIN")
    └── next.handle(exchange) → Pasa al controller
    ↓
AuthController.me():
    ├── Lee exchange.getAttribute("userId") → "1"
    ├── Lee exchange.getAttribute("correo") → "d@mail.com"
    └── Lee exchange.getAttribute("rol") → "ADMIN"
    ↓
Frontend recibe: 200 {"success":true,"userId":"1","correo":"d@mail.com","rol":"ADMIN"}
```

---

## Endpoints del modulo de Usuarios y Auth

| Metodo | Ruta | Protegida | Descripcion |
|--------|------|-----------|-------------|
| POST | `/api/auth/login` | No | Iniciar sesion (retorna token JWT) |
| GET | `/api/auth/me` | Si (JWT) | Obtener datos del usuario autenticado |
| GET | `/api/users` | No | Listar todos los usuarios |
| POST | `/api/users` | No | Crear un usuario nuevo |
| GET | `/api/users/id?id=X` | No | Obtener un usuario por su ID |
| PUT | `/api/users/id?id=X` | No | Actualizar usuario completo |
| PATCH | `/api/users/id?id=X` | No | Actualizar usuario parcialmente |

---

## Como ejecutar el proyecto

```bash
# 1. Crear la base de datos
#    Abrir MySQL y ejecutar el script db/UrbanLife.sql

# 2. Crear el archivo .env en la raiz del proyecto
#    DB_URL=jdbc:mysql://localhost:3306/UrbanLife
#    DB_USER=root
#    DB_PASSWD=tu_contrasena
#    JWT_SECRET=clave_secreta_de_al_menos_32_caracteres

# 3. Compilar y ejecutar
mvn clean compile exec:java
```

El servidor iniciara en `http://localhost:8080` y los seeders insertaran los datos iniciales automaticamente.

---

## Roles del sistema

| Rol | Descripcion | Quien lo usa |
|-----|-------------|-------------|
| `SUPER_ADMIN` | Acceso total incluyendo configuracion tecnica | Desarrollador |
| `ADMIN` | Gestion completa del negocio | Dueno del negocio |
| `EMPLEADO` | Acceso operativo limitado | Empleados de la tienda |
