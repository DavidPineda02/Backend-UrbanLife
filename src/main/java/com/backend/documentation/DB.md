# Base de Datos — UrbanLife

Explicación completa de cada entidad y cada atributo del sistema UrbanLife.
El enfoque está en el **flujo dentro del software** y la **relación entre entidades**.

---

## 1. Usuarios

**Flujo:** Es el centro del sistema. Representa a las personas que **inician sesión** y operan el software (SUPER_ADMIN, ADMIN, EMPLEADO). Un usuario registra ventas, compras, gastos y gestiona el inventario. NO es un cliente ni un proveedor — esos son directorios aparte.

| Atributo | Explicación |
|---|---|
| `ID_USUARIO` | Identificador único auto-incremental. Lo usa el JWT como `subject` para saber quién está autenticado en cada petición. |
| `NOMBRE_USUARIO` | Nombre de pila. Se muestra en el sidebar y en la vista de perfil. |
| `APELLIDO_USUARIO` | Apellido. Junto con el nombre, identifica al usuario en el sistema. |
| `GOOGLE_ID` | ID único que Google asigna al usuario cuando se autentica con OAuth 2.0. Es `NULL` si el usuario se registró con correo/contraseña. `UNIQUE` porque un Google ID solo puede pertenecer a una cuenta. |
| `CONTRASENA` | Hash BCrypt de la contraseña. Es `NULL` si el usuario se registró con Google (no necesita contraseña). Entre `GOOGLE_ID` y `CONTRASENA`, al menos uno siempre tiene valor. |
| `ESTADO_USUARIO` | Soft delete. `TRUE` = activo, `FALSE` = inactivo. Un usuario inactivo no puede iniciar sesión. El ADMIN puede desactivar empleados sin borrar su historial de operaciones. |

**Relaciones salientes:** → Correos_Usuarios, Telefonos_Usuarios, Usuarios_Roles, Venta, Compra, Gastos_Adicionales, Tokens_Recuperacion

---

## 2. Roles

**Flujo:** Define **qué nivel de acceso** tiene un usuario. El seeder crea 3 roles fijos: SUPER_ADMIN (desarrollador), ADMIN (dueño del negocio), EMPLEADO (operador limitado). El AuthMiddleware consulta el rol del usuario en cada petición para permitir o bloquear rutas.

| Atributo | Explicación |
|---|---|
| `ID_ROLES` | Identificador único. Referenciado por Usuarios_Roles y Roles_Permisos. |
| `NOMBRE_ROL` | Nombre del rol (ej: "ADMIN"). El middleware lo compara contra los roles permitidos de cada endpoint. |
| `DESCRIPCION_ROL` | Texto descriptivo para saber qué hace el rol (solo informativo). |

**Relaciones salientes:** → Usuarios_Roles, Roles_Permisos

---

## 3. Permisos

**Flujo:** Define **acciones específicas** que se pueden realizar (ej: "GESTIONAR_VENTAS", "GESTIONAR_PRODUCTOS"). Se asocian a roles mediante Rol_Permisos. Permite granularidad: un EMPLEADO puede tener permiso de gestionar ventas pero no de ver usuarios. El frontend usa estos permisos para ocultar/mostrar opciones del sidebar y el backend los valida en cada endpoint como doble protección.

| Atributo | Explicación |
|---|---|
| `ID_PERMISOS` | Identificador único. Referenciado por Roles_Permisos. |
| `NOMBRE_PERMISO` | Clave del permiso (ej: "GESTIONAR_VENTAS"). El backend lo consulta para validar acceso. |
| `DESCRIPCION_PERMISO` | Texto explicativo del permiso (solo informativo). |

**Relaciones salientes:** → Roles_Permisos

---

## 4. Categoria

**Flujo:** Agrupa productos por tipo (ej: "Camisetas", "Pantalones", "Accesorios"). En la vista de productos, el usuario selecciona la categoría de un select. Permite filtrar y organizar el inventario.

