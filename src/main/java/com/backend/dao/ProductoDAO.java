// Paquete de Data Access Objects (DAOs) de la aplicación
package com.backend.dao;

// Clase para obtener conexión a la base de datos
import com.backend.config.dbConnection;
// Modelo que representa un producto del inventario
import com.backend.models.Producto;

// Clases JDBC para conexión, consultas preparadas, resultados y sentencias
import java.sql.*;
// Lista dinámica para retornar múltiples productos
import java.util.ArrayList;
// Interfaz de lista genérica
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con productos en la base de datos.
 * Centraliza todo el acceso a la tabla Producto evitando SQL disperso en capas superiores.
 */
public class ProductoDAO {

    /**
     * Busca un producto por su ID.
     * @param id ID del producto a buscar
     * @return Producto encontrado o null si no existe
     */
    public static Producto findById(int id) {
        // SQL para seleccionar un producto por su clave primaria
        String sql = "SELECT * FROM Productos WHERE ID_PRODUCTO = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID como parámetro de búsqueda
            consulta.setInt(1, id);
            // Ejecutar consulta y obtener resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProductoDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el producto
        return null;
    }

    /**
     * Obtiene todos los productos de la base de datos ordenados por ID ascendente.
     * @return Lista de productos (vacía si no hay ninguno)
     */
    public static List<Producto> findAll() {
        // Lista donde se acumularán los productos encontrados
        List<Producto> lista = new ArrayList<>();
        // SQL para seleccionar todos los productos ordenados por ID
        String sql = "SELECT * FROM Productos ORDER BY ID_PRODUCTO ASC";
        // Abrir conexión, preparar consulta y ejecutarla con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer todos los registros y agregar cada producto a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProductoDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los productos encontrados
        return lista;
    }

