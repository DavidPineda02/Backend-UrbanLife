// Paquete de rutas de la API
package com.backend.routes;

// Importar todos los controllers que manejan las rutas registradas
import com.backend.controllers.AuthController;
import com.backend.controllers.CategoriaController;
import com.backend.controllers.ClienteController;
import com.backend.controllers.GoogleAuthController;
import com.backend.controllers.PasswordResetController;
import com.backend.controllers.ProductoController;
import com.backend.controllers.ProveedorController;
import com.backend.controllers.UserController;
// Middleware para proteger rutas con autenticacion JWT y control de roles
import com.backend.middlewares.AuthMiddleware;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Clase que registra todas las rutas de la API en el Router.
 * Configura endpoints de autenticación, usuarios y recuperación de contraseña.
 * Centraliza la configuración de todas las rutas del sistema.
 */
public class Routes {

    /** Instancia del dispatcher que mapea metodo+path a sus handlers */
    Router router = new Router();

    /**
     * Registra todas las rutas y retorna el router configurado para el servidor.
     * Configura autenticación, autorización y endpoints de la API.
     * @return HttpHandler configurado con todas las rutas de la API
     */
    public HttpHandler configureRoutes() {

        // Instancia del middleware de autenticacion para proteger rutas
        AuthMiddleware auth = new AuthMiddleware();

        // ========== RUTAS DE AUTH ==========
        // Login con correo y contrasena (publico)
        router.post("/api/auth/login", AuthController.login());
        // Registro de nuevo usuario con rol EMPLEADO (publico)
        router.post("/api/auth/register", AuthController.register());
        // Login con token de Google (publico)
        router.post("/api/auth/google", GoogleAuthController.loginWithGoogle());
        // Retorna datos del usuario autenticado (requiere JWT)
        router.get("/api/auth/me", auth.protect(AuthController.me()));

        // ========== RUTAS DE RECUPERACION DE CONTRASENA ==========
        // Solicitar enlace de recuperacion (publico)
        router.post("/api/auth/forgot-password", PasswordResetController.solicitarRecuperacion());
        // Validar que el token no expiro (publico)
        router.get("/api/auth/reset-password/validate", PasswordResetController.validarToken());
        // Cambiar contrasena con el token (publico)
        router.post("/api/auth/reset-password", PasswordResetController.cambiarContrasena());

        // ========== RUTAS DE USUARIOS ==========
        // Listar todos los usuarios
        router.get("/api/users",    auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN"));
        // Obtener usuario por ID
        router.get("/api/users/id", auth.protect(UserController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Actualizar usuario completo (PUT)
        router.put("/api/users/id", auth.protect(UserController.update(),  "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Actualizar usuario parcial (PATCH)
        router.patch("/api/users/id", auth.protect(UserController.patch(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        
        // ========== RUTAS DE CATEGORIAS ==========
        // Listar todas las categorías
        router.get("/api/categorias",    auth.protect(CategoriaController.listAll(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Obtener categoría por ID
        router.get("/api/categorias/id", auth.protect(CategoriaController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Crear nueva categoría
        router.post("/api/categorias",   auth.protect(CategoriaController.create(),  "SUPER_ADMIN", "ADMIN"));
        // Actualizar categoría completa
        router.put("/api/categorias/id", auth.protect(CategoriaController.update(),  "SUPER_ADMIN", "ADMIN"));
        // Cambiar estado activo/inactivo
        router.patch("/api/categorias/id", auth.protect(CategoriaController.patch(), "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE PRODUCTOS ==========
        // Listar todos los productos
        router.get("/api/productos",    auth.protect(ProductoController.listAll(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Obtener producto por ID
        router.get("/api/productos/id", auth.protect(ProductoController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Crear nuevo producto
        router.post("/api/productos",   auth.protect(ProductoController.create(),  "SUPER_ADMIN", "ADMIN"));
        // Actualizar producto completo
        router.put("/api/productos/id", auth.protect(ProductoController.update(),  "SUPER_ADMIN", "ADMIN"));
        // Cambiar estado activo/inactivo
        router.patch("/api/productos/id", auth.protect(ProductoController.patch(), "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE CLIENTES ==========
        // Listar todos los clientes
        router.get("/api/clientes",    auth.protect(ClienteController.listAll(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Obtener cliente por ID
        router.get("/api/clientes/id", auth.protect(ClienteController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Crear nuevo cliente
        router.post("/api/clientes",   auth.protect(ClienteController.create(),  "SUPER_ADMIN", "ADMIN"));
        // Actualizar cliente completo
        router.put("/api/clientes/id", auth.protect(ClienteController.update(),  "SUPER_ADMIN", "ADMIN"));
        // Cambiar estado activo/inactivo
        router.patch("/api/clientes/id", auth.protect(ClienteController.patch(), "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE PROVEEDORES ==========
        // Listar todos los proveedores
        router.get("/api/proveedores",    auth.protect(ProveedorController.listAll(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Obtener proveedor por ID
        router.get("/api/proveedores/id", auth.protect(ProveedorController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Crear nuevo proveedor
        router.post("/api/proveedores",   auth.protect(ProveedorController.create(),  "SUPER_ADMIN", "ADMIN"));
        // Actualizar proveedor completo
        router.put("/api/proveedores/id", auth.protect(ProveedorController.update(),  "SUPER_ADMIN", "ADMIN"));
        // Cambiar estado activo/inactivo
        router.patch("/api/proveedores/id", auth.protect(ProveedorController.patch(), "SUPER_ADMIN", "ADMIN"));

        // Imprimir en consola las rutas activas al iniciar el servidor
        System.out.println("Rutas registradas:");
        System.out.println("  POST   /api/auth/login                      (publico)");
        System.out.println("  POST   /api/auth/register                   (publico)");
        System.out.println("  POST   /api/auth/google                     (publico)");
        System.out.println("  GET    /api/auth/me                         (autenticado)");
        System.out.println("  POST   /api/auth/forgot-password            (publico)");
        System.out.println("  GET    /api/auth/reset-password/validate    (publico)");
        System.out.println("  POST   /api/auth/reset-password             (publico)");
        System.out.println("  GET    /api/users                           (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/users/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO propio)");
        System.out.println("  PUT    /api/users/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO propio)");
        System.out.println("  PATCH  /api/users/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO propio)");
        System.out.println("  GET    /api/categorias                       (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  GET    /api/categorias/id?id=X               (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  POST   /api/categorias                       (SUPER_ADMIN, ADMIN)");
        System.out.println("  PUT    /api/categorias/id?id=X               (SUPER_ADMIN, ADMIN)");
        System.out.println("  PATCH  /api/categorias/id?id=X               (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/productos                         (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  GET    /api/productos/id?id=X                 (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  POST   /api/productos                         (SUPER_ADMIN, ADMIN)");
        System.out.println("  PUT    /api/productos/id?id=X                 (SUPER_ADMIN, ADMIN)");
        System.out.println("  PATCH  /api/productos/id?id=X                 (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/clientes                           (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  GET    /api/clientes/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  POST   /api/clientes                           (SUPER_ADMIN, ADMIN)");
        System.out.println("  PUT    /api/clientes/id?id=X                   (SUPER_ADMIN, ADMIN)");
        System.out.println("  PATCH  /api/clientes/id?id=X                   (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/proveedores                         (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  GET    /api/proveedores/id?id=X                 (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  POST   /api/proveedores                         (SUPER_ADMIN, ADMIN)");
        System.out.println("  PUT    /api/proveedores/id?id=X                 (SUPER_ADMIN, ADMIN)");
        System.out.println("  PATCH  /api/proveedores/id?id=X                 (SUPER_ADMIN, ADMIN)");

        // Retornar el router ya configurado para registrarlo en el servidor HTTP
        return router;
    }
}