| Atributo | Explicación |
|---|---|
| `ID_CATEGORIA` | Identificador único. Referenciado por Productos como FK. |
| `NOMBRE_CATEGORIA` | Nombre visible (ej: "Zapatos"). Se muestra en los selects y en la tabla de productos. |
| `DESCRIPCION_CATEGORIA` | Detalle opcional de la categoría. |
| `ESTADO_CATEGORIA` | Soft delete. `TRUE` = activa. Al desactivarla, los productos de esa categoría siguen existiendo pero no se puede asignar nuevos productos a ella. |

**Relaciones salientes:** → Productos

---

## 5. Tipo_Movimientos

**Flujo:** Tabla catálogo con **3 registros fijos** creados por seeder. Clasifica cada movimiento financiero por su origen y naturaleza contable. El backend la consulta cuando crea un movimiento automáticamente al registrar una venta, compra o gasto.

| Atributo | Explicación |
|---|---|
| `ID_TIPO_MOVIMIENTOS` | Identificador único. 1=Venta, 2=Compra, 3=Gasto Adicional (fijos por seeder). |
| `MOVIMIENTO` | ENUM que indica el origen: `'Venta'`, `'Compra'` o `'Gasto Adicional'`. Se muestra como badge en la tabla de movimientos del frontend. |
| `NATURALEZA` | ENUM que indica si es `'Ingreso'` o `'Egreso'`. Venta=Ingreso, Compra=Egreso, Gasto=Egreso. Las tarjetas resumen del frontend suman por esta naturaleza. |

**Relaciones salientes:** → Movimientos_Financieros

---

## 6. Clientes

**Flujo:** Directorio de personas a quienes se les vende. **No tienen acceso al sistema** — solo son un registro. Al crear una venta, el usuario selecciona un cliente de un select. Tener el cliente asociado permite generar historial de compras por cliente.

| Atributo | Explicación |
|---|---|
| `ID_CLIENTE` | Identificador único. Referenciado por Venta como FK. |
| `NOMBRE_CLIENTE` | Nombre completo del cliente. Se muestra en la tabla de ventas para saber a quién se le vendió. |
| `DOCUMENTO_CLIENTE` | Cédula colombiana (6-10 dígitos). `UNIQUE` porque dos clientes no pueden tener la misma cédula. `NULL` porque puede ser un cliente ocasional sin documento. |
| `CORREO_CLIENTE` | Email de contacto. `UNIQUE` para no duplicar clientes. `NULL` porque no todos los clientes dan su correo. |
| `TELEFONO_CLIENTE` | Teléfono de contacto. `NULL` porque es opcional. |
| `DIRECCION_CLIENTE` | Dirección física. Útil si el negocio hace domicilios. `NULL` porque es opcional. |
| `CIUDAD_CLIENTE` | Ciudad del cliente. `NULL` porque es opcional. |
| `ESTADO_CLIENTE` | Soft delete. `TRUE` = activo. Al desactivar, el cliente no aparece en el select de ventas pero su historial se conserva. |

**Relaciones salientes:** → Venta

---

## 7. Proveedores

**Flujo:** Directorio de empresas/personas a quienes se les compra mercancía. **No tienen acceso al sistema**. Al crear una compra, el usuario selecciona un proveedor. Permite rastrear de dónde viene cada producto comprado.

| Atributo | Explicación |
|---|---|
| `ID_PROVEEDOR` | Identificador único. Referenciado por Compra como FK. |
| `NOMBRE_PROVEEDOR` | Nombre de contacto o empresa. Se muestra en la tabla de compras. |
| `RAZON_SOCIAL` | Nombre legal registrado ante cámara de comercio. `NULL` si es persona natural. |
| `NIT` | Número de Identificación Tributaria colombiano. `UNIQUE` porque no pueden haber dos proveedores con el mismo NIT. `NULL` si es persona natural sin NIT. |
| `CORREO_PROVEEDOR` | Email de contacto. `UNIQUE` para evitar duplicados. `NULL` porque es opcional. |
| `TELEFONO_PROVEEDOR` | Teléfono de contacto (7-10 dígitos). `NULL` porque es opcional. |
| `DIRECCION_PROVEEDOR` | Dirección de la bodega o sede. `NULL` porque es opcional. |
| `CIUDAD_PROVEEDOR` | Ciudad del proveedor. `NULL` porque es opcional. |
| `ESTADO_PROVEEDOR` | Soft delete. Al desactivar, no aparece en el select de compras pero las compras pasadas conservan la referencia. |

