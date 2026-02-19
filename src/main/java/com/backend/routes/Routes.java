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
        router.delete("/api/users/id", UserController.delete());

        System.out.println("Rutas registradas:");
        System.out.println("  POST   /api/auth/login       (publico)");
        System.out.println("  GET    /api/auth/me           (protegido)");
        System.out.println("  GET    /api/users             (publico)");
        System.out.println("  POST   /api/users             (publico)");
        System.out.println("  GET    /api/users/id?id=X     (publico)");
        System.out.println("  PUT    /api/users/id?id=X     (publico)");
        System.out.println("  DELETE /api/users/id?id=X     (publico)");

        return router;
    }
}
