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
import com.backend.controllers.VentaController;
import com.backend.controllers.CompraController;
// Controller para gestionar gastos adicionales del negocio
import com.backend.controllers.GastoAdicionalController;
// Controller para gestionar correos adicionales del perfil de usuario
import com.backend.controllers.CorreoUsuarioController;
// Controller para gestionar números telefónicos del perfil de usuario
import com.backend.controllers.NumeroUsuarioController;
// Controller para el módulo de reportes y métricas del dashboard
import com.backend.controllers.DashboardController;
// Controller para el módulo de imágenes de productos
import com.backend.controllers.ImagenProductoController;
// Middleware para proteger rutas con autenticacion JWT y control de roles
import com.backend.middlewares.AuthMiddleware;
// Interfaz del manejador HTTP de Java
import com.sun.net.httpserver.HttpHandler;

/**
 * Clase que registra todas las rutas de la API en el Router.
 * Configura endpoints de autenticación, usuarios, categorías, productos, clientes, proveedores, ventas, compras y gastos adicionales.
 * Centraliza la configuración y protección por roles de todas las rutas del sistema.
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
        // Listar todas las categorías (solo administradores)
        router.get("/api/categorias",    auth.protect(CategoriaController.listAll(), "SUPER_ADMIN", "ADMIN"));
        // Obtener categoría por ID (solo administradores)
        router.get("/api/categorias/id", auth.protect(CategoriaController.getById(), "SUPER_ADMIN", "ADMIN"));
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
        // Crear nuevo cliente (empleados también pueden agregar clientes)
        router.post("/api/clientes",   auth.protect(ClienteController.create(),  "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Actualizar cliente completo
        router.put("/api/clientes/id", auth.protect(ClienteController.update(),  "SUPER_ADMIN", "ADMIN"));
        // Cambiar estado activo/inactivo
        router.patch("/api/clientes/id", auth.protect(ClienteController.patch(), "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE PROVEEDORES ==========
        // Listar todos los proveedores (solo administradores)
        router.get("/api/proveedores",    auth.protect(ProveedorController.listAll(), "SUPER_ADMIN", "ADMIN"));
        // Obtener proveedor por ID (solo administradores)
        router.get("/api/proveedores/id", auth.protect(ProveedorController.getById(), "SUPER_ADMIN", "ADMIN"));
        // Crear nuevo proveedor
        router.post("/api/proveedores",   auth.protect(ProveedorController.create(),  "SUPER_ADMIN", "ADMIN"));
        // Actualizar proveedor completo
        router.put("/api/proveedores/id", auth.protect(ProveedorController.update(),  "SUPER_ADMIN", "ADMIN"));
        // Cambiar estado activo/inactivo
        router.patch("/api/proveedores/id", auth.protect(ProveedorController.patch(), "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE VENTAS ==========
        // Listar todas las ventas
        router.get("/api/ventas",    auth.protect(VentaController.listAll(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Obtener venta por ID (incluye detalles)
        router.get("/api/ventas/id", auth.protect(VentaController.getById(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Registrar nueva venta (transacción atómica)
        router.post("/api/ventas",   auth.protect(VentaController.create(),  "SUPER_ADMIN", "ADMIN", "EMPLEADO"));

        // ========== RUTAS DE COMPRAS ==========
        // Listar todas las compras
        router.get("/api/compras",    auth.protect(CompraController.listAll(), "SUPER_ADMIN", "ADMIN"));
        // Obtener compra por ID (incluye detalles)
        router.get("/api/compras/id", auth.protect(CompraController.getById(), "SUPER_ADMIN", "ADMIN"));
        // Registrar nueva compra (transacción atómica)
        router.post("/api/compras",   auth.protect(CompraController.create(),  "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE GASTOS ADICIONALES ==========
        // Listar todos los gastos adicionales
        router.get("/api/gastos",    auth.protect(GastoAdicionalController.listAll(), "SUPER_ADMIN", "ADMIN"));
        // Obtener gasto adicional por ID
        router.get("/api/gastos/id", auth.protect(GastoAdicionalController.getById(), "SUPER_ADMIN", "ADMIN"));
        // Registrar nuevo gasto adicional (transacción atómica)
        router.post("/api/gastos",   auth.protect(GastoAdicionalController.create(),  "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE IMÁGENES DE PRODUCTOS ==========
        // Subir imagen a un producto (Base64 JSON)
        router.post("/api/productos/imagen",   auth.protect(ImagenProductoController.upload(),         "SUPER_ADMIN", "ADMIN"));
        // Obtener imágenes de un producto
        router.get("/api/productos/imagen",    auth.protect(ImagenProductoController.getByProductoId(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Eliminar imagen por ID
        router.delete("/api/productos/imagen", auth.protect(ImagenProductoController.delete(),          "SUPER_ADMIN", "ADMIN"));

        // ========== RUTAS DE CORREOS DE USUARIO (PERFIL) ==========
        // Listar correos adicionales del usuario autenticado
        router.get("/api/correos-usuario",       auth.protect(CorreoUsuarioController.listByUsuario(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Agregar un correo adicional al perfil
        router.post("/api/correos-usuario",      auth.protect(CorreoUsuarioController.create(),        "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Eliminar un correo adicional del perfil
        router.delete("/api/correos-usuario",    auth.protect(CorreoUsuarioController.delete(),        "SUPER_ADMIN", "ADMIN", "EMPLEADO"));

        // ========== RUTAS DE NÚMEROS DE USUARIO (PERFIL) ==========
        // Listar números telefónicos del usuario autenticado
        router.get("/api/numeros-usuario",       auth.protect(NumeroUsuarioController.listByUsuario(), "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Agregar un número telefónico al perfil
        router.post("/api/numeros-usuario",      auth.protect(NumeroUsuarioController.create(),        "SUPER_ADMIN", "ADMIN", "EMPLEADO"));
        // Eliminar un número telefónico del perfil
        router.delete("/api/numeros-usuario",    auth.protect(NumeroUsuarioController.delete(),        "SUPER_ADMIN", "ADMIN", "EMPLEADO"));

        // ========== RUTAS DE DASHBOARD ==========
        // Tarjetas: ingresos/egresos del día, ingresos/egresos/ganancia del mes, contadores
        router.get("/api/dashboard/resumen",             auth.protect(DashboardController.getResumen(),           "SUPER_ADMIN", "ADMIN"));
        // Gráfico de barras: ventas por día en los últimos 7 días
        router.get("/api/dashboard/ventas-semanales",    auth.protect(DashboardController.getVentasSemanales(),   "SUPER_ADMIN", "ADMIN"));
        // Gráfico de barras agrupadas: ingresos, egresos y ganancia neta por día (últimos 7 días)
        router.get("/api/dashboard/resumen-semanal",     auth.protect(DashboardController.getResumenSemanal(),    "SUPER_ADMIN", "ADMIN"));
        // Gráfico de dona: stock total agrupado por categoría (solo productos activos)
        router.get("/api/dashboard/stock-categorias",    auth.protect(DashboardController.getStockPorCategoria(), "SUPER_ADMIN", "ADMIN"));
        // Lista: top 10 productos más rentables por margen absoluto
        router.get("/api/dashboard/productos-rentables", auth.protect(DashboardController.getProductosRentables(),"SUPER_ADMIN", "ADMIN"));

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
        System.out.println("  GET    /api/categorias                       (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/categorias/id?id=X               (SUPER_ADMIN, ADMIN)");
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
        System.out.println("  POST   /api/clientes                           (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  PUT    /api/clientes/id?id=X                   (SUPER_ADMIN, ADMIN)");
        System.out.println("  PATCH  /api/clientes/id?id=X                   (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/proveedores                         (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/proveedores/id?id=X                 (SUPER_ADMIN, ADMIN)");
        System.out.println("  POST   /api/proveedores                         (SUPER_ADMIN, ADMIN)");
        System.out.println("  PUT    /api/proveedores/id?id=X                 (SUPER_ADMIN, ADMIN)");
        System.out.println("  PATCH  /api/proveedores/id?id=X                 (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/ventas                                (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  GET    /api/ventas/id?id=X                        (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  POST   /api/ventas                                (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  GET    /api/compras                               (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/compras/id?id=X                       (SUPER_ADMIN, ADMIN)");
        System.out.println("  POST   /api/compras                               (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/gastos                                (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/gastos/id?id=X                        (SUPER_ADMIN, ADMIN)");
        System.out.println("  POST   /api/gastos                                (SUPER_ADMIN, ADMIN)");
        System.out.println("  POST   /api/productos/imagen?id=X                  (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/productos/imagen?id=X                  (SUPER_ADMIN, ADMIN, EMPLEADO)");
        System.out.println("  DELETE /api/productos/imagen?id=X                  (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/correos-usuario                        (autenticado)");
        System.out.println("  POST   /api/correos-usuario                        (autenticado)");
        System.out.println("  DELETE /api/correos-usuario?id=X                   (autenticado)");
        System.out.println("  GET    /api/numeros-usuario                        (autenticado)");
        System.out.println("  POST   /api/numeros-usuario                        (autenticado)");
        System.out.println("  DELETE /api/numeros-usuario?id=X                   (autenticado)");
        System.out.println("  GET    /api/dashboard/resumen                     (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/dashboard/ventas-semanales            (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/dashboard/resumen-semanal             (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/dashboard/stock-categorias            (SUPER_ADMIN, ADMIN)");
        System.out.println("  GET    /api/dashboard/productos-rentables         (SUPER_ADMIN, ADMIN)");

        // Retornar el router ya configurado para registrarlo en el servidor HTTP
        return router;
    }
}