**Relaciones salientes:** → Compra

---

## 8. Usuario_Rol (tabla pivote)

**Flujo:** Conecta usuarios con roles. Relación muchos-a-muchos. Cuando el usuario inicia sesión, el AuthMiddleware consulta esta tabla para saber qué rol tiene y decidir si puede acceder a la ruta solicitada.

| Atributo | Explicación |
|---|---|
| `ID_USUARIO_ROL` | Identificador único de la asignación. |
| `USUARIO_ID` | FK → Usuarios. El usuario al que se le asigna el rol. |
| `ROL_ID` | FK → Roles. El rol que se le asigna. |
| `UNIQUE (USUARIO_ID, ROL_ID)` | Evita duplicados: un usuario no puede tener el mismo rol asignado dos veces. |

---

## 9. Rol_Permisos (tabla pivote)

**Flujo:** Conecta roles con permisos. Define qué acciones puede realizar cada rol. El seeder configura las combinaciones iniciales (ej: ADMIN tiene todos los permisos, EMPLEADO tiene un subconjunto).

| Atributo | Explicación |
|---|---|
| `ID_ROL_PERMISO` | Identificador único de la asignación. |
| `ROL_ID` | FK → Roles. El rol al que se le otorga el permiso. |
| `PERMISOS_ID` | FK → Permisos. El permiso otorgado. |
| `UNIQUE (ROL_ID, PERMISOS_ID)` | Evita duplicados: un rol no puede tener el mismo permiso dos veces. |

---

## 10. Correos_Usuarios

**Flujo:** Un usuario puede tener **múltiples correos**. El correo principal (`ES_PRINCIPAL = TRUE`) es el que usa para iniciar sesión y recibir correos de recuperación de contraseña. Los demás son información adicional visible en el perfil.

| Atributo | Explicación |
|---|---|
| `ID_CORREO` | Identificador único del registro. |
| `CORREO_USUARIO` | Dirección de email. `UNIQUE` globalmente porque dos usuarios no pueden compartir el mismo correo. |
| `ES_PRINCIPAL` | `TRUE` = correo de login (solo uno por usuario, protegido por UNIQUE). `NULL` = correo secundario/informativo (puede haber varios porque UNIQUE ignora NULLs en MySQL). |
| `USUARIO_ID` | FK → Usuarios. El dueño de este correo. |
| `UNIQUE (USUARIO_ID, ES_PRINCIPAL)` | Garantiza a nivel de BD que solo exista un correo principal por usuario. |

---

## 11. Telefonos_Usuarios

**Flujo:** Un usuario puede tener **múltiples teléfonos**. El principal (`ES_PRINCIPAL = TRUE`) se muestra en la vista de perfil como el teléfono de contacto. Los demás son adicionales.

| Atributo | Explicación |
|---|---|
| `ID_TELEFONO` | Identificador único del registro. |
| `TELEFONO_USUARIO` | Número de teléfono. |
| `ES_PRINCIPAL` | `TRUE` = teléfono principal mostrado en perfil (solo uno por usuario). `NULL` = teléfono adicional (ilimitados). Misma lógica que en Correos. |
| `USUARIO_ID` | FK → Usuarios. El dueño de este teléfono. |
| `UNIQUE (USUARIO_ID, ES_PRINCIPAL)` | Garantiza un solo teléfono principal por usuario. |

---

## 12. Producto

**Flujo:** Pieza central del inventario. Se crea desde la vista de productos con nombre, precio de venta, categoría e imágenes. Su `STOCK` y `COSTO_PROMEDIO` se actualizan automáticamente con cada compra. Se referencia en Detalle_Venta y Detalle_Compra.

