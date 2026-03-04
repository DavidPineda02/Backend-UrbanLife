package com.backend.routes;

// Importar todos los controllers que manejan las rutas registradas
import com.backend.controllers.AuthController;
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
 */
public class Routes {

    /** Instancia del dispatcher que mapea metodo+path a sus handlers */
    Router router = new Router();

    /**
     * Registra todas las rutas y retorna el router configurado para el servidor.
     * @return HttpHandler configurado con todas las rutas de la API
     */
    public HttpHandler configureRoutes() {

        // Instancia del middleware de autenticacion para proteger rutas
        AuthMiddleware auth = new AuthMiddleware();

        // ========== RUTAS DE AUTH ==========
        router.post("/api/auth/login", AuthController.login());                    // Login con correo y contrasena (publico)
        router.post("/api/auth/google", GoogleAuthController.loginWithGoogle());   // Login con token de Google (publico)
        router.get("/api/auth/me", auth.protect(AuthController.me()));             // Retorna datos del usuario autenticado (requiere JWT)

        // ========== RUTAS DE RECUPERACION DE CONTRASENA ==========
        router.post("/api/auth/forgot-password", PasswordResetController.solicitarRecuperacion()); // Solicitar enlace de recuperacion (publico)
        router.get("/api/auth/reset-password/validate", PasswordResetController.validarToken());   // Validar que el token no expiro (publico)
        router.post("/api/auth/reset-password", PasswordResetController.cambiarContrasena());      // Cambiar contrasena con el token (publico)

        // ========== RUTAS DE USUARIOS ==========
        router.get("/api/users",    auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN"));                        // Listar todos los usuarios
        router.post("/api/users",   auth.protect(UserController.create(),  "SUPER_ADMIN", "ADMIN"));                        // Crear nuevo usuario
        router.get("/api/users/id", auth.protect(UserController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));            // Obtener usuario por ID
        router.put("/api/users/id", auth.protect(UserController.update(),  "SUPER_ADMIN", "ADMIN", "EMPLEADO"));            // Actualizar usuario completo (PUT)
        router.patch("/api/users/id", auth.protect(UserController.patch(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));            // Actualizar usuario parcial (PATCH)
        // router.delete("/api/users/id", auth.protect(UserController.delete(), "SUPER_ADMIN", "ADMIN"));                   // Desactivar usuario (soft delete) - pendiente de habilitar

        // Imprimir en consola las rutas activas al iniciar el servidor
        System.out.println("Rutas registradas:");
        System.out.println("  POST   /api/auth/login                      (publico)");
        System.out.println("  POST   /api/auth/google                     (publico)");
        System.out.println("  GET    /api/auth/me                         (autenticado)");
        System.out.println("  POST   /api/auth/forgot-password            (publico)");
        System.out.println("  GET    /api/auth/reset-password/validate    (publico)");
        System.out.println("  POST   /api/auth/reset-password             (publico)");
        System.out.println("  GET    /api/users                           (SUPER_ADMIN, ADMIN)");
        System.out.println("  POST   /api/users                           (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/users/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO propio)");
        System.out.println("  PUT    /api/users/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO propio)");
        System.out.println("  PATCH  /api/users/id?id=X                   (SUPER_ADMIN, ADMIN, EMPLEADO propio)");
        // System.out.println("  DELETE /api/users/id?id=X                   (SUPER_ADMIN, ADMIN)");

        // Retornar el router ya configurado para registrarlo en el servidor HTTP
        return router;
    }
}
