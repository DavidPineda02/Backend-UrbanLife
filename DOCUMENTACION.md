# Documentación Backend UrbanLife

## Arquitectura del Proyecto

Este backend está construido con **Java puro** usando el servidor HTTP nativo (`com.sun.net.httpserver.HttpServer`), sin frameworks como Spring Boot. La arquitectura sigue un patrón de capas:

```
Petición HTTP → Router → Controller → Service → DAO → Base de Datos
```

### Estructura de carpetas

```
com.backend/
├── Main.java                          # Punto de entrada
├── config/
│   └── dbConnection.java             # Conexión a MySQL
├── server/
│   ├── serverConnection.java         # Configuración del servidor HTTP
│   └── http/
│       ├── ApiRequest.java           # Utilidad para leer peticiones
│       └── ApiResponse.java          # Utilidad para enviar respuestas JSON
├── routes/
│   ├── Router.java                   # Motor de ruteo (Map-based)
│   └── Routes.java                   # Configuración centralizada de rutas
├── controllers/
│   ├── UserController.java           # Handlers del CRUD de usuarios
│   └── AuthController.java           # Handlers de autenticación
├── services/
│   ├── UserService.java              # Lógica de negocio de usuarios
│   └── AuthService.java              # Lógica de negocio de login
├── dao/
│   └── UsuarioDAO.java               # Acceso a base de datos (JDBC)
├── middlewares/
│   └── AuthMiddleware.java           # Validación JWT y control de roles
├── models/
│   └── Usuario.java                  # Modelo de datos
└── helpers/
    ├── JwtHelper.java                # Generación y validación de tokens JWT
    └── PasswordHelper.java           # Hashing de contraseñas con BCrypt
```

---

## Paso 1: Punto de entrada — Main.java

```java
public class Main {
    public static void main(String[] args) {
        serverConnection.startServer(8080);
    }
}
```

**¿Qué hace?** Inicia el servidor HTTP en el puerto 8080. Es la clase que se ejecuta al correr el proyecto.

---

## Paso 2: Servidor HTTP — serverConnection.java

```java
public static void startServer(int port) {
    server = HttpServer.create(new InetSocketAddress(port), 0);

    Routes routes = new Routes();
    HttpHandler router = routes.configureRoutes();

    server.createContext("/", router);
    server.setExecutor(null);
    server.start();
}
```

**¿Qué hace?**
1. Crea un servidor HTTP nativo de Java en el puerto indicado.
2. Instancia `Routes` y obtiene el `Router` con todas las rutas configuradas.
3. Registra un único contexto `"/"` que delega **todas** las peticiones al Router.
4. `setExecutor(null)` usa el executor por defecto (un hilo por petición).

---

## Paso 3: Motor de Ruteo — Router.java

```java
public class Router implements HttpHandler {

    Map<String, Map<String, HttpHandler>> routes = new HashMap<>();

    public void addRoute(String method, String path, HttpHandler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public void get(String path, HttpHandler handler)    { addRoute("GET", path, handler); }
    public void post(String path, HttpHandler handler)   { addRoute("POST", path, handler); }
    public void put(String path, HttpHandler handler)    { addRoute("PUT", path, handler); }
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

**¿Qué hace?**
- Usa un `Map<String, Map<String, HttpHandler>>` donde:
  - La **clave exterior** es el método HTTP (`GET`, `POST`, `PUT`, `DELETE`).
  - La **clave interior** es la ruta (`/api/users`, `/api/auth/login`).
  - El **valor** es el handler (función lambda) que procesa la petición.
- Cuando llega una petición, busca primero por método y luego por ruta.
- Si no encuentra la ruta, responde con error 404.
- Las peticiones `OPTIONS` se manejan automáticamente para CORS.

**Ejemplo visual del Map:**
```
{
  "GET": {
    "/api/users"    → UserController.listAll(),
    "/api/users/id" → UserController.getById(),
    "/api/auth/me"  → AuthMiddleware.protect(AuthController.me())
  },
  "POST": {
    "/api/users"         → UserController.create(),
    "/api/auth/login"    → AuthController.login()
  },
  "PUT": {
    "/api/users/id" → UserController.update()
  },
  "DELETE": {
    "/api/users/id" → UserController.delete()
  }
}
```

---

## Paso 4: Configuración de Rutas — Routes.java

```java
public class Routes {

