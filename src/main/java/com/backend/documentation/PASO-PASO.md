# Paso a Paso — Construcción del Backend UrbanLife

Este documento explica **todo lo que se construyó**, en el orden en que se fue desarrollando, con el razonamiento detrás de cada decisión.

---

## Tabla de contenidos

1. [Punto de partida — ¿Por qué Java puro?](#1-punto-de-partida--por-qué-java-puro)
2. [Paso 1 — Conexión a la base de datos](#2-paso-1--conexión-a-la-base-de-datos)
3. [Paso 2 — El servidor HTTP](#3-paso-2--el-servidor-http)
4. [Paso 3 — Utilidades de request y response](#4-paso-3--utilidades-de-request-y-response)
5. [Paso 4 — El sistema de rutas (Router y Routes)](#5-paso-4--el-sistema-de-rutas-router-y-routes)
6. [Paso 5 — Modelos](#6-paso-5--modelos)
7. [Paso 6 — DTOs](#7-paso-6--dtos)
8. [Paso 7 — DAOs (acceso a la base de datos)](#8-paso-7--daos-acceso-a-la-base-de-datos)
9. [Paso 8 — Seeders (datos iniciales)](#9-paso-8--seeders-datos-iniciales)
10. [Paso 9 — Helpers (JWT y BCrypt)](#10-paso-9--helpers-jwt-y-bcrypt)
11. [Paso 10 — Servicio y controller de login](#11-paso-10--servicio-y-controller-de-login)
12. [Paso 11 — Middleware de autenticación y roles](#12-paso-11--middleware-de-autenticación-y-roles)
13. [Paso 12 — Registro y gestión de usuarios](#13-paso-12--registro-y-gestión-de-usuarios)
14. [Paso 13 — Login con Google](#14-paso-13--login-con-google)
15. [Paso 14 — Recuperación de contraseña](#15-paso-14--recuperación-de-contraseña)
16. [Paso 15 — Punto de entrada (Main.java)](#16-paso-15--punto-de-entrada-mainjava)
17. [Resumen de la arquitectura final](#17-resumen-de-la-arquitectura-final)

---

## 1. Punto de partida — ¿Por qué Java puro?

Se eligió construir el backend **sin frameworks** (sin Spring Boot, sin Quarkus, sin Jakarta EE). La razón es didáctica: entender exactamente qué pasa en cada capa sin que un framework lo haga de manera automática y oculta.

Esto significa que todo lo que en Spring Boot es "magia" (inyección de dependencias, mapeo de rutas, manejo de JSON, CORS, etc.) aquí **lo construimos nosotros a mano**.

**Dependencias elegidas y por qué:**

| Dependencia | Propósito |
|---|---|
| `com.sun.net.httpserver` | Servidor HTTP incluido en el JDK — no requiere instalar nada |
| `mysql-connector-java` | Conectar Java con MySQL |
| `jjwt` (io.jsonwebtoken) | Generar y validar tokens JWT |
| `jbcrypt` (org.mindrot) | Hashear contraseñas con BCrypt |
| `gson` (com.google.gson) | Convertir objetos Java a JSON y viceversa |
| `dotenv-java` | Leer variables de entorno desde el archivo `.env` |
| `javax.mail` | Enviar correos SMTP (recuperación de contraseña) |

---

## 2. Paso 1 — Conexión a la base de datos

**Archivo:** [dbConnection.java](src/main/java/com/backend/config/dbConnection.java)

Lo primero que se necesita en cualquier backend con base de datos es poder **conectarse a ella**. Sin esto, ningún DAO puede funcionar.

**¿Qué hace este archivo?**

Lee las credenciales de la base de datos desde el archivo `.env` (nunca hardcodeadas en el código) y expone un método `getConnection()` que devuelve una conexión JDBC fresca cada vez que se llama.

```java
// Cada DAO llama a esto antes de ejecutar su SQL
Connection conexion = dbConnection.getConnection();
```

**¿Por qué una conexión nueva cada vez y no un pool?**
Para simplificar el código en esta etapa. Cada `try-with-resources` abre y cierra la conexión automáticamente, sin riesgo de dejarla abierta.

**Variables requeridas en el `.env`:**
```env
DB_URL=jdbc:mysql://localhost:3306/urbanlife
DB_USER=root
DB_PASSWD=tu_contrasena
```

---

## 3. Paso 2 — El servidor HTTP

**Archivo:** [serverConnection.java](src/main/java/com/backend/server/serverConnection.java)

Con la base de datos lista, el siguiente paso es levantar un servidor que pueda recibir peticiones HTTP.

Java incluye `com.sun.net.httpserver.HttpServer` en su JDK estándar. Se crea el servidor en el puerto **8080** y se le dice que **todas las rutas** (`/`) pasen por nuestro `Router`.

```java
server = HttpServer.create(new InetSocketAddress(port), 0);
server.createContext("/", router);  // Un solo punto de entrada
server.start();
```

**¿Por qué un solo contexto `/`?**
Porque nosotros mismos construimos el router. En lugar de registrar un contexto por cada ruta en `HttpServer`, registramos uno solo y dejamos que nuestro `Router` decida a qué handler enviar cada petición. Esto nos da control total.

---

## 4. Paso 3 — Utilidades de request y response

**Archivos:**
- [ApiRequest.java](src/main/java/com/backend/server/http/ApiRequest.java)
- [ApiResponse.java](src/main/java/com/backend/server/http/ApiResponse.java)

`HttpServer` de Java devuelve un objeto `HttpExchange` muy crudo. Leer el body, escribir la respuesta, agregar headers… todo es verbose y repetitivo. Se crearon estas dos clases para encapsular esa complejidad.

**ApiRequest** — leer el cuerpo de la petición:
```java
ApiRequest peticion = new ApiRequest(exchange);
String cuerpo = peticion.readBody();  // Lee el JSON que envió el cliente
```

**ApiResponse** — responder al cliente:
```java
// Respuesta de error rápida
ApiResponse.error(exchange, 400, "El cuerpo esta vacio");

// Respuesta con JSON personalizado
ApiResponse.send(exchange, jsonString, 200);

// Respuesta con cualquier objeto Java (lo convierte a JSON automáticamente)
ApiResponse.sendJson(exchange, 200, Map.of("success", true, "data", lista));
```

**CORS** — Todos los métodos de `ApiResponse` agregan automáticamente los headers de CORS:
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
```

Esto permite que el frontend (corriendo en otro puerto, como `:5500`) pueda hacer peticiones al backend sin ser bloqueado por el navegador.

---

## 5. Paso 4 — El sistema de rutas (Router y Routes)

**Archivos:**
- [Router.java](src/main/java/com/backend/routes/Router.java)
- [Routes.java](src/main/java/com/backend/routes/Routes.java)

### Router.java — El despachador

Es el corazón del sistema de rutas. Internamente guarda un `Map<método HTTP, Map<ruta, handler>>`.

```java
// Estructura interna
{
  "GET":  { "/api/users": handler1, "/api/auth/me": handler2 },
  "POST": { "/api/auth/login": handler3 }
}
```

Cuando llega una petición:
1. Lee el método HTTP (`GET`, `POST`, etc.)
2. Lee la ruta (`/api/auth/login`)
3. Busca el handler correspondiente en el mapa
4. Si lo encuentra, lo ejecuta. Si no, devuelve `404`

También maneja automáticamente las peticiones `OPTIONS` (preflight CORS) devolviendo `204`.

**Métodos disponibles para registrar rutas:**
```java
router.get("/ruta", handler);
router.post("/ruta", handler);
router.put("/ruta", handler);
router.patch("/ruta", handler);
router.delete("/ruta", handler);
```

### Routes.java — El registro de rutas

Aquí se registran **todas las rutas** del sistema en un solo lugar. Es el equivalente a un `app.js` en Express o un `@RestController` en Spring.

```java
// Ejemplo de lo que hace:
router.post("/api/auth/login", AuthController.login());
router.get("/api/users", auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN"));
```

Al arrancar el servidor, `Routes.configureRoutes()` construye todo el mapa de rutas y lo devuelve al `HttpServer`.

---

## 6. Paso 5 — Modelos

**Paquete:** [models/](src/main/java/com/backend/models/)

Los modelos son clases Java que representan las tablas de la base de datos. Son **POJOs** puros (Plain Old Java Objects): solo atributos, constructores, getters y setters. No tienen lógica de negocio.

**Modelos del sistema de autenticación:**

### Usuario.java
Representa la tabla `usuarios` en la BD:

| Campo Java | Columna BD | Tipo | Descripción |
|---|---|---|---|
| `idUsuario` | `id_usuario` | int | Clave primaria |
| `nombre` | `nombre` | String | Nombre del usuario |
| `correo` | `correo` | String | Email único |
| `contrasena` | `contrasena` | String | Hash BCrypt (NULL si es cuenta Google) |
| `estado` | `estado` | boolean | true = activo, false = inactivo |
| `googleId` | `google_id` | String | ID de Google (NULL si no usa Google) |

El campo `contrasena` siendo `NULL` es la señal de que esa cuenta se creó con Google y **no puede hacer login con contraseña**.

### Rol.java
Representa la tabla `roles`:
- `idRoles`, `nombre`, `descripcion`

### UsuarioRol.java
Tabla intermedia `usuarios_roles` — relaciona un usuario con su rol:
- `idUsuarioRol`, `usuarioId`, `rolId`

### TokenRecuperacion.java
Representa la tabla `token_recuperacion` — tokens para restablecer contraseña:
- `idToken`, `usuarioId`, `token` (UUID), `fechaExpiracion`, `usado`, `fechaCreacion`

---

## 7. Paso 6 — DTOs

**Paquete:** [dto/](src/main/java/com/backend/dto/)

Un DTO (Data Transfer Object) es una clase que representa los datos que **llegan desde el cliente** en el body de una petición. Se usa en lugar de parsear el JSON a mano.

### CreateUserRequest.java
Para crear un usuario (`POST /api/users`):
```json
{ "nombre": "Juan", "correo": "juan@x.com", "contrasena": "Abc12345" }
```
Gson convierte ese JSON directamente a un objeto `CreateUserRequest`.

### LoginRequest.java
Para el login (`POST /api/auth/login`):
```json
{ "correo": "juan@x.com", "contrasena": "Abc12345" }
```

Ambos DTOs tienen un método `isValid()` que verifica que ningún campo venga vacío o nulo.

**¿Por qué usar DTOs en lugar de parsear el JSON directamente?**
Organización: el controller no necesita saber cómo se estructura el JSON. Solo recibe el DTO ya construido. Gson se encarga de la conversión.

---

## 8. Paso 7 — DAOs (acceso a la base de datos)

**Paquete:** [dao/](src/main/java/com/backend/dao/)

Un DAO (Data Access Object) es la capa que habla directamente con la base de datos. Cada DAO corresponde a una tabla y contiene todos los SQL necesarios para operar sobre ella.

**Patrón que siguen todos los DAOs:**
```java
// Siempre con try-with-resources para cerrar la conexión automáticamente
try (Connection conexion = dbConnection.getConnection();
     PreparedStatement consulta = conexion.prepareStatement(sql)) {
    // configurar parámetros
    // ejecutar
    // retornar resultado o null si falla
} catch (Exception excepcion) {
    System.out.println("Error NombreDAO.metodo: " + excepcion.getMessage());
}
return null; // o false según el caso
```

**¿Por qué PreparedStatement y no concatenar strings?**
Seguridad. Concatenar el input del usuario directamente en el SQL permite **inyección SQL**. Con `PreparedStatement`, los parámetros se escapan automáticamente.

### UsuarioDAO.java — Los métodos más importantes

| Método | SQL que ejecuta |
|---|---|
| `findByCorreo(correo)` | `SELECT * FROM usuarios WHERE correo = ?` |
| `findById(id)` | `SELECT * FROM usuarios WHERE id_usuario = ?` |
| `findByGoogleId(googleId)` | `SELECT * FROM usuarios WHERE google_id = ?` |
| `findAll()` | `SELECT * FROM usuarios ORDER BY id_usuario ASC` |
| `create(usuario)` | `INSERT INTO usuarios (nombre, correo, contrasena, estado) VALUES (?, ?, ?, ?)` |
| `createWithGoogle(...)` | INSERT con `contrasena = NULL` y `google_id = ?` |
| `linkGoogleId(id, googleId)` | `UPDATE usuarios SET google_id = ? WHERE id_usuario = ?` |
| `update(usuario)` | `UPDATE usuarios SET nombre, correo, estado WHERE id_usuario = ?` |
| `updatePassword(id, hash)` | `UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?` |
| `findRolByUsuarioId(id)` | JOIN entre `roles` y `usuarios_roles` para obtener el rol del usuario |

El método `mapRow()` privado convierte un `ResultSet` en un objeto `Usuario` — evita repetir ese código en cada método.

### TokenRecuperacionDAO.java

| Método | Qué hace |
|---|---|
| `guardarToken(usuarioId, expiracion)` | Genera un UUID, lo guarda en BD con fecha de expiración, retorna el UUID |
| `validarToken(token)` | Busca el token en BD, verifica que no esté usado y no haya expirado |
| `marcarTokenUsado(idToken)` | Hace `UPDATE ... SET usado = TRUE` para inutilizar el token |
| `actualizarContrasena(usuarioId, hash)` | Actualiza la contraseña del usuario en BD |

### RolDAO.java y UsuarioRolDAO.java

- `RolDAO.findByNombre("EMPLEADO")` — busca el rol por nombre para asignarlo a usuarios nuevos
- `UsuarioRolDAO.create(usuarioRol)` — inserta la relación usuario-rol en la tabla intermedia `usuarios_roles`

---

## 9. Paso 8 — Seeders (datos iniciales)

**Paquete:** [seeders/](src/main/java/com/backend/seeders/)

Los seeders son clases que insertan datos básicos e indispensables en la base de datos **solo si no existen todavía**. Se ejecutan al arrancar el servidor.

**¿Por qué son necesarios?**
El sistema necesita que existan los roles (`SUPER_ADMIN`, `ADMIN`, `EMPLEADO`) en la BD antes de poder asignarlos a usuarios. Si la tabla está vacía, el registro de usuarios fallaría.

**Seeders disponibles:**
- `SeedRoles` — Inserta los 3 roles del sistema
- `SeedPermisos` — Inserta permisos base
- `SeedTipoMovimientos` — Datos para movimientos financieros
- `SeedTipoGasto` — Tipos de gasto

**¿Cómo evitan duplicar datos?**
Antes de insertar, hacen un `SELECT COUNT(*)`. Si la tabla ya tiene datos, simplemente imprimen `"Ya existen datos -> omitido"` y no hacen nada.

```java
// Lógica de todos los seeders
String sqlVerificacion = "SELECT COUNT(*) FROM Roles";
if (resultado.getInt(1) > 0) {
    System.out.println("[Roles] Ya existen datos -> omitido");
    return;
}
// Si está vacía, inserta los datos iniciales
```

---

## 10. Paso 9 — Helpers (JWT y BCrypt)

**Paquete:** [helpers/](src/main/java/com/backend/helpers/)

Los helpers son utilidades reutilizables que encapsulan lógica técnica específica. No tienen lógica de negocio — solo hacen bien una cosa.

### PasswordHelper.java

Envuelve la librería `jbcrypt` para hashear y verificar contraseñas.

```java
// Al registrar un usuario o cambiar contraseña:
String hash = PasswordHelper.hashPassword("MiContrasena1");
// Resultado: "$2a$12$..." (hash BCrypt de 60 caracteres)

// Al hacer login:
boolean esCorrecta = PasswordHelper.checkPassword("MiContrasena1", hashGuardadoEnBD);
```

**¿Por qué BCrypt y no MD5 o SHA?**
BCrypt está diseñado específicamente para contraseñas. Tiene tres ventajas clave:
1. **Lento por diseño** — el factor de costo 12 hace que cada hash tarde ~250ms, lo que hace los ataques de fuerza bruta impracticables
2. **Salt automático** — cada hash incluye un salt aleatorio, por lo que dos usuarios con la misma contraseña tendrán hashes distintos
3. **`checkpw` es en tiempo constante** — resiste ataques de timing

### JwtHelper.java

Genera y valida tokens JWT.

```java
// Al hacer login exitoso:
String token = JwtHelper.generateToken(5, "juan@x.com", "EMPLEADO");

// En el middleware, al llegar una petición protegida:
Claims datos = JwtHelper.validateToken(tokenDelHeader);
datos.getSubject();          // "5" (userId)
datos.get("correo");         // "juan@x.com"
datos.get("rol");            // "EMPLEADO"
```

**Estructura del token generado:**

```
Header:  { "alg": "HS256" }
Payload: { "sub": "5", "correo": "juan@x.com", "rol": "EMPLEADO", "iat": ..., "exp": ... }
Firma:   HMAC-SHA256 con JWT_SECRET del .env
```

**Expiración: 24 horas.** Después de ese tiempo, `validateToken()` lanza `ExpiredJwtException` y el middleware responde con `401 Token expirado`.

---

## 11. Paso 10 — Servicio y controller de login

**Archivos:**
- [AuthService.java](src/main/java/com/backend/services/AuthService.java)
- [AuthController.java](src/main/java/com/backend/controllers/AuthController.java)

Aquí empieza la lógica de negocio real. Se sigue el patrón **Controller → Service → DAO**:

- El **Controller** recibe la petición HTTP, parsea el JSON, llama al servicio y devuelve la respuesta
- El **Service** contiene toda la lógica de validación y decisión
- El **DAO** solo habla con la base de datos

**¿Por qué separar Controller y Service?**
Si la lógica estuviera en el Controller, mezclaría responsabilidades HTTP con lógica de negocio. El Service puede ser reutilizado por múltiples controllers, probado de forma aislada, y modificado sin tocar el controller.

### Flujo del login paso a paso

```
POST /api/auth/login
  │
  ▼ AuthController.login()
  │  Lee el body JSON
  │  Extrae "correo" y "contrasena"
  │  Llama a AuthService.validateLogin(correo, contrasena)
  │
  ▼ AuthService.validateLogin()
  │
  ├─ ¿correo o contrasena vacíos?       → 400 "Correo y contraseña son requeridos"
  ├─ ¿formato de correo inválido?       → 400 "El formato del correo no es válido"
  ├─ UsuarioDAO.findByCorreo(correo)
  │    ├─ ¿usuario == null?             → 401 "Credenciales inválidas"
  │    ├─ ¿usuario.contrasena == null?  → 401 "Esta cuenta usa inicio de sesión con Google"
  │    ├─ BCrypt.checkpw() falla?       → 401 "Credenciales inválidas"
  │    └─ ¿usuario.estado == false?     → 403 "Usuario inactivo"
  │
  ├─ UsuarioDAO.findRolByUsuarioId()
  │
  └─ JwtHelper.generateToken()
       → 200 { token, nombre, correo, rol }
```

**Nota de seguridad importante:** cuando el usuario no existe y cuando la contraseña es incorrecta, el mensaje de error es **idéntico**: `"Credenciales inválidas"`. Esto evita que un atacante pueda descubrir qué correos están registrados en el sistema probando emails.

### AuthController.me()

Un segundo endpoint en el mismo controller: `GET /api/auth/me`. Como está protegido por el middleware, cuando llega al controller el token ya fue validado y los datos ya están inyectados en el exchange. Solo hay que leerlos:

```java
String idUsuario = (String) exchange.getAttribute("userId");
String correo    = (String) exchange.getAttribute("correo");
String rol       = (String) exchange.getAttribute("rol");
```

No consulta la base de datos — los datos vienen del propio token JWT.

---

## 12. Paso 11 — Middleware de autenticación y roles

**Archivo:** [AuthMiddleware.java](src/main/java/com/backend/middlewares/AuthMiddleware.java)

Con el login funcionando, el siguiente problema es: **¿cómo proteger rutas?** Sin un mecanismo de protección, cualquiera podría acceder a `GET /api/users` sin autenticarse.

La solución es un **middleware**: una función que intercepta la petición antes de que llegue al handler real, verifica el token y decide si deja pasar o rechaza.

**Uso en Routes.java:**
```java
// Ruta pública — sin middleware
router.post("/api/auth/login", AuthController.login());

// Ruta protegida — solo JWT válido
router.get("/api/auth/me", auth.protect(AuthController.me()));

// Ruta protegida con roles específicos
router.get("/api/users", auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN"));
```

**¿Cómo funciona `auth.protect()`?**

`protect()` devuelve un nuevo `HttpHandler` que envuelve al handler original:

```
Petición HTTP
     │
     ▼ AuthMiddleware.protect()
     │
     ├─ ¿Es OPTIONS (preflight CORS)?  → responde 204 y termina
     │
     ├─ ¿Tiene header "Authorization: Bearer ..."?
     │    └─ No → 401 "Token de autenticacion requerido"
     │
     ├─ JwtHelper.validateToken(token)
     │    ├─ ExpiredJwtException  → 401 "Token expirado. Inicie sesion nuevamente"
     │    └─ JwtException         → 401 "Token invalido"
     │
     ├─ Inyecta userId, correo, rol en el exchange como atributos
     │
     ├─ ¿Se especificaron roles?
     │    └─ ¿El rol del token no está en la lista? → 403 "No tiene permiso para esta accion"
     │
     └─ next.handle(exchange)  ← pasa al controller real
```

**¿Por qué distinguir token expirado de token inválido?**
Porque el frontend necesita reaccionar diferente: si el token expiró, debe redirigir al login con mensaje "sesión expirada". Si es inválido (manipulado), es un caso de seguridad más serio.

---

## 13. Paso 12 — Registro y gestión de usuarios

**Archivos:**
- [UserService.java](src/main/java/com/backend/services/UserService.java)
- [UserController.java](src/main/java/com/backend/controllers/UserController.java)

### Registro (`POST /api/users`)

El registro de usuarios está protegido — solo `SUPER_ADMIN` y `ADMIN` pueden crear usuarios. El flujo:

```
POST /api/users (requiere JWT de SUPER_ADMIN o ADMIN)
  │
  ▼ UserController.create()
  │  Parsea el body → CreateUserRequest
  │  Llama a UserService.validateAndCreate()
  │
  ▼ UserService.validateAndCreate()
  │
  ├─ ¿Campos vacíos?           → 400
  ├─ ¿Formato email inválido?  → 400
  ├─ ¿Contraseña débil?        → 400 (no cumple PASSWORD_REGEX)
  ├─ UsuarioDAO.findByCorreo() → ¿correo duplicado? → 409
  │
  ├─ PasswordHelper.hashPassword(contrasena)
  ├─ UsuarioDAO.create(usuario)
  ├─ RolDAO.findByNombre("EMPLEADO")
  ├─ UsuarioRolDAO.create(usuarioId, rolEmpleadoId)
  └─ JwtHelper.generateToken()
       → 201 { token, nombre, correo, rol: "EMPLEADO" }
```

El registro hace **auto-login**: al crear el usuario exitosamente, devuelve directamente un JWT. Así el frontend puede iniciar la sesión sin un paso extra.

**Regla de contraseña (PASSWORD_REGEX):**
```
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$
```
- Al menos una minúscula
- Al menos una mayúscula
- Al menos un número
- Mínimo 8 caracteres en total

### Actualización de usuarios (PUT y PATCH)

Se implementaron dos variantes:

**PUT** — Reemplazo completo. `nombre` y `correo` son **obligatorios**. Si se envía `contrasena`, se actualiza también.

**PATCH** — Actualización parcial. Solo se actualizan los campos que vienen en el body. Si no viene `correo`, el correo no cambia.

### Restricción de recurso propio para EMPLEADO

El middleware verifica el rol pero no puede saber el `id` del recurso que se está pidiendo (está en el query param `?id=X`). Por eso esa verificación extra se hace **en el controller**:

```java
String rolUsuario    = (String) exchange.getAttribute("rol");
String idUsuarioToken = (String) exchange.getAttribute("userId");

if ("EMPLEADO".equalsIgnoreCase(rolUsuario) && !idUsuarioToken.equals(String.valueOf(id))) {
    ApiResponse.error(exchange, 403, "No tiene permiso para acceder a este recurso");
    return;
}
```

Un `EMPLEADO` puede ver o modificar su propio perfil (`?id=5` cuando su token dice `userId=5`), pero no el de otro usuario.

**Las contraseñas nunca se devuelven en las respuestas:**
```java
usuario.setContrasena(null);  // Se limpia antes de serializar
```

---

## 14. Paso 13 — Login con Google

**Archivos:**
- [GoogleAuthService.java](src/main/java/com/backend/services/GoogleAuthService.java)
- [GoogleAuthController.java](src/main/java/com/backend/controllers/GoogleAuthController.java)

El login con Google permite que los usuarios se autentiquen usando su cuenta de Google sin necesidad de registrarse manualmente.

**¿Cómo funciona Google Sign-In?**

El frontend usa la librería de Google Identity Services. Cuando el usuario hace clic en "Iniciar con Google", Google devuelve al frontend un `credential` (un ID Token JWT firmado por Google). El frontend envía ese token a nuestro backend.

**Flujo completo:**

```
POST /api/auth/google  { "credential": "<id-token-de-google>" }
  │
  ▼ GoogleAuthController → GoogleAuthService.loginWithGoogle()
  │
  ├─ ¿Token vacío? → 400
  │
  ├─ verificarTokenConGoogle(idToken)
  │    Hace GET a https://oauth2.googleapis.com/tokeninfo?id_token=<token>
  │    Si Google responde != 200 → return null → 401 "Token inválido o expirado"
  │
  ├─ ¿datosGoogle.aud == GOOGLE_CLIENT_ID?
  │    No → 401 "Token no autorizado para esta aplicación"
  │    (Evita que un token de otra app de Google funcione en la nuestra)
  │
  ├─ ¿datosGoogle.email_verified == "true"?
  │    No → 401 "El correo de Google no está verificado"
  │
  ├─ Extrae: googleId (sub), correo, nombre
  │
  ├─ UsuarioDAO.findByGoogleId(googleId)
  │    ├─ Encontrado → usar ese usuario
  │    └─ No encontrado → UsuarioDAO.findByCorreo(correo)
  │         ├─ Encontrado (cuenta manual con ese correo)
  │         │    └─ UsuarioDAO.linkGoogleId() → vincula el google_id a la cuenta existente
  │         └─ No encontrado → UsuarioDAO.createWithGoogle()
  │              └─ UsuarioRolDAO.create() → asigna rol EMPLEADO
  │
  ├─ ¿usuario.estado == false? → 403
  │
  ├─ UsuarioDAO.findRolByUsuarioId()
  └─ JwtHelper.generateToken()
       → 200 { token, nombre, correo, rol }
```

**El caso de vinculación:** si alguien tiene cuenta manual con `correo = juan@gmail.com` y luego inicia sesión con Google usando ese mismo correo, el sistema detecta la cuenta existente y le agrega el `google_id`. A partir de ese momento puede autenticarse de ambas formas.

**Las cuentas de Google tienen `contrasena = NULL` en la BD.** Esta es la señal que usa `AuthService` para detectar que una cuenta no puede hacer login con contraseña.

---

## 15. Paso 14 — Recuperación de contraseña

**Archivos:**
- [PasswordResetService.java](src/main/java/com/backend/services/PasswordResetService.java)
- [PasswordResetController.java](src/main/java/com/backend/controllers/PasswordResetController.java)
- [EmailService.java](src/main/java/com/backend/services/EmailService.java)
- [TokenRecuperacionDAO.java](src/main/java/com/backend/dao/TokenRecuperacionDAO.java)

La recuperación de contraseña es un flujo de **3 endpoints encadenados**.

### Endpoint 1 — Solicitar recuperación (`POST /api/auth/forgot-password`)

```java
// Flujo en PasswordResetService.solicitarRecuperacion()

UsuarioDAO.findByCorreo(correo)
  ├─ ¿No existe?
  │    → Respuesta GENÉRICA exitosa (no revela si el correo existe)
  │      "Si el correo está registrado, recibirás un enlace en breve"
  │
  ├─ ¿usuario.contrasena == null? (cuenta Google)
  │    → 400 "Esta cuenta usa inicio de sesion con Google"
  │
  ├─ ¿usuario.estado == false?
  │    → 403 "Usuario inactivo"
  │
  └─ TokenRecuperacionDAO.guardarToken(usuarioId, ahora + 1 hora)
       Genera UUID aleatorio, lo guarda en BD con expiración
       └─ EmailService.enviarCorreoRecuperacion(correo, token)
            Envía email HTML con botón que apunta a:
            http://localhost:5500/reset-password.html?token=<uuid>
```

**¿Por qué la respuesta genérica cuando el correo no existe?**
Si respondiéramos con "ese correo no está registrado", un atacante podría usar este endpoint para averiguar qué correos tienen cuenta en el sistema (enumeración de usuarios). Con la respuesta genérica, no puede saber si el correo existe o no.

### Endpoint 2 — Validar token (`GET /api/auth/reset-password/validate?token=X`)

El frontend llama a este endpoint al cargar la página de restablecimiento. Si el token no es válido o expiró, puede mostrar un error antes de que el usuario intente cambiar la contraseña.

```java
TokenRecuperacionDAO.validarToken(token)
  ├─ ¿No existe o ya fue usado?  → 400 "Token invalido o ya utilizado"
  ├─ ¿Fecha actual > expiración? → 400 "El token ha expirado"
  └─ Token válido                → 200 { idToken, usuarioId }
```

### Endpoint 3 — Cambiar contraseña (`POST /api/auth/reset-password`)

```java
// Flujo en PasswordResetService.cambiarContrasena()

├─ ¿Token o contraseña vacíos?      → 400
├─ ¿Contraseña no cumple PASSWORD_REGEX? → 400
├─ TokenRecuperacionDAO.validarToken(token)  (segunda verificación)
│    └─ Si falla → retorna el error del DAO directamente
├─ PasswordHelper.hashPassword(nuevaContrasena)
├─ TokenRecuperacionDAO.actualizarContrasena(usuarioId, hash)
└─ TokenRecuperacionDAO.marcarTokenUsado(idToken)
     → El token queda inutilizable para siempre
```

**¿Por qué validar el token dos veces (en el endpoint 2 y en el endpoint 3)?**
Porque entre la validación y el cambio podría pasar tiempo. El token se valida nuevamente antes de actualizar la contraseña para garantizar que no expiró en ese intervalo.

### EmailService.java

Envía correos usando SMTP de Gmail con `javax.mail`. Requiere:
```env
EMAIL_USER=tu_correo@gmail.com
EMAIL_PASS=tu_contrasena_de_aplicacion_gmail
```

> **Nota:** Gmail requiere una "contraseña de aplicación" (no la contraseña normal) cuando la cuenta tiene verificación en dos pasos activada.

Si el envío falla (red, credenciales incorrectas, etc.), el link se imprime en consola del servidor para no bloquear el flujo de desarrollo:
```
==============================
LINK RECUPERACION (fallo email):
http://localhost:5500/reset-password.html?token=abc-123-...
==============================
```

---

## 16. Paso 15 — Punto de entrada (Main.java)

**Archivo:** [Main.java](src/main/java/com/backend/Main.java)

El punto de entrada del programa. Hace exactamente tres cosas en orden:

```java
public static void main(String[] args) {
    // 1. Ejecutar seeders — inserta datos iniciales si las tablas están vacías
    SeedRoles.insertRoles();
    SeedPermisos.insertPermisos();
    SeedTipoMovimientos.insertTipoMovimientos();
    SeedTipoGasto.insertTipoGasto();

    // 2. Iniciar el servidor en el puerto 8080
    serverConnection.startServer(8080);

    // Al iniciar, serverConnection llama a Routes.configureRoutes()
    // que registra todas las rutas en el Router
}
```

**Secuencia de arranque completa:**
```
Main.main()
  └─ SeedRoles, SeedPermisos... (insertan datos si BD está vacía)
  └─ serverConnection.startServer(8080)
       └─ Routes.configureRoutes()
            └─ Registra todos los endpoints con sus handlers y middlewares
       └─ HttpServer.start()
            └─ Servidor escuchando en http://localhost:8080
```

---

## 17. Resumen de la arquitectura final

```
src/main/java/com/backend/
│
├── Main.java                    ← Punto de entrada
│
├── config/
│   └── dbConnection.java        ← Conexión a MySQL con variables de entorno
│
├── server/
│   ├── serverConnection.java    ← Levanta el HttpServer en el puerto 8080
│   └── http/
│       ├── ApiRequest.java      ← Lee el body de la petición
│       └── ApiResponse.java     ← Escribe la respuesta JSON con headers CORS
│
├── routes/
│   ├── Router.java              ← Despacha peticiones según método+ruta
│   └── Routes.java              ← Registra todos los endpoints del sistema
│
├── models/                      ← Representan las tablas de la BD (POJOs)
│   ├── Usuario.java
│   ├── Rol.java
│   ├── UsuarioRol.java
│   └── TokenRecuperacion.java
│
├── dto/                         ← Representan el body de las peticiones
│   ├── CreateUserRequest.java
│   └── LoginRequest.java
│
├── dao/                         ← Hablan directamente con MySQL (PreparedStatement)
│   ├── UsuarioDAO.java
│   ├── RolDAO.java
│   ├── UsuarioRolDAO.java
│   └── TokenRecuperacionDAO.java
│
├── helpers/                     ← Utilidades técnicas reutilizables
│   ├── JwtHelper.java           ← Generar y validar JWT
│   └── PasswordHelper.java      ← Hashear y verificar con BCrypt
│
├── middlewares/
│   └── AuthMiddleware.java      ← Protege rutas verificando JWT y roles
│
├── services/                    ← Lógica de negocio y validaciones
│   ├── AuthService.java         ← Validaciones del login
│   ├── UserService.java         ← Crear y actualizar usuarios
│   ├── GoogleAuthService.java   ← Verificar tokens de Google
│   ├── PasswordResetService.java← Flujo de recuperación de contraseña
│   └── EmailService.java        ← Envío de correos SMTP
│
├── controllers/                 ← Reciben HTTP, llaman al service, responden
│   ├── AuthController.java      ← login(), me()
│   ├── GoogleAuthController.java← loginWithGoogle()
│   ├── UserController.java      ← CRUD de usuarios
│   └── PasswordResetController.java← 3 endpoints de recuperación
│
└── seeders/                     ← Insertan datos iniciales al arrancar
    ├── SeedRoles.java
    ├── SeedPermisos.java
    ├── SeedTipoMovimientos.java
    └── SeedTipoGasto.java
```

### Flujo de una petición cualquiera (de principio a fin)

```
Cliente HTTP (navegador / Postman)
    │
    │  POST /api/auth/login
    ▼
HttpServer (serverConnection.java)
    │
    ▼
Router.handle() (Router.java)
    │  Busca en el mapa: POST → /api/auth/login → handler
    ▼
AuthMiddleware (si la ruta es protegida)
    │  Verifica JWT, verifica rol
    ▼
AuthController.login() (controller)
    │  Lee body, extrae correo y contrasena
    ▼
AuthService.validateLogin() (service)
    │  Valida formato, llama al DAO
    ▼
UsuarioDAO.findByCorreo() (dao)
    │  Ejecuta SQL contra MySQL
    ▼
MySQL devuelve resultado
    │
    ▼  (de vuelta subiendo por las capas)
AuthService genera JWT
    ▼
AuthController construye respuesta JSON
    ▼
ApiResponse.send() agrega headers y envía
    ▼
Cliente recibe { success: true, token: "...", ... }
```