| Atributo | Explicación |
|---|---|
| `ID_PRODUCTO` | Identificador único. Referenciado por Detalle_Venta, Detalle_Compra e Imagenes_Producto. |
| `NOMBRE_PRODUCTO` | Nombre visible en el catálogo (ej: "Camiseta Negra Talla M"). |
| `DESCRIPCION_PRODUCTO` | Detalle opcional del producto. |
| `PRECIO_VENTA` | Precio sugerido al que se vende. Se auto-llena en el formulario de venta, pero el admin puede modificarlo para dar descuentos. |
| `COSTO_PROMEDIO` | Media ponderada de los costos de compra. Se recalcula cada vez que se registra una compra: `(stockActual × costoActual + cantidadNueva × costoNuevo) / (stockActual + cantidadNueva)`. Sirve como piso para validar que el precio de venta garantice mínimo 10% de ganancia. |
| `STOCK` | Cantidad disponible en inventario. Se **incrementa** con cada compra y se **decrementa** con cada venta. Si llega a 0, el frontend impide vender ese producto. |
| `ESTADO_PRODUCTO` | Soft delete. Al desactivar, el producto no aparece en los selects de venta/compra pero su historial en transacciones pasadas se conserva. |
| `CATEGORIA_ID` | FK → Categoria. Clasifica el producto. Se selecciona de un dropdown en el formulario. |

**Relaciones salientes:** → Imagenes_Producto, Detalle_Venta, Detalle_Compra

---

## 13. Imagenes_Producto

**Flujo:** Almacena las imágenes de cada producto. Cuando el usuario sube imágenes en el formulario de producto, se guardan en el servidor (carpeta `uploads/`) y la URL se registra aquí. Se muestran en la tarjeta del producto.

| Atributo | Explicación |
|---|---|
| `IMAGEN_PRODUCTO` | Identificador único de la imagen. |
| `URL` | Ruta del archivo en el servidor (ej: `/uploads/producto_1_img1.jpg`). El frontend la usa como `src` del `<img>`. |
| `FECHA_REGISTRO` | Fecha en que se subió la imagen. Permite ordenar o saber cuándo se actualizó la foto. |
| `PRODUCTO_ID` | FK → Producto. El producto al que pertenece esta imagen. |

---

## 14. Venta

**Flujo:** Se crea cuando el usuario registra una venta desde el modal de ventas. Es **inmutable** (sin UPDATE/DELETE) porque es un registro contable. La transacción atómica hace: crear Venta → crear Detalle_Venta por cada producto → decrementar STOCK → crear Movimiento_Financiero tipo=1 (Ingreso).

| Atributo | Explicación |
|---|---|
| `ID_VENTA` | Identificador único. Referenciado por Detalle_Venta y por Movimientos_Financieros como REFERENCIA_ID cuando tipo=1. |
| `FECHA_VENTA` | Fecha de la transacción. Se usa en los filtros de período del frontend y en los reportes del dashboard. |
| `TOTAL_VENTA` | Suma de todos los subtotales de Detalle_Venta. Es el monto que entra como ingreso al negocio. |
| `METODO_PAGO_VENTA` | Cómo pagó el cliente: `'Transferencia'` o `'Efectivo'`. Útil para el control de caja. |
| `USUARIO_ID` | FK → Usuarios. El empleado/admin que registró la venta. Permite saber quién hizo cada operación. |
| `CLIENTE_ID` | FK → Clientes. A quién se le vendió. Permite historial de compras por cliente. |

**Relaciones salientes:** → Detalle_Venta

---

## 15. Compra

**Flujo:** Se crea cuando el usuario registra una compra de mercancía a un proveedor. **Inmutable** por ser registro contable. La transacción atómica hace: crear Compra → crear Detalle_Compra por cada producto → incrementar STOCK → recalcular COSTO_PROMEDIO → crear Movimiento_Financiero tipo=2 (Egreso).