    Router router = new Router();

    public HttpHandler configureRoutes() {
        AuthMiddleware auth = new AuthMiddleware();

        // Rutas de autenticación
        router.post("/api/auth/login", AuthController.login());
        router.get("/api/auth/me", auth.protect(AuthController.me()));

        // Rutas CRUD de usuarios
        router.get("/api/users", UserController.listAll());
        router.post("/api/users", UserController.create());
        router.get("/api/users/id", UserController.getById());
        router.put("/api/users/id", UserController.update());
        router.delete("/api/users/id", UserController.delete());

        return router;
    }
}
```

**¿Qué hace?**
- Es el archivo central donde se definen **todas** las rutas de la aplicación.
- Cada línea asocia un **método HTTP** + **ruta** con un **handler** del controller.
- Para rutas protegidas, se envuelve el handler con `auth.protect()`.
- Retorna el router como `HttpHandler` para que el servidor lo use.

---

## Paso 5: Utilidades HTTP — ApiRequest.java y ApiResponse.java

### ApiRequest.java — Leer el cuerpo de la petición

```java
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

**¿Qué hace?** Envuelve el `HttpExchange` y proporciona un método sencillo para leer el body de la petición como String (usado en POST y PUT).

### ApiResponse.java — Enviar respuestas JSON

```java
public class ApiResponse {

    // Enviar respuesta JSON genérica
    public static void send(HttpExchange exchange, String body, int statusCode);

    // Enviar objeto Java como JSON (usa Gson)
    public static void sendJson(HttpExchange exchange, int statusCode, Object data);

    // Respuesta de éxito rápida
    public static void success(HttpExchange exchange, String message);

    // Respuesta de error rápida
    public static void error(HttpExchange exchange, int code, String message);

    // Manejar peticiones CORS (OPTIONS)
    public static void handleCors(HttpExchange exchange);
}
```

**¿Qué hace?**
- Todos los métodos son **estáticos** — se llaman directamente sin crear instancias.
- Configura automáticamente los headers:
  - `Content-Type: application/json; charset=UTF-8`
  - Headers CORS (`Access-Control-Allow-Origin: *`, etc.)
- Convierte objetos Java a JSON usando la librería **Gson**.

---

## Paso 6: Controladores (Controllers)

Los controladores son clases con **métodos estáticos** que retornan `HttpHandler` (lambdas). Cada método es un handler independiente para una operación específica.

### UserController.java — CRUD de Usuarios

#### Listar todos los usuarios — `GET /api/users`

```java
public static HttpHandler listAll() {
    return exchange -> {
        List<Usuario> lista = UsuarioDAO.findAll();       // Consulta a BD
        lista.forEach(u -> u.setContrasena(null));         // Ocultar contraseña
        ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
    };
}
```

**Flujo:** Petición → DAO consulta BD → Se ocultan contraseñas → Respuesta JSON

#### Obtener usuario por ID — `GET /api/users/id?id=5`

