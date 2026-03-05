# Documentacion Paso a Paso — Backend UrbanLife (Modulo de Usuarios y Autenticacion)

Esta documentacion explica paso a paso como se construyo el backend del proyecto UrbanLife desde cero. Esta pensada para alguien que nunca ha hecho un proyecto en Java y necesita entender que se hizo, por que se hizo, y en que orden.

> **Alcance:** Cubre el modulo de **Usuarios** (CRUD), **Autenticacion** (Login + JWT + Google OAuth) y **Recuperacion de contrasena**. Los demas modulos se documentaran por separado.

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
| **javax.mail** | Envia correos electronicos via SMTP (Gmail) |

**Arquitectura del proyecto (patron por capas):**

Cada peticion que llega del frontend pasa por estas capas en orden:

```
Peticion HTTP → Router → Middleware → Controller → Service → DAO → Base de Datos
                                                                        ↓
Respuesta JSON ←──────── Controller ←──── Service ←── DAO ←── Resultado de la BD
```

Cada capa tiene una responsabilidad unica:
- **Router**: Decide a que Controller enviar la peticion segun la ruta y el metodo HTTP
- **Middleware**: Intercepta la peticion antes del Controller para verificar JWT y roles
- **Controller**: Recibe la peticion, extrae los datos y delega al Service
- **Service**: Contiene TODA la logica de validacion y reglas de negocio
- **DAO**: Es el UNICO que escribe SQL y habla con la base de datos

---

## Estructura de carpetas del proyecto

```
src/main/java/com/backend/
│
├── Main.java                              → Punto de entrada (arranca todo)
│
├── config/
│   └── dbConnection.java                 → Conexion a MySQL usando .env
│
├── server/
│   ├── serverConnection.java             → Crea y arranca el servidor HTTP
│   └── http/
│       ├── ApiRequest.java               → Lee el cuerpo (body) de las peticiones
│       └── ApiResponse.java              → Envia respuestas JSON estandarizadas
│
├── routes/
│   ├── Router.java                       → Motor de ruteo (busca que handler ejecutar)
│   └── Routes.java                       → Archivo donde se registran TODAS las rutas
│
├── models/
│   ├── Usuario.java                      → Representa la tabla usuarios
│   ├── Rol.java                          → Representa la tabla roles
│   ├── UsuarioRol.java                   → Tabla intermedia usuarios_roles
│   └── TokenRecuperacion.java            → Representa la tabla token_recuperacion
│
├── dto/
│   ├── CreateUserRequest.java            → Datos que llegan al crear un usuario
│   └── LoginRequest.java                 → Datos que llegan al hacer login
│
├── dao/
│   ├── UsuarioDAO.java                   → Consultas SQL para la tabla usuarios
│   ├── RolDAO.java                       → Consultas SQL para la tabla roles
│   ├── UsuarioRolDAO.java                → Consultas SQL para la tabla usuarios_roles
│   └── TokenRecuperacionDAO.java         → Consultas SQL para tokens de recuperacion
│
├── services/
│   ├── AuthService.java                  → Logica del login
│   ├── UserService.java                  → Logica del CRUD de usuarios
│   ├── GoogleAuthService.java            → Logica del login con Google
│   ├── PasswordResetService.java         → Logica de recuperacion de contrasena
│   └── EmailService.java                 → Envio de correos electronicos
│
├── controllers/
│   ├── AuthController.java               → Endpoints de autenticacion (login, me)
│   ├── UserController.java               → Endpoints del CRUD de usuarios
│   ├── GoogleAuthController.java         → Endpoint de login con Google
│   └── PasswordResetController.java      → Endpoints de recuperacion de contrasena
│
├── helpers/
│   ├── PasswordHelper.java               → Encripta y verifica contrasenas con BCrypt
│   ├── JwtHelper.java                    → Genera y valida tokens JWT
│   └── JsonHelper.java                   → Convierte JSON a objetos Java y viceversa
│
├── middlewares/
│   └── AuthMiddleware.java               → Protege rutas verificando el token JWT y roles
│
└── seeders/                               → Datos iniciales que se insertan al arrancar
    ├── SeedRoles.java
    ├── SeedPermisos.java
    ├── SeedTipoMovimientos.java
    └── SeedTipoGasto.java
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

    <!-- Envio de correos electronicos (recuperacion de contrasena) -->
    <dependency>
        <groupId>com.sun.mail</groupId>
        <artifactId>javax.mail</artifactId>
        <version>1.6.2</version>
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

GOOGLE_CLIENT_ID=tu_client_id_de_google_cloud_console

EMAIL_USER=tu_correo@gmail.com
EMAIL_PASS=tu_contrasena_de_aplicacion_gmail
```

**¿Por que usamos .env en vez de escribir las credenciales directo en el codigo?**
- Si el codigo se sube a GitHub, las credenciales quedarian expuestas publicamente
- Cada desarrollador puede tener credenciales diferentes
- El `.env` se agrega al `.gitignore` para que Git lo ignore

**Nota sobre EMAIL_PASS:** Gmail no acepta la contrasena normal cuando la cuenta tiene verificacion en dos pasos. Se debe crear una "Contrasena de aplicacion" desde la configuracion de la cuenta de Google.

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

Se ejecuta este script en MySQL para crear todas las tablas. Para el modulo de usuarios y autenticacion, las tablas relevantes son:

```sql
CREATE TABLE Usuarios (
    ID_USUARIO INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(100) NOT NULL,
    CORREO VARCHAR(120) NOT NULL UNIQUE,
    CONTRASENA VARCHAR(255),           -- NULL si la cuenta es de Google
    ESTADO BOOLEAN NOT NULL DEFAULT TRUE,
    GOOGLE_ID VARCHAR(255) UNIQUE      -- ID de Google, NULL si no usa Google
);

CREATE TABLE Roles (
    ID_ROLES INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(80) NOT NULL UNIQUE,
    DESCRIPCION VARCHAR(200)
);

CREATE TABLE Usuarios_Roles (
    ID_USUARIO_ROL INT AUTO_INCREMENT PRIMARY KEY,
    USUARIO_ID INT NOT NULL,
    ROL_ID INT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO),
    FOREIGN KEY (ROL_ID) REFERENCES Roles(ID_ROLES),
    UNIQUE (USUARIO_ID, ROL_ID)
);

CREATE TABLE Token_Recuperacion (
    ID_TOKEN INT AUTO_INCREMENT PRIMARY KEY,
    USUARIO_ID INT NOT NULL,
    TOKEN VARCHAR(255) NOT NULL UNIQUE,
    FECHA_EXPIRACION DATETIME NOT NULL,
    USADO BOOLEAN NOT NULL DEFAULT FALSE,
    FECHA_CREACION DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (USUARIO_ID) REFERENCES Usuarios(ID_USUARIO)
);
```

**Conceptos clave:**

- `AUTO_INCREMENT` → MySQL genera el ID automaticamente al insertar
- `NOT NULL` → El campo es obligatorio
- `UNIQUE` → No puede haber duplicados (ej: dos usuarios con el mismo correo)
- `FOREIGN KEY` → Vincula una tabla con otra (ej: `USUARIO_ID` apunta a `Usuarios`)
- `DEFAULT TRUE` → Si no se especifica, el estado sera `true` (activo)
- `CONTRASENA VARCHAR(255)` sin `NOT NULL` → Puede ser `NULL` cuando la cuenta fue creada con Google
- `GOOGLE_ID` → Se llena al vincular o crear una cuenta con Google; queda `NULL` si no usa Google
- `USADO BOOLEAN DEFAULT FALSE` en `Token_Recuperacion` → Los tokens de recuperacion son de un solo uso
- `Usuarios_Roles` es una **tabla intermedia** que conecta usuarios con roles. El `UNIQUE (USUARIO_ID, ROL_ID)` evita asignar el mismo rol dos veces

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
    "/api/users"                        → ejecuta middleware → UserController.listAll()
    "/api/users/id"                     → ejecuta middleware → UserController.getById()
    "/api/auth/me"                      → ejecuta middleware → AuthController.me()
    "/api/auth/reset-password/validate" → PasswordResetController.validarToken()
  },
  "POST": {
    "/api/users"              → ejecuta middleware → UserController.create()
    "/api/auth/login"         → AuthController.login()
    "/api/auth/google"        → GoogleAuthController.loginWithGoogle()
    "/api/auth/forgot-password" → PasswordResetController.solicitarRecuperacion()
    "/api/auth/reset-password"  → PasswordResetController.cambiarContrasena()
  },
  "PUT":   { "/api/users/id" → ejecuta middleware → UserController.update() },
  "PATCH": { "/api/users/id" → ejecuta middleware → UserController.patch() }
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

import com.backend.controllers.*;
import com.backend.middlewares.AuthMiddleware;
import com.sun.net.httpserver.HttpHandler;

public class Routes {

    Router router = new Router();