| Atributo | Explicación |
|---|---|
| `ID_COMPRA` | Identificador único. Referenciado por Detalle_Compra y por Movimientos_Financieros como REFERENCIA_ID cuando tipo=2. |
| `FECHA_COMPRA` | Fecha de la compra. Se usa en filtros y reportes. |
| `TOTAL_COMPRA` | Suma de todos los subtotales de Detalle_Compra. Es el monto que sale como egreso del negocio. |
| `METODO_PAGO_COMPRA` | Método de pago al proveedor. |
| `USUARIO_ID` | FK → Usuarios. Quién registró la compra. |
| `PROVEEDOR_ID` | FK → Proveedores. A quién se le compró la mercancía. |

**Relaciones salientes:** → Detalle_Compra

---

## 16. Detalle_Compra

**Flujo:** Cada fila es **un producto dentro de una compra**. Una compra puede tener múltiples detalles (comprar 3 productos distintos en una sola factura). Al insertarse, el DAO actualiza el STOCK y COSTO_PROMEDIO del producto.

| Atributo | Explicación |
|---|---|
| `ID_DET_COMPRA` | Identificador único del detalle. |
| `CANTIDAD_COMPRA` | Cuántas unidades se compraron de este producto. Se suma al STOCK del producto. |
| `COSTO_UNITARIO` | Precio que cobró el proveedor **por unidad**. Viene del frontend porque el precio del proveedor varía entre compras. Se usa para recalcular COSTO_PROMEDIO. |
| `SUBTOTAL_COMPRA` | `CANTIDAD_COMPRA × COSTO_UNITARIO`. Se suma para obtener TOTAL_COMPRA. |
| `COMPRA_ID` | FK → Compra. La compra a la que pertenece este detalle. |
| `PRODUCTO_ID` | FK → Producto. El producto que se compró. |

---

## 17. Detalle_Venta

**Flujo:** Cada fila es **un producto dentro de una venta**. Guarda una foto histórica del precio al momento de vender, porque el PRECIO_VENTA del producto puede cambiar después.

| Atributo | Explicación |
|---|---|
| `ID_DET_VENTA` | Identificador único del detalle. |
| `CANTIDAD_VENTA` | Cuántas unidades se vendieron. Se resta del STOCK del producto. |
| `PRECIO_UNITARIO` | Precio al que se vendió **en ese momento**. Se auto-llena desde PRECIO_VENTA del producto pero el admin puede ajustarlo (descuento). Debe ser >= COSTO_PROMEDIO × 1.10 para garantizar mínimo 10% de ganancia. Es una **foto histórica**: si mañana el precio sube, esta venta conserva el precio real al que se cobró. |
| `SUBTOTAL_VENTA` | `CANTIDAD_VENTA × PRECIO_UNITARIO`. Se suma para obtener TOTAL_VENTA. |
| `VENTA_ID` | FK → Venta. La venta a la que pertenece este detalle. |
| `PRODUCTO_ID` | FK → Producto. El producto que se vendió. |

---

## 18. Gastos_Adicionales

**Flujo:** Registra gastos operativos que no son compras de mercancía (ej: arriendo, servicios, transporte). **Inmutable**. Al insertarse, crea automáticamente un Movimiento_Financiero tipo=3 (Egreso). Permite al dueño ver a dónde se va el dinero más allá de las compras.

| Atributo | Explicación |
|---|---|
| `ID_GASTOS_ADIC` | Identificador único. Referenciado por Movimientos_Financieros como REFERENCIA_ID cuando tipo=3. |
| `MONTO` | Cuánto costó el gasto. Se registra como egreso en movimientos financieros. |
| `DESCRIPCION_GASTO` | Descripción del gasto (ej: "Pago arriendo local enero"). Se copia al CONCEPTO del movimiento financiero. |
| `FECHA_REGISTRO` | Fecha del gasto. Se copia a la FECHA del movimiento financiero. |
| `METODO_PAGO_GASTO` | Cómo se pagó el gasto. |
| `USUARIO_ID` | FK → Usuarios. Quién registró el gasto. |

---

## 19. Movimientos_Financieros