```java
public static HttpHandler getById() {
    return exchange -> {
        String query = exchange.getRequestURI().getQuery();  // "id=5"
        // Validar que venga el parámetro id
        if (query == null || !query.matches("id=\\d+")) {
            ApiResponse.error(exchange, 400, "Parametro id requerido");
            return;
        }
        int id = Integer.parseInt(query.split("=")[1]);     // Extraer el número

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

**Flujo:** Se extrae el `id` del query string → DAO busca en BD → Respuesta JSON

#### Crear usuario — `POST /api/users`

```java
public static HttpHandler create() {
    return exchange -> {
        ApiRequest request = new ApiRequest(exchange);
        String body = request.readBody();                    // Leer el body JSON

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);  // Parsear JSON

        // Extraer campos del JSON
        String nombre = json.has("nombre") ? json.get("nombre").getAsString() : "";
        String correo = json.has("correo") ? json.get("correo").getAsString() : "";
        String contrasena = json.has("contrasena") ? json.get("contrasena").getAsString() : "";
        String estado = json.has("estado") ? json.get("estado").getAsString() : "Activo";

        // Delegar al servicio (validación + creación)
        JsonObject response = UserService.validateAndCreate(nombre, correo, contrasena, estado);
        int code = response.get("status").getAsInt();
        response.remove("status");

        ApiResponse.send(exchange, response.toString(), code);
    };
}
```

**Flujo:** Leer body → Parsear JSON → Extraer campos → Service valida y crea → Respuesta

#### Actualizar usuario — `PUT /api/users/id?id=5`

```java
public static HttpHandler update() {
    return exchange -> {
        // 1. Extraer ID del query string
        // 2. Leer y parsear body JSON
        // 3. Extraer campos (nombre, correo, contrasena, estado)
        // 4. Delegar al servicio: UserService.validateAndUpdate(id, nombre, correo, ...)
        // 5. Enviar respuesta JSON
    };
}
```

#### Eliminar usuario — `DELETE /api/users/id?id=5`

```java
public static HttpHandler delete() {
    return exchange -> {
        // 1. Extraer ID del query string
        // 2. Delegar al servicio: UserService.deleteUser(id)
        // 3. Enviar respuesta JSON
    };
}
```

### AuthController.java — Autenticación

#### Login — `POST /api/auth/login`

```java
public static HttpHandler login() {
    return exchange -> {
        ApiRequest request = new ApiRequest(exchange);
        String body = request.readBody();

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

**Flujo:** Leer body → Extraer correo y contraseña → AuthService valida credenciales → Retorna token JWT

#### Obtener usuario autenticado — `GET /api/auth/me`

```java
public static HttpHandler me() {
    return exchange -> {
        // Los datos vienen del middleware (ya validó el token)
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

**Flujo:** Esta ruta está protegida con middleware → El middleware ya validó el token y puso los datos en `exchange.getAttribute()` → El controller solo los lee y responde.

---

## Paso 7: Servicios (Services)

Los servicios contienen la **lógica de negocio**: validaciones, reglas, y coordinación entre capas. Todos los métodos son **estáticos** y retornan `JsonObject`.

### UserService.java

#### Crear usuario — `validateAndCreate()`

```java
public static JsonObject validateAndCreate(String nombre, String correo, 
                                            String contrasena, String estado) {
    // 1. Validar que nombre, correo y contraseña no estén vacíos → 400
    // 2. Verificar que el correo no esté registrado → 409
    // 3. Hashear la contraseña con BCrypt
    // 4. Crear el objeto Usuario y guardarlo en BD via DAO
    // 5. Retornar JsonObject con success, message, data y status
}
```

**Validaciones:**
| Caso | Código HTTP | Mensaje |
|------|-------------|---------|
| Campos vacíos | 400 | "Nombre, correo y contraseña son requeridos" |
| Correo duplicado | 409 | "El correo ya está registrado" |
| Error de BD | 500 | "Error al crear el usuario" |
| Éxito | 201 | "Usuario creado exitosamente" |

#### Actualizar usuario — `validateAndUpdate()`

```java
public static JsonObject validateAndUpdate(int id, String nombre, String correo,
                                            String contrasena, String estado) {
    // 1. Buscar usuario por ID → 404 si no existe
    // 2. Validar que al menos nombre o correo se envíen → 400
    // 3. Si cambia el correo, verificar que no esté en uso → 409
    // 4. Actualizar campos no vacíos
    // 5. Si se envió contraseña, hashear y actualizar
    // 6. Retornar usuario actualizado
}
```

#### Eliminar usuario — `deleteUser()`

```java
public static JsonObject deleteUser(int id) {
    // 1. Verificar que el usuario existe → 404
    // 2. Eliminar de BD via DAO
    // 3. Retornar resultado
}
```

### AuthService.java

#### Validar login — `validateLogin()`

```java
public static JsonObject validateLogin(String correo, String contrasena) {
    // 1. Validar que correo y contraseña no estén vacíos → 400
    // 2. Buscar usuario por correo en BD → 401 si no existe
    // 3. Comparar contraseña con hash BCrypt → 401 si no coincide
    // 4. Verificar estado "Activo" → 403 si está inactivo
    // 5. Obtener rol del usuario desde BD
    // 6. Generar token JWT con (id, correo, rol)
    // 7. Retornar token + datos del usuario
}
```

**Flujo de autenticación:**
```
correo + contraseña
       ↓
   ¿Campos vacíos? → 400
       ↓
   ¿Usuario existe en BD? → 401
       ↓
   ¿Contraseña coincide (BCrypt)? → 401
       ↓
   ¿Estado es "Activo"? → 403
       ↓
   Obtener rol → Generar JWT → 200 + token
```

---

## Paso 8: Capa de Acceso a Datos (DAO)

### UsuarioDAO.java

Todos los métodos son **estáticos** y usan **JDBC** para ejecutar SQL directamente contra MySQL.

```java
public class UsuarioDAO {

    // Buscar por correo (usado en login y validación de duplicados)
    public static Usuario findByCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        // PreparedStatement → executeQuery → mapRow
    }

    // Buscar por ID
    public static Usuario findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
    }

    // Listar todos
    public static List<Usuario> findAll() {
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
    }

    // Crear usuario (retorna el usuario con ID generado)
    public static Usuario create(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, estado) VALUES (?, ?, ?, ?)";
        // Statement.RETURN_GENERATED_KEYS para obtener el ID auto-generado
    }

    // Actualizar datos básicos (nombre, correo, estado)
    public static boolean update(Usuario u) {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, estado = ? WHERE id_usuario = ?";
    }

    // Actualizar contraseña por separado
    public static boolean updatePassword(int id, String hashedPassword) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
    }

    // Eliminar usuario
    public static boolean delete(int id) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
    }

    // Obtener el rol del usuario (JOIN con tabla roles)
    public static String findRolByUsuarioId(int usuarioId) {
        String sql = "SELECT r.nombre FROM roles r " +
                     "INNER JOIN usuarios_roles ur ON r.id_roles = ur.rol_id " +
                     "WHERE ur.usuario_id = ? LIMIT 1";
    }

    // Mapear ResultSet a objeto Usuario
    private static Usuario mapRow(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id_usuario"),
            rs.getString("nombre"),
            rs.getString("correo"),
            rs.getString("contrasena"),
            rs.getString("estado")
        );
    }
}
```

**Patrón de cada método:**
1. Definir el SQL con `?` como placeholders.
2. Usar `try-with-resources` para conexión y PreparedStatement (se cierran automáticamente).
3. Setear parámetros con `stmt.setString()`, `stmt.setInt()`, etc.
4. Ejecutar query y mapear resultado.
5. Capturar excepciones SQL y retornar null/false.

---

## Paso 9: Middleware de Autenticación

### AuthMiddleware.java

```java
public class AuthMiddleware {