    /**
     * Obtiene todos los productos que pertenecen a una categoría específica.
     * @param categoriaId ID de la categoría cuyos productos se desean obtener
     * @return Lista de productos de esa categoría (vacía si no hay ninguno)
     */
    public static List<Producto> findByCategoriaId(int categoriaId) {
        // Lista donde se acumularán los productos de la categoría
        List<Producto> lista = new ArrayList<>();
        // SQL para seleccionar productos filtrando por categoría y ordenando por ID
        String sql = "SELECT * FROM Productos WHERE CATEGORIA_ID = ? ORDER BY ID_PRODUCTO ASC";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID de la categoría como parámetro de búsqueda
            consulta.setInt(1, categoriaId);
            // Ejecutar consulta y obtener resultados
            ResultSet resultado = consulta.executeQuery();
            // Recorrer todos los registros y agregar cada producto a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProductoDAO.findByCategoriaId: " + excepcion.getMessage());
        }
        // Retornar la lista con todos los productos de esa categoría
        return lista;
    }

    /**
     * Inserta un nuevo producto en la base de datos y asigna el ID generado.
     * @param producto Objeto Producto con todos los campos a insertar
     * @return El mismo Producto con su ID asignado, o null si falló la inserción
     */
    public static Producto create(Producto producto) {
        // SQL para insertar un nuevo producto con todos sus campos
        String sql = "INSERT INTO Productos (NOMBRE_PRODUCTO, DESCRIPCION_PRODUCTO, PRECIO_VENTA, COSTO_PROMEDIO, STOCK, ESTADO_PRODUCTO, CATEGORIA_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Abrir conexión y preparar consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre del producto
            consulta.setString(1, producto.getNombre());
            // Asignar la descripción del producto
            consulta.setString(2, producto.getDescripcion());
            // Asignar el precio de venta del producto
            consulta.setDouble(3, producto.getPrecioVenta());
            // Asignar el costo promedio del producto
            consulta.setDouble(4, producto.getCostoPromedio());
            // Asignar el stock disponible del producto
            consulta.setInt(5, producto.getStock());
            // Asignar el estado del producto
            consulta.setBoolean(6, producto.isEstado());
            // Asignar el ID de la categoría asociada
            consulta.setInt(7, producto.getCategoriaId());
            // Ejecutar INSERT y verificar que se insertó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por la BD (ID auto-incrementado)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si se obtuvo la clave generada, asignarla al objeto
                if (clavesGeneradas.next()) producto.setIdProducto(clavesGeneradas.getInt(1));
                // Retornar el producto con su ID asignado
                return producto;
            }
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProductoDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la inserción falló
        return null;
    }

    /**
     * Actualiza todos los campos de un producto existente en la base de datos.
     * @param producto Objeto Producto con los nuevos datos y el ID a actualizar
     * @return true si se actualizó al menos una fila, false en caso contrario
     */
    public static boolean update(Producto producto) {
        // SQL para actualizar todos los campos del producto por su ID
        String sql = "UPDATE Productos SET NOMBRE_PRODUCTO = ?, DESCRIPCION_PRODUCTO = ?, PRECIO_VENTA = ?, COSTO_PROMEDIO = ?, STOCK = ?, ESTADO_PRODUCTO = ?, CATEGORIA_ID = ? WHERE ID_PRODUCTO = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo nombre del producto
            consulta.setString(1, producto.getNombre());
            // Asignar la nueva descripción del producto
            consulta.setString(2, producto.getDescripcion());
            // Asignar el nuevo precio de venta del producto
            consulta.setDouble(3, producto.getPrecioVenta());
            // Asignar el nuevo costo promedio del producto
            consulta.setDouble(4, producto.getCostoPromedio());
            // Asignar el nuevo stock disponible del producto
            consulta.setInt(5, producto.getStock());
            // Asignar el nuevo estado del producto
            consulta.setBoolean(6, producto.isEstado());
            // Asignar el nuevo ID de categoría del producto
            consulta.setInt(7, producto.getCategoriaId());
            // Asignar el ID del producto a actualizar como condición WHERE
            consulta.setInt(8, producto.getIdProducto());
            // Ejecutar UPDATE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProductoDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Actualiza únicamente el stock de un producto específico.
     * Usado internamente por los módulos de Ventas y Compras para ajustar inventario.
     * @param id ID del producto cuyo stock se actualizará
     * @param nuevoStock Nueva cantidad de unidades en inventario
     * @return true si se actualizó al menos una fila, false en caso contrario
     */
    public static boolean updateStock(int id, int nuevoStock) {
        // SQL para actualizar solo el campo stock de un producto por su ID
        String sql = "UPDATE Productos SET STOCK = ? WHERE ID_PRODUCTO = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo stock como primer parámetro
            consulta.setInt(1, nuevoStock);
            // Asignar el ID del producto como condición WHERE
            consulta.setInt(2, id);
            // Ejecutar UPDATE y retornar true si se afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ProductoDAO.updateStock: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Producto.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto Producto con todos los campos del registro
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Producto mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un Producto con los datos del registro actual
        return new Producto(
                // Leer el ID del producto desde la columna ID_PRODUCTO
                resultado.getInt("ID_PRODUCTO"),
                // Leer el nombre desde la columna NOMBRE_PRODUCTO
                resultado.getString("NOMBRE_PRODUCTO"),
                // Leer la descripción desde la columna DESCRIPCION_PRODUCTO
                resultado.getString("DESCRIPCION_PRODUCTO"),
                // Leer el precio de venta desde la columna PRECIO_VENTA
                resultado.getDouble("PRECIO_VENTA"),
                // Leer el costo promedio desde la columna COSTO_PROMEDIO
                resultado.getDouble("COSTO_PROMEDIO"),
                // Leer el stock desde la columna STOCK
                resultado.getInt("STOCK"),
                // Leer el estado desde la columna ESTADO_PRODUCTO
                resultado.getBoolean("ESTADO_PRODUCTO"),
                // Leer el ID de categoría desde la columna CATEGORIA_ID
                resultado.getInt("CATEGORIA_ID"));
    }
}
