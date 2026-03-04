package com.backend.routes;

import com.backend.controllers.AuthController;
import com.backend.controllers.GoogleAuthController;
import com.backend.controllers.PasswordResetController;
import com.backend.controllers.UserController;
import com.backend.middlewares.AuthMiddleware;
import com.sun.net.httpserver.HttpHandler;

public class Routes {

    Router router = new Router();

    public HttpHandler configureRoutes() {

        AuthMiddleware auth = new AuthMiddleware();

        // ========== RUTAS DE AUTH ==========
        router.post("/api/auth/login", AuthController.login());
        router.post("/api/auth/google", GoogleAuthController.loginWithGoogle());
        router.get("/api/auth/me", auth.protect(AuthController.me()));

        // ========== RUTAS DE RECUPERACION DE CONTRASENA ==========
        router.post("/api/auth/forgot-password", PasswordResetController.solicitarRecuperacion());
        router.get("/api/auth/reset-password/validate", PasswordResetController.validarToken());
        router.post("/api/auth/reset-password", PasswordResetController.cambiarContrasena());

        // ========== RUTAS DE USUARIOS ==========
        router.get("/api/users",    auth.protect(UserController.listAll(), "SUPER_ADMIN", "ADMIN"));
        router.post("/api/users",   auth.protect(UserController.create(),  "SUPER_ADMIN", "ADMIN"));
        router.get("/api/users/id", auth.protect(UserController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        router.put("/api/users/id", auth.protect(UserController.update(),  "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        router.patch("/api/users/id", auth.protect(UserController.patch(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // router.delete("/api/users/id", auth.protect(UserController.delete(), "SUPER_ADMIN"));

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

        return router;
    }
}