    public HttpHandler protect(HttpHandler next, String... allowedRoles) {
        return exchange -> {
            // 1. Obtener header "Authorization: Bearer <token>"
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            // 2. Validar que el header exista y tenga formato correcto
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ApiResponse.error(exchange, 401, "Token de autenticacion requerido");
                return;
            }

            // 3. Extraer y validar el token JWT
            Claims claims = JwtHelper.validateToken(authHeader.substring(7));

            // 4. Poner datos del usuario en el exchange (para que el controller los lea)
            exchange.setAttribute("userId", claims.getSubject());
            exchange.setAttribute("correo", claims.get("correo", String.class));
            exchange.setAttribute("rol", claims.get("rol", String.class));

            // 5. Si se especificaron roles, verificar autorización
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
                    ApiResponse.error(exchange, 403, "No tiene permiso");
                    return;
                }
            }

            // 6. Si todo está bien, ejecutar el handler original
            next.handle(exchange);
        };
    }
}
```

**¿Cómo se usa?**
```java
// Sin restricción de rol (cualquier usuario autenticado)
auth.protect(AuthController.me())

// Solo Admin puede acceder
auth.protect(UserController.delete(), "Admin")

// Admin o Premium pueden acceder
auth.protect(UserController.create(), "Admin", "Premium")
```

---

## Paso 10: Helpers

### JwtHelper.java — Tokens JWT
- `generateToken(int userId, String correo, String rol)` → Genera un token firmado con HMAC-SHA256 que expira en cierto tiempo.
- `validateToken(String token)` → Valida el token y retorna los Claims (datos del usuario). Lanza excepciones si el token es inválido o expiró.

### PasswordHelper.java — Contraseñas seguras
- `hashPassword(String password)` → Usa **BCrypt** para generar un hash seguro de la contraseña.
- `checkPassword(String password, String hashed)` → Compara una contraseña en texto plano contra su hash BCrypt. Retorna `true` si coinciden.

---

## Flujos completos

### Flujo: Crear un usuario

```
Cliente envía POST /api/users con JSON: {"nombre":"David", "correo":"d@mail.com", "contrasena":"123"}
    ↓
