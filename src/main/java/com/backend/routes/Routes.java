// Paquete de rutas de la API
package com.backend.routes;

// Importar todos los controllers que manejan las rutas registradas
import com.backend.controllers.AuthController;
import com.backend.controllers.CategoriaController;
import com.backend.controllers.GoogleAuthController;
import com.backend.controllers.PasswordResetController;
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

        // Retornar el router ya configurado para registrarlo en el servidor HTTP
        return router;
    }
}