**Flujo:** **Libro contable central** del sistema. Es de **solo lectura** — nunca se crea manualmente. Se genera automáticamente dentro de la transacción atómica de cada venta (tipo=1, ingreso), compra (tipo=2, egreso) o gasto (tipo=3, egreso). La vista de movimientos muestra esta tabla con tarjetas resumen (total ingresos, total egresos, ganancia neta).

| Atributo | Explicación |
|---|---|
| `ID_MOVS_FINANCIEROS` | Identificador único del movimiento. Se muestra en la primera columna de la tabla del frontend. |
| `CONCEPTO` | Descripción legible del movimiento (ej: "Venta #15 — 3 productos", "Compra #8 a Proveedor X"). Lo genera el backend automáticamente al crear la transacción. |
| `MONTO` | Valor monetario del movimiento. Copia del TOTAL_VENTA, TOTAL_COMPRA o MONTO del gasto. Auto-contenido para que la vista no necesite JOINs condicionales. |
| `FECHA` | Fecha del movimiento. Copia de FECHA_VENTA, FECHA_COMPRA o FECHA_REGISTRO. Permite filtrar por período en el frontend. |
| `TIPO_MOVIMIENTO_ID` | FK → Tipos_Movimientos. Indica el origen (1=Venta, 2=Compra, 3=Gasto) y la naturaleza (Ingreso/Egreso). El frontend hace JOIN para mostrar badges de tipo y naturaleza, y para clasificar en las tarjetas resumen. |
| `VENTA_ID` | FK → Ventas. ID de la venta que generó este movimiento. `NULL` si no es venta. |
| `COMPRA_ID` | FK → Compras. ID de la compra que generó este movimiento. `NULL` si no es compra. |
| `GASTO_ADICIONAL_ID` | FK → Gastos_Adicionales. ID del gasto que generó este movimiento. `NULL` si no es gasto. |

**Nota importante:** La tabla usa 3 FKs separadas (VENTA_ID, COMPRA_ID, GASTO_ADICIONAL_ID) en lugar de una sola FK REFERENCIA_ID. Solo una de estas 3 columnas tendrá valor según el TIPO_MOVIMIENTO_ID: si es venta (1) → VENTA_ID tiene valor, si es compra (2) → COMPRA_ID tiene valor, si es gasto (3) → GASTO_ADICIONAL_ID tiene valor. Las otras dos serán NULL. |

---

## 20. Token_Recuperacion

**Flujo:** Cuando un usuario olvida su contraseña, solicita recuperación por email. El backend genera un token aleatorio, lo guarda aquí y envía un correo (JavaMail) con un enlace que contiene el token. Al hacer clic, el frontend envía el token al backend, que lo valida (no expirado, no usado) y permite cambiar la contraseña.

| Atributo | Explicación |
|---|---|
| `ID_TOKEN` | Identificador único del registro. |
| `USUARIO_ID` | FK → Usuarios. El usuario que solicitó la recuperación. |
| `TOKEN` | String aleatorio único. Se incluye en la URL del correo de recuperación. `UNIQUE` para que el backend pueda buscarlo directamente. |
| `FECHA_EXPIRACION` | Hasta cuándo es válido el token (generalmente 1 hora). Si el usuario hace clic después de esta fecha, se rechaza. |
| `USADO` | `TRUE` si ya se usó para cambiar la contraseña. Evita que el mismo enlace se use dos veces. |
| `FECHA_CREACION` | Cuándo se generó el token. Registro informativo con `DEFAULT CURRENT_TIMESTAMP`. |

---

## Resumen de la Base de Datos

**20 tablas en total:**

| Grupo | Tablas |
|---|---|
| **Autenticación** | Usuarios, Correos_Usuarios, Telefonos_Usuarios, Tokens_Recuperacion |
| **Autorización** | Roles, Permisos, Usuarios_Roles, Roles_Permisos |
| **Inventario** | Categorias, Productos, Imagenes_Productos |
| **Operaciones** | Ventas, Detalles_Ventas, Compras, Detalles_Compras, Gastos_Adicionales |
| **Contabilidad** | Tipos_Movimientos, Movimientos_Financieros |
| **Directorio** | Clientes, Proveedores |