Router busca: routes["POST"]["/api/users"] → UserController.create()
    ↓
Controller: Lee body → Parsea JSON → Extrae campos
    ↓
Service: validateAndCreate("David", "d@mail.com", "123", "Activo")
    ↓
    ├── ¿Campos vacíos? → No → Continuar
    ├── ¿Correo existe? → UsuarioDAO.findByCorreo("d@mail.com") → null → No existe → Continuar
    ├── Hashear contraseña: BCrypt.hashpw("123") → "$2a$10$..."
    └── UsuarioDAO.create(nuevo) → INSERT INTO usuarios → Retorna usuario con ID
    ↓
Controller: Recibe JsonObject → Extrae status code → Envía respuesta
    ↓
Cliente recibe: 201 {"success":true, "message":"Usuario creado", "data":{...}}
```

### Flujo: Login

```
Cliente envía POST /api/auth/login con JSON: {"correo":"d@mail.com", "contrasena":"123"}
    ↓
Router: routes["POST"]["/api/auth/login"] → AuthController.login()
    ↓
Controller: Lee body → Parsea JSON → Extrae correo y contrasena
    ↓
Service: AuthService.validateLogin("d@mail.com", "123")
    ↓
    ├── UsuarioDAO.findByCorreo("d@mail.com") → Usuario encontrado
    ├── BCrypt.checkpw("123", "$2a$10$...") → true → Coincide
    ├── Estado es "Activo" → OK
    ├── UsuarioDAO.findRolByUsuarioId(1) → "Admin"
    └── JwtHelper.generateToken(1, "d@mail.com", "Admin") → "eyJhbG..."
    ↓
Cliente recibe: 200 {"success":true, "token":"eyJhbG...", "nombre":"David", "rol":"Admin"}
```

### Flujo: Ruta protegida (GET /api/auth/me)

```
Cliente envía GET /api/auth/me con Header: "Authorization: Bearer eyJhbG..."
    ↓
Router: routes["GET"]["/api/auth/me"] → auth.protect(AuthController.me())
    ↓
Middleware:
    ├── Extrae token del header
    ├── JwtHelper.validateToken("eyJhbG...") → Claims: {sub:"1", correo:"d@mail.com", rol:"Admin"}
    ├── exchange.setAttribute("userId", "1")
    ├── exchange.setAttribute("correo", "d@mail.com")
    ├── exchange.setAttribute("rol", "Admin")
    └── next.handle(exchange) → Pasa al controller
    ↓
AuthController.me():
    ├── Lee exchange.getAttribute("userId") → "1"
    ├── Lee exchange.getAttribute("correo") → "d@mail.com"
    └── Lee exchange.getAttribute("rol") → "Admin"
    ↓
Cliente recibe: 200 {"success":true, "userId":"1", "correo":"d@mail.com", "rol":"Admin"}
```

---

## Librerías utilizadas

| Librería | Uso |
|----------|-----|
| `com.sun.net.httpserver` | Servidor HTTP nativo de Java |
| `com.google.gson` | Serialización/deserialización JSON |
| `io.jsonwebtoken (jjwt)` | Generación y validación de tokens JWT |
| `org.mindrot.jbcrypt` | Hashing seguro de contraseñas |
| `mysql-connector-java` | Driver JDBC para MySQL |

---

## Endpoints disponibles

| Método | Ruta | Descripción | Autenticación |
|--------|------|-------------|---------------|
| POST | `/api/auth/login` | Iniciar sesión | No |
| GET | `/api/auth/me` | Datos del usuario autenticado | Sí (JWT) |
| GET | `/api/users` | Listar todos los usuarios | No |
| POST | `/api/users` | Crear un usuario | No |
| GET | `/api/users/id?id=X` | Obtener usuario por ID | No |
| PUT | `/api/users/id?id=X` | Actualizar usuario | No |
| DELETE | `/api/users/id?id=X` | Eliminar usuario | No |