    public HttpHandler configureRoutes() {
        AuthMiddleware auth = new AuthMiddleware();

        // ========== RUTAS DE AUTH ==========
        router.post("/api/auth/login",  AuthController.login());
        router.post("/api/auth/google", GoogleAuthController.loginWithGoogle());
        router.get("/api/auth/me",      auth.protect(AuthController.me()));

        // ========== RUTAS DE RECUPERACION DE CONTRASENA ==========
        router.post("/api/auth/forgot-password",         PasswordResetController.solicitarRecuperacion());
        router.get("/api/auth/reset-password/validate",  PasswordResetController.validarToken());
        router.post("/api/auth/reset-password",          PasswordResetController.cambiarContrasena());

        // ========== RUTAS DE USUARIOS ==========
        router.get("/api/users",      auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN"));
        router.post("/api/users",     auth.protect(UserController.create(),  "SUPER_ADMIN", "ADMIN"));
        router.get("/api/users/id",   auth.protect(UserController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        router.put("/api/users/id",   auth.protect(UserController.update(),  "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        router.patch("/api/users/id", auth.protect(UserController.patch(),   "SUPER_ADMIN", "ADMIN", "EMPLEADO"));

        return router;
    }
}
```

**¿Que hace cada linea?**

- `router.post("/api/auth/login", AuthController.login())` → Cuando llegue un `POST` a `/api/auth/login`, ejecuta el metodo `login()` del `AuthController`. Es **publica**, no necesita token.
- `router.get("/api/auth/me", auth.protect(AuthController.me()))` → **Primero** pasa por el middleware de autenticacion, y **solo si el token es valido**, ejecuta `me()`.
- `auth.protect(handler, "SUPER_ADMIN", "ADMIN")` → El middleware verifica el JWT **y ademas** que el rol del usuario este en la lista. Si el rol es `EMPLEADO`, responde `403`.

**Nota sobre las rutas de recuperacion de contrasena:** Son **publicas** porque el usuario que las usa aun no tiene sesion activa (olvidó su contrasena).

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
// body = {"correo":"d@mail.com","contrasena":"Abc1234"}
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

- `hashPassword("MiClave1")` → Genera un hash como `$2a$12$xRz7kL...` (60 caracteres). Este hash es lo que se guarda en la BD.
- `checkPassword("MiClave1", "$2a$12$xRz7kL...")` → Compara la contrasena contra el hash. Retorna `true` si coinciden.

**¿Por que BCrypt y no SHA-256 u otro?**
- BCrypt agrega un **salt** (valor aleatorio) a cada hash, entonces la misma contrasena genera hashes diferentes cada vez
- Es **lento a proposito** (factor 12 tarda ~250ms), lo que hace practicamente imposible un ataque de fuerza bruta
- Es **irreversible**: no se puede obtener la contrasena original desde el hash
- `checkpw` es en tiempo constante, resistente a ataques de timing

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

**¿Que contiene el token generado?**

```json
{
  "sub": "5",
  "correo": "david@mail.com",
  "rol": "ADMIN",
  "iat": 1700000000,
  "exp": 1700086400
}
```

- `sub` → ID del usuario
- `correo` y `rol` → Claims personalizados
- `iat` → Fecha de creacion
- `exp` → Expiracion (24 horas despues)
- El token va firmado con `JWT_SECRET`, nadie puede modificarlo sin invalidarlo

**¿Como se usa el token en la practica?**
```
1. Usuario hace login → Backend genera token y lo envia
2. Frontend guarda el token (ej: localStorage)
3. Frontend envia el token en cada peticion: Header "Authorization: Bearer eyJhbG..."
4. Backend valida el token y sabe quien es el usuario sin consultar la BD
```

**¿Que pasa si el token expira?**
`validateToken()` lanza `ExpiredJwtException`. El middleware la captura y responde `401 "Token expirado. Inicie sesion nuevamente"`.

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

## Paso 10: Modelos

**Paquete:** `models/`

Un **modelo** es una clase Java que representa exactamente una tabla de la base de datos. Son POJOs puros: solo atributos, constructores, getters y setters. No tienen logica de negocio.

### Usuario.java

```java
package com.backend.models;

public class Usuario {
    private int idUsuario;       // id_usuario INT AUTO_INCREMENT PRIMARY KEY
    private String nombre;       // nombre VARCHAR(100) NOT NULL
    private String correo;       // correo VARCHAR(120) NOT NULL UNIQUE
    private String contrasena;   // contrasena VARCHAR(255) — NULL si cuenta Google
    private boolean estado;      // estado BOOLEAN NOT NULL DEFAULT TRUE
    private String googleId;     // google_id VARCHAR(255) UNIQUE

    // Constructores, Getters y Setters...
}
```

**¿Por que tres constructores?**
- **Con todos los campos (incluyendo googleId):** Para mapear filas completas de la BD
- **Sin googleId:** Para usuarios sin autenticacion de Google
- **Sin ID:** Al crear un usuario nuevo, MySQL genera el ID automaticamente

**Campo clave:** `contrasena = NULL` es la senal de que esa cuenta fue creada con Google. Si alguien intenta hacer login con contrasena en una cuenta Google, el sistema lo detecta.

### Rol.java

```java
private int idRoles;
private String nombre;       // "SUPER_ADMIN", "ADMIN", "EMPLEADO"
private String descripcion;
```

### UsuarioRol.java

Tabla intermedia que relaciona un usuario con su rol:

```java
private int idUsuarioRol;
private int usuarioId;
private int rolId;
```

### TokenRecuperacion.java

Representa los tokens de un solo uso para restablecer contrasenas:

```java
private int idToken;
private int usuarioId;
private String token;                    // UUID aleatorio, ej: "a1b2c3d4-..."
private LocalDateTime fechaExpiracion;   // Ahora + 1 hora
private boolean usado;                   // false = disponible, true = ya se uso
private LocalDateTime fechaCreacion;
```

---

## Paso 11: DTOs (Data Transfer Objects)

**Paquete:** `dto/`

Un DTO es una clase que representa los datos que **llegan desde el cliente** en el body de una peticion. Se usa en lugar de parsear el JSON a mano campo por campo.

### CreateUserRequest.java

Para crear un usuario (`POST /api/users`):

```java
package com.backend.dto;

public class CreateUserRequest {
    private String nombre;
    private String correo;
    private String contrasena;

    public CreateUserRequest() {}

    public boolean isValid() {
        return nombre != null && !nombre.isBlank()
                && correo != null && !correo.isBlank()
                && contrasena != null && !contrasena.isBlank();
    }

    // Getters y Setters...
}
```

Gson convierte el JSON que llega en el body directamente a este objeto:
```java
CreateUserRequest request = new Gson().fromJson(body, CreateUserRequest.class);
String nombre = request.getNombre();
```

### LoginRequest.java

Para el login (`POST /api/auth/login`):

```java
private String correo;
private String contrasena;

public boolean isValid() {
    return correo != null && !correo.trim().isEmpty()
            && contrasena != null && !contrasena.trim().isEmpty();
}
```

**¿Por que usar DTOs en lugar de parsear el JSON a mano?**
El controller no necesita saber como se estructura el JSON. Solo recibe el DTO ya construido. Gson se encarga de la conversion automaticamente.

---

## Paso 12: DAOs — Acceso a la Base de Datos

**Paquete:** `dao/`

El DAO (Data Access Object) es la **unica clase** que ejecuta SQL contra la base de datos. Ningun otro archivo escribe SQL.

**Patron que siguen todos los DAOs:**
```java
// Siempre con try-with-resources para cerrar la conexion automaticamente
try (Connection conn = dbConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    // configurar parametros con stmt.setString(), stmt.setInt(), etc.
    // ejecutar con stmt.executeQuery() o stmt.executeUpdate()
    // retornar resultado o null si falla
} catch (Exception excepcion) {
    System.out.println("Error NombreDAO.metodo: " + excepcion.getMessage());
}
return null; // o false segun el caso
```

**¿Por que `PreparedStatement` y no concatenar strings?**

```java
// MAL (vulnerable a inyeccion SQL):
String sql = "SELECT * FROM usuarios WHERE correo = '" + correo + "'";

// BIEN (seguro):
String sql = "SELECT * FROM usuarios WHERE correo = ?";
stmt.setString(1, correo);  // Java escapa el valor automaticamente
```

**¿Que es `try-with-resources`?**

Al usar `try()` con parentesis, Java cierra automaticamente la conexion y el statement al terminar. Sin esto, las conexiones quedarian abiertas y la BD dejaria de aceptar nuevas.

### UsuarioDAO.java — Metodos principales

| Metodo | SQL que ejecuta |
|--------|----------------|
| `findByCorreo(correo)` | `SELECT * FROM usuarios WHERE correo = ?` |
| `findById(id)` | `SELECT * FROM usuarios WHERE id_usuario = ?` |
| `findByGoogleId(googleId)` | `SELECT * FROM usuarios WHERE google_id = ?` |
| `findAll()` | `SELECT * FROM usuarios ORDER BY id_usuario ASC` |
| `create(usuario)` | `INSERT INTO usuarios (nombre, correo, contrasena, estado) VALUES (?, ?, ?, ?)` |
| `createWithGoogle(...)` | INSERT con `contrasena = NULL` y `google_id = ?` |
| `linkGoogleId(id, googleId)` | `UPDATE usuarios SET google_id = ? WHERE id_usuario = ?` |
| `update(usuario)` | `UPDATE usuarios SET nombre, correo, estado WHERE id_usuario = ?` |
| `updatePassword(id, hash)` | `UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?` |
| `findRolByUsuarioId(id)` | JOIN entre `roles` y `usuarios_roles` para obtener el nombre del rol |

**¿Que es `Statement.RETURN_GENERATED_KEYS`?**

En `create()`, al hacer un `INSERT`, MySQL genera un ID automatico. Para obtener ese ID usamos `RETURN_GENERATED_KEYS` y luego `getGeneratedKeys()`. Asi devolvemos el usuario creado con su ID asignado.

El metodo privado `mapRow(ResultSet rs)` convierte una fila del resultado en un objeto `Usuario`. Se reutiliza en todos los metodos que leen de la BD para no repetir codigo.

### RolDAO.java

Operaciones sobre la tabla `roles`. El metodo mas usado en el modulo de autenticacion:

```java
public static Rol findByNombre(String nombre) {
    // SELECT * FROM roles WHERE nombre = ?
    // Usado para buscar el rol "EMPLEADO" al crear usuarios nuevos
}
```

### UsuarioRolDAO.java

Operaciones sobre la tabla intermedia `usuarios_roles`. El metodo mas usado:

```java
public static UsuarioRol create(UsuarioRol usuarioRol) {
    // INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)
    // Asigna el rol EMPLEADO a un usuario recien creado
}
```

### TokenRecuperacionDAO.java

Maneja los tokens de recuperacion de contrasena:

```java
// Genera un UUID, lo guarda en BD con fecha de expiracion, retorna el UUID
public static String guardarToken(int usuarioId, LocalDateTime fechaExpiracion)

// Verifica si el token existe, no esta usado y no ha expirado
public static JsonObject validarToken(String token)

// Hace UPDATE ... SET usado = TRUE para inutilizar el token
public static boolean marcarTokenUsado(int idToken)

// Actualiza la contrasena del usuario en la BD
public static boolean actualizarContrasena(int usuarioId, String hashContrasena)
```

---

## Paso 13: Seeders — Datos iniciales

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

**¿Por que son necesarios?** El sistema asigna el rol `EMPLEADO` a cada usuario nuevo. Si la tabla `roles` esta vacia, `RolDAO.findByNombre("EMPLEADO")` retornaria `null` y el usuario quedaria sin rol.

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

## Paso 14: Services — Logica de negocio y validaciones

Los Services son la capa mas importante. Contienen TODAS las validaciones y reglas de negocio. Los Controllers NO validan, solo delegan al Service.

### AuthService.java — Logica del Login

**Archivo:** `services/AuthService.java`

```java
private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

public static JsonObject validateLogin(String correo, String contrasena) {

    // 1. Validar que los campos no esten vacios
    if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
        // → 400 "Correo y contraseña son requeridos"
    }

    // 2. Validar formato de correo
    if (!correo.matches(EMAIL_REGEX)) {
        // → 400 "El formato del correo no es valido"
    }

    // 3. Buscar usuario por correo en la BD
    Usuario usuario = UsuarioDAO.findByCorreo(correo);
    if (usuario == null) {
        // → 401 "Credenciales invalidas"
    }

    // 4. Detectar si es cuenta de Google (no tiene contrasena)
    if (usuario.getContrasena() == null) {
        // → 401 "Esta cuenta usa inicio de sesion con Google"
    }

    // 5. Verificar contrasena con BCrypt
    if (!PasswordHelper.checkPassword(contrasena, usuario.getContrasena())) {
        // → 401 "Credenciales invalidas"
    }

    // 6. Verificar que el usuario este activo
    if (!usuario.isEstado()) {
        // → 403 "Usuario inactivo. Contacte al administrador"
    }

    // 7. Obtener rol → Generar JWT → 200 + token + datos del usuario
}
```

**Flujo visual del login:**

```
correo + contrasena
       ↓
   ¿Estan vacios?             → Si → 400 "Correo y contrasena son requeridos"
       ↓ No
   ¿Formato de correo valido? → No → 400 "El formato del correo no es valido"
       ↓ Si
   ¿Existe en la BD?          → No → 401 "Credenciales invalidas"
       ↓ Si
   ¿Tiene contrasena en BD?   → No → 401 "Esta cuenta usa inicio de sesion con Google"
       ↓ Si
   ¿Contrasena coincide?      → No → 401 "Credenciales invalidas"
       ↓ Si
   ¿Usuario activo?           → No → 403 "Usuario inactivo"
       ↓ Si
   Obtener rol → Generar JWT
       ↓
   200 + token + datos del usuario
```

**Nota de seguridad:** Cuando el usuario no existe y cuando la contrasena es incorrecta, el mensaje es **identico**: `"Credenciales invalidas"`. Esto evita que un atacante descubra si un correo esta registrado en el sistema.

### UserService.java — Logica del CRUD de Usuarios

**Archivo:** `services/UserService.java`

```java
private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
// Minimo 8 caracteres, al menos una mayuscula, una minuscula y un numero
private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
```

#### Crear usuario — `validateAndCreate()`

```java
public static JsonObject validateAndCreate(String nombre, String correo, String contrasena) {

    // 1. Validar campos obligatorios
    if (nombre vacio || correo vacio || contrasena vacia)
        → 400 "Nombre, correo y contraseña son requeridos"

    // 2. Validar formato de correo
    if (!correo.matches(EMAIL_REGEX))
        → 400 "El formato del correo no es valido"

    // 3. Validar complejidad de contrasena
    if (!contrasena.matches(PASSWORD_REGEX))
        → 400 "La contraseña debe tener minimo 8 caracteres, una mayuscula, una minuscula y un numero"

    // 4. Verificar correo duplicado
    if (UsuarioDAO.findByCorreo(correo) != null)
        → 409 "El correo ya esta registrado"

    // 5. Crear usuario con contrasena encriptada
    PasswordHelper.hashPassword(contrasena) → BCrypt hash
    UsuarioDAO.create(usuario) → INSERT en BD

    // 6. Asignar rol EMPLEADO
    RolDAO.findByNombre("EMPLEADO")
    UsuarioRolDAO.create(usuarioId, rolEmpleadoId)

    // 7. Generar JWT (auto-login) → 201 + token + datos
}
```

**¿Por que auto-login al registrar?** Para que el frontend pueda iniciar la sesion sin hacer un segundo request al servidor.

#### Actualizar completo — `validateAndUpdate()` (PUT)

```
1. Verificar que el usuario existe         → 404 si no
2. Validar nombre y correo obligatorios    → 400
3. Validar formato de correo               → 400
4. Si cambio el correo, verificar duplicado → 409
5. Actualizar nombre, correo, estado via UsuarioDAO.update()
6. Si envio contrasena, actualizarla via UsuarioDAO.updatePassword()
7. Retornar usuario actualizado            → 200
```

#### Actualizar parcial — `partialUpdate()` (PATCH)

```
1. Verificar que el usuario existe
2. Si envio correo, validar formato y verificar duplicado
3. Solo actualizar campos que NO son null
4. Si envio contrasena, actualizarla aparte
5. Retornar usuario actualizado → 200
```

**Diferencia entre PUT y PATCH:**

| Aspecto | PUT | PATCH |
|---------|-----|-------|
| Campos | Nombre y correo OBLIGATORIOS | Todos opcionales |
| Estado | `boolean estado` (primitivo) | `Boolean estado` (objeto, puede ser `null`) |
| Logica | Reemplaza TODOS los campos | Solo modifica los enviados |

**¿Por que `Boolean` (objeto) en PATCH y `boolean` (primitivo) en PUT?**

`boolean` solo puede ser `true` o `false`. `Boolean` puede ser `true`, `false` o `null`. En PATCH necesitamos saber si el frontend envio el campo o no: si envio `estado: false` → actualizamos; si no envio `estado` → lo dejamos como esta.

### GoogleAuthService.java — Logica del Login con Google

**Archivo:** `services/GoogleAuthService.java`

El login con Google no verifica contrasena. En su lugar, verifica que el token de Google sea autentico y fue emitido para nuestra aplicacion.

**Flujo completo:**

```
POST /api/auth/google  { "credential": "<id-token-de-google>" }
  │
  ├─ ¿Token vacio?                               → 400
  │
  ├─ Llama a la API de Google:
  │    GET https://oauth2.googleapis.com/tokeninfo?id_token=<token>
  │    Si Google responde != 200                 → 401 "Token invalido o expirado"
  │
  ├─ ¿datosGoogle.aud == GOOGLE_CLIENT_ID?
  │    No → 401 "Token no autorizado para esta aplicacion"
  │    (Evita que un token de otra app de Google funcione en la nuestra)
  │
  ├─ ¿datosGoogle.email_verified == "true"?
  │    No → 401 "El correo de Google no esta verificado"
  │
  ├─ Extrae: googleId (campo "sub"), correo, nombre
  │
  ├─ UsuarioDAO.findByGoogleId(googleId)
  │    ├─ Encontrado → usar ese usuario directamente
  │    └─ No encontrado → UsuarioDAO.findByCorreo(correo)
  │         ├─ Encontrado (cuenta manual con ese correo)
  │         │    → UsuarioDAO.linkGoogleId() → vincula el google_id
  │         └─ No encontrado
  │              → UsuarioDAO.createWithGoogle() → crea cuenta nueva
  │              → UsuarioRolDAO.create() → asigna rol EMPLEADO
  │
  ├─ ¿usuario.estado == false?                   → 403
  │
  └─ JwtHelper.generateToken()
       → 200 { token, nombre, correo, rol }
```

**El caso de vinculacion:** Si alguien tiene cuenta manual con `correo = juan@gmail.com` y despues inicia sesion con Google usando ese mismo correo, el sistema detecta la cuenta existente y le agrega el `google_id`. A partir de ese momento puede autenticarse de ambas formas.

### PasswordResetService.java — Logica de Recuperacion de Contrasena

**Archivo:** `services/PasswordResetService.java`

El flujo de recuperacion tiene tres etapas:

**Etapa 1 — Solicitar recuperacion:**
```
UsuarioDAO.findByCorreo(correo)
  ├─ ¿No existe?
  │    → Respuesta GENERICA exitosa (no revela si el correo existe)
  │      "Si el correo esta registrado, recibiras un enlace en breve"
  │
  ├─ ¿usuario.contrasena == null? (cuenta Google)
  │    → 400 "Esta cuenta usa inicio de sesion con Google"
  │
  ├─ ¿usuario.estado == false?
  │    → 403 "Usuario inactivo"
  │
  └─ TokenRecuperacionDAO.guardarToken(usuarioId, ahora + 1 hora)
       Genera UUID aleatorio, lo guarda en BD con expiracion
       → EmailService.enviarCorreoRecuperacion(correo, token)
```

**¿Por que la respuesta generica cuando el correo no existe?**
Si respondiéramos "ese correo no esta registrado", un atacante podria usar este endpoint para averiguar que correos tienen cuenta en el sistema (enumeracion de usuarios).

**Etapa 2 — Validar token:**
```
TokenRecuperacionDAO.validarToken(token)
  ├─ ¿No existe o ya fue usado?   → 400 "Token invalido o ya utilizado"
  ├─ ¿Fecha actual > expiracion?  → 400 "El token ha expirado"
  └─ Token valido                 → 200 { idToken, usuarioId }
```

**Etapa 3 — Cambiar contrasena:**
```
├─ ¿Token o contrasena vacios?        → 400
├─ ¿Contrasena no cumple PASSWORD_REGEX? → 400
├─ TokenRecuperacionDAO.validarToken() (segunda verificacion)
├─ PasswordHelper.hashPassword(nuevaContrasena)
├─ TokenRecuperacionDAO.actualizarContrasena(usuarioId, hash)
└─ TokenRecuperacionDAO.marcarTokenUsado(idToken)
     → El token queda inutilizable para siempre
```

**¿Por que validar el token dos veces?** Porque entre la validacion y el cambio podria pasar tiempo. El token se valida nuevamente antes de actualizar para garantizar que no expiro en ese intervalo.

### EmailService.java — Envio de Correos

**Archivo:** `services/EmailService.java`

Usa SMTP de Gmail con `javax.mail`. Requiere `EMAIL_USER` y `EMAIL_PASS` en el `.env`.

```java
// Configuracion SMTP de Gmail
propiedades.put("mail.smtp.host", "smtp.gmail.com");
propiedades.put("mail.smtp.port", "587");
propiedades.put("mail.smtp.auth", "true");
propiedades.put("mail.smtp.starttls.enable", "true");
```

El metodo `enviarCorreoRecuperacion(correo, token)` construye un email HTML con un boton que apunta a:
```
http://localhost:5500/reset-password.html?token=<uuid>
```

**Si el envio de correo falla** (red, credenciales incorrectas, etc.), el link se imprime en consola del servidor para no bloquear el flujo de desarrollo:
```
==============================
LINK RECUPERACION (fallo email):
http://localhost:5500/reset-password.html?token=abc-123-...
==============================
```

---

## Paso 15: Controllers — Recepcion de peticiones HTTP

Los Controllers son el **punto de entrada** de cada peticion. Su responsabilidad es simple:
1. Leer los datos de la peticion (body JSON, parametros de URL)
2. Delegar al Service correspondiente
3. Enviar la respuesta al frontend

**Patron comun de todos los controllers:**
```java
public static HttpHandler nombreEndpoint() {
    return exchange -> {
        // 1. Leer body
        ApiRequest request = new ApiRequest(exchange);
        String body = request.readBody();
        if (body.isEmpty()) { ApiResponse.error(...400...); return; }

        // 2. Parsear JSON
        JsonObject json = new Gson().fromJson(body, JsonObject.class);

        // 3. Extraer campos
        String campo = json.has("campo") ? json.get("campo").getAsString() : "";

        // 4. Delegar al Service
        JsonObject response = AlgunService.metodo(campo);

        // 5. Extraer status code interno y enviar respuesta
        int code = response.get("status").getAsInt();
        response.remove("status");
        ApiResponse.send(exchange, response.toString(), code);
    };
}
```

**¿Por que se hace `response.remove("status")`?**
El Service agrega un campo `"status"` al JsonObject para indicar el codigo HTTP internamente. Lo extraemos, lo usamos como status code de la respuesta HTTP, y lo removemos del JSON que se envia al frontend.

### AuthController.java

#### `login()` — `POST /api/auth/login`

Lee body → Extrae correo y contrasena → Llama a `AuthService.validateLogin()` → Retorna token JWT

#### `me()` — `GET /api/auth/me` (ruta protegida)

El middleware ya valido el token y puso los datos en el exchange. El controller solo los lee:
```java
String userId = (String) exchange.getAttribute("userId");
String correo = (String) exchange.getAttribute("correo");
String rol    = (String) exchange.getAttribute("rol");
```
No consulta la base de datos. Los datos vienen del propio token JWT.

### GoogleAuthController.java

#### `loginWithGoogle()` — `POST /api/auth/google`

Lee el campo `"credential"` del body → Llama a `GoogleAuthService.loginWithGoogle()` → Retorna token JWT

### PasswordResetController.java

#### `solicitarRecuperacion()` — `POST /api/auth/forgot-password`
Lee `"correo"` del body → Llama a `PasswordResetService.solicitarRecuperacion()`

#### `validarToken()` — `GET /api/auth/reset-password/validate?token=X`
Lee el token del query param (no del body, porque es un GET) → Llama a `PasswordResetService.validarToken()`

#### `cambiarContrasena()` — `POST /api/auth/reset-password`
Lee `"token"` y `"contrasena"` del body → Llama a `PasswordResetService.cambiarContrasena()`

### UserController.java

#### `listAll()` — `GET /api/users`
```java
List<Usuario> lista = UsuarioDAO.findAll();
lista.forEach(u -> u.setContrasena(null));  // Ocultar contrasenas
ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
```

#### `getById()` — `GET /api/users/id?id=5`

Ademas de buscar el usuario, verifica la restriccion de recurso propio:
```java
String rolUsuario    = (String) exchange.getAttribute("rol");
String idUsuarioToken = (String) exchange.getAttribute("userId");

if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
    ApiResponse.error(exchange, 403, "No tiene permiso para acceder a este recurso");
    return;
}
```
Un `EMPLEADO` puede ver solo su propio perfil (`?id=5` cuando su token dice `userId=5`), no el de otros usuarios.

#### `create()`, `update()`, `patch()` — Siguen el patron estandar

`update()` y `patch()` tambien aplican la misma restriccion de recurso propio para EMPLEADO.

**Las contrasenas nunca se devuelven:**
```java
usuario.setContrasena(null);  // Se limpia antes de serializar a JSON
```

---

## Paso 16: Middleware de Autenticacion

**Archivo:** `middlewares/AuthMiddleware.java`

El middleware es una capa que se ejecuta **ANTES** del controller. Se usa para proteger rutas que requieren autenticacion.

```java
public HttpHandler protect(HttpHandler next, String... rolesPermitidos) {
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

            // 3. Guardar datos del usuario en el exchange (para que el controller los lea)
            exchange.setAttribute("userId", claims.getSubject());
            exchange.setAttribute("correo", claims.get("correo", String.class));
            exchange.setAttribute("rol", claims.get("rol", String.class));

            // 4. Si se especificaron roles, verificar autorizacion
            if (rolesPermitidos.length > 0) {
                String rolUsuario = claims.get("rol", String.class);
                boolean autorizado = false;
                for (String rol : rolesPermitidos) {
                    if (rol.equalsIgnoreCase(rolUsuario)) {
                        autorizado = true;
                        break;
                    }
                }
                if (!autorizado) {
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
```

**Flujo de ejecucion:**

```
Peticion llega con Header: "Authorization: Bearer eyJhbG..."
       ↓
   ¿Es peticion OPTIONS?          → Si → responde 204 CORS y termina
       ↓ No
   ¿Header Authorization existe?  → No → 401 "Token de autenticacion requerido"
       ↓ Si
   ¿Token valido?   → Expiro      → 401 "Token expirado. Inicie sesion nuevamente"
                    → Invalido    → 401 "Token invalido"
       ↓ Valido
   Guarda userId, correo, rol en exchange
       ↓
   ¿Se especificaron roles?
       ↓ Si                        ↓ No
   ¿Rol del usuario esta           Ejecutar handler (controller)
    en la lista permitida?
       ↓ No            ↓ Si
   403 "No tiene       Ejecutar handler (controller)
    permiso"
```

**¿Por que distinguir token expirado de token invalido?**
El frontend necesita reaccionar diferente: si el token expiro, debe redirigir al login con mensaje "sesion expirada". Si es invalido (manipulado), es un caso de seguridad mas serio.

**¿Como se usa en Routes.java?**
```java
auth.protect(handler)                              // Solo JWT valido (cualquier rol)
auth.protect(handler, "SUPER_ADMIN", "ADMIN")      // JWT valido + rol especifico
auth.protect(handler, "SUPER_ADMIN", "ADMIN", "EMPLEADO") // Los tres roles
```

---

## Paso 17: Punto de entrada — Main.java

**Archivo:** `Main.java`

Es el archivo que arranca todo el sistema:

```java
package com.backend;

import com.backend.seeders.*;
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
   ├── Roles: SUPER_ADMIN, ADMIN, EMPLEADO
   ├── Permisos: 10 permisos del sistema
   ├── Tipos de movimiento: Venta (Ingreso), Compra (Egreso), Gasto (Egreso)
   └── Tipos de gasto: Transporte, Impuestos, Almacenamiento, Embalaje, Otros
3. serverConnection.startServer(8080) crea el HttpServer
4. Routes.configureRoutes() registra todas las rutas en el Router
5. El servidor inicia en http://localhost:8080
6. Listo para recibir peticiones del frontend
```

---

## Flujos completos (de principio a fin)

### Flujo: Login con correo y contrasena

```
Frontend envia POST /api/auth/login
Body: {"correo":"david@mail.com","contrasena":"MiClave1"}
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
    ├── Formato correo OK
    ├── UsuarioDAO.findByCorreo("david@mail.com") → Usuario encontrado
    ├── usuario.getContrasena() != null → no es cuenta Google
    ├── PasswordHelper.checkPassword("MiClave1", "$2a$12$...") → true
    ├── usuario.isEstado() → true (activo)
    ├── UsuarioDAO.findRolByUsuarioId(5) → "ADMIN"
    └── JwtHelper.generateToken(5, "david@mail.com", "ADMIN") → "eyJhbG..."
    ↓
Frontend recibe: 200 {"success":true,"token":"eyJhbG...","nombre":"David","rol":"ADMIN"}
```

### Flujo: Login con Google

```
Frontend envia POST /api/auth/google
Body: {"credential": "<id-token-de-google>"}
    ↓
GoogleAuthController → GoogleAuthService.loginWithGoogle()
    ↓
Service:
    ├── GET https://oauth2.googleapis.com/tokeninfo?id_token=<token>
    │    → Google responde: {sub:"12345", email:"david@gmail.com", email_verified:"true", aud:"..."}
    ├── aud == GOOGLE_CLIENT_ID → OK
    ├── email_verified == "true" → OK
    ├── UsuarioDAO.findByGoogleId("12345") → null (primera vez)
    ├── UsuarioDAO.findByCorreo("david@gmail.com") → null (tampoco existe)
    ├── UsuarioDAO.createWithGoogle("12345", "David", "david@gmail.com") → nuevo usuario
    ├── UsuarioRolDAO.create(nuevoId, rolEmpleadoId) → asigna EMPLEADO
    └── JwtHelper.generateToken(...) → "eyJhbG..."
    ↓
Frontend recibe: 200 {"success":true,"token":"eyJhbG...","rol":"EMPLEADO"}
```

### Flujo: Ruta protegida con rol

```
Frontend envia GET /api/users
Header: "Authorization: Bearer eyJhbG..."
    ↓
Router → auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN")
    ↓
AuthMiddleware.protect():
    ├── Extrae token del header
    ├── JwtHelper.validateToken("eyJhbG...") → Claims: {sub:"5", rol:"EMPLEADO"}
    ├── Guarda userId, correo, rol en exchange
    ├── ¿"EMPLEADO" esta en ["SUPER_ADMIN", "ADMIN"]? → NO
    └── ApiResponse.error(403, "No tiene permiso para esta accion")
    ↓
Frontend recibe: 403 {"success":false,"message":"No tiene permiso para esta accion"}
```

### Flujo: Recuperacion de contrasena (los 3 pasos)

```
PASO 1 — Solicitar:
Frontend → POST /api/auth/forgot-password  {"correo":"david@mail.com"}
    ↓ PasswordResetService:
    ├── UsuarioDAO.findByCorreo → usuario encontrado, activo, con contrasena
    ├── TokenRecuperacionDAO.guardarToken(5, ahora+1hora) → UUID generado
    ├── EmailService.enviarCorreoRecuperacion("david@mail.com", uuid)
    └── 200 "Si el correo esta registrado, recibiras un enlace en breve"

PASO 2 — Validar token:
Frontend → GET /api/auth/reset-password/validate?token=abc-123
    ↓ PasswordResetService:
    ├── TokenRecuperacionDAO.validarToken("abc-123")
    │    Token encontrado, no usado, no expirado
    └── 200 "Token valido"

PASO 3 — Cambiar contrasena:
Frontend → POST /api/auth/reset-password  {"token":"abc-123","contrasena":"NuevaClave1"}
    ↓ PasswordResetService:
    ├── PASSWORD_REGEX OK
    ├── TokenRecuperacionDAO.validarToken("abc-123") → segunda validacion
    ├── PasswordHelper.hashPassword("NuevaClave1") → nuevo hash BCrypt
    ├── TokenRecuperacionDAO.actualizarContrasena(5, nuevoHash)
    ├── TokenRecuperacionDAO.marcarTokenUsado(idToken)
    └── 200 "Contrasena actualizada correctamente"
```

---

## Endpoints del sistema

| Metodo | Ruta | Acceso | Descripcion |
|--------|------|--------|-------------|
| POST | `/api/auth/login` | Publico | Iniciar sesion con correo y contrasena |
| POST | `/api/auth/google` | Publico | Iniciar sesion con Google |
| GET | `/api/auth/me` | JWT valido | Obtener datos del usuario autenticado |
| POST | `/api/auth/forgot-password` | Publico | Solicitar recuperacion de contrasena |
| GET | `/api/auth/reset-password/validate` | Publico | Validar token de recuperacion |
| POST | `/api/auth/reset-password` | Publico | Cambiar contrasena con token |
| GET | `/api/users` | SUPER_ADMIN, ADMIN | Listar todos los usuarios |
| POST | `/api/users` | SUPER_ADMIN, ADMIN | Crear un usuario nuevo |
| GET | `/api/users/id?id=X` | SUPER_ADMIN, ADMIN, EMPLEADO* | Obtener usuario por ID |
| PUT | `/api/users/id?id=X` | SUPER_ADMIN, ADMIN, EMPLEADO* | Actualizar usuario completo |
| PATCH | `/api/users/id?id=X` | SUPER_ADMIN, ADMIN, EMPLEADO* | Actualizar usuario parcialmente |

> *EMPLEADO solo puede acceder o modificar su propio perfil (cuando `?id=X` coincide con su `userId` del token).

---

## Codigos de respuesta HTTP

| Codigo | Significado en este sistema |
|--------|----------------------------|
| 200 | Operacion exitosa |
| 201 | Recurso creado exitosamente |
| 204 | Respuesta a preflight CORS (OPTIONS) |
| 400 | Error de validacion (campos faltantes, formato incorrecto, token invalido/expirado) |
| 401 | No autenticado (credenciales invalidas, token ausente/expirado/invalido) |
| 403 | Sin permisos (rol insuficiente, usuario inactivo, recurso ajeno) |
| 404 | Recurso no encontrado |
| 409 | Conflicto (correo duplicado) |
| 500 | Error interno del servidor |

---

## Roles del sistema

| Rol | Descripcion | Quien lo usa |
|-----|-------------|-------------|
| `SUPER_ADMIN` | Acceso total incluyendo configuracion tecnica | Desarrollador |
| `ADMIN` | Gestion completa del negocio | Dueno del negocio |
| `EMPLEADO` | Acceso operativo limitado | Empleados de la tienda |

Los usuarios nuevos (manuales o de Google) reciben el rol `EMPLEADO` por defecto.

---

## Como ejecutar el proyecto

```bash
# 1. Crear la base de datos
#    Abrir MySQL y ejecutar el script db/UrbanLife.sql

# 2. Crear el archivo .env en la raiz del proyecto con:
#    DB_URL=jdbc:mysql://localhost:3306/UrbanLife
#    DB_USER=root
#    DB_PASSWD=tu_contrasena
#    JWT_SECRET=clave_secreta_de_al_menos_32_caracteres
#    GOOGLE_CLIENT_ID=tu_client_id_de_google
#    EMAIL_USER=tu_correo@gmail.com
#    EMAIL_PASS=tu_contrasena_de_aplicacion

# 3. Compilar y ejecutar
mvn clean compile exec:java
```

El servidor iniciara en `http://localhost:8080` y los seeders insertaran los datos iniciales automaticamente.
