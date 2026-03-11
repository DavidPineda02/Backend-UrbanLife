// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa una imagen asociada a un producto
import com.backend.models.ImagenProducto;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de imágenes
import java.util.ArrayList;
// Para manejar colecciones de imágenes
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con las imágenes de productos en la base de datos.
 * Permite almacenar y consultar las URLs de imágenes asociadas a cada producto.
 */
public class ImagenProductoDAO {

    /**
     * Busca una imagen de producto por su ID en la base de datos.
     * @param id ID de la imagen a buscar
     * @return ImagenProducto encontrada o null si no existe
     */
    public static ImagenProducto findById(int id) {
        // Consulta SQL para buscar una imagen por su ID
        String sql = "SELECT * FROM imagenes_producto WHERE imagen_producto = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID de la imagen como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto ImagenProducto y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error ImagenProductoDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró la imagen
        return null;
    }

    /**
     * Busca todas las imágenes asociadas a un producto específico.
     * @param productoId ID del producto a consultar
     * @return Lista de imágenes del producto especificado
     */
    public static List<ImagenProducto> findByProductoId(int productoId) {
        // Crear lista vacía para almacenar las imágenes encontradas
        List<ImagenProducto> lista = new ArrayList<>();
        // Consulta SQL para buscar imágenes por ID del producto
        String sql = "SELECT * FROM imagenes_producto WHERE producto_id = ? ORDER BY imagen_producto ASC";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del producto como parámetro de la consulta
            consulta.setInt(1, productoId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Recorrer cada registro y agregarlo a la lista como objeto ImagenProducto
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error ImagenProductoDAO.findByProductoId: " + excepcion.getMessage());
        }
        // Retornar la lista de imágenes (vacía si hubo error o no hay datos)
        return lista;
    }

    /**
     * Obtiene todas las imágenes de productos registradas en la base de datos.
     * @return Lista de imágenes ordenadas por ID ascendente
     */
    public static List<ImagenProducto> findAll() {
        // Crear lista vacía para almacenar las imágenes encontradas
        List<ImagenProducto> lista = new ArrayList<>();
        // Consulta SQL para obtener todas las imágenes ordenadas por ID
        String sql = "SELECT * FROM imagenes_producto ORDER BY imagen_producto ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto ImagenProducto
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error ImagenProductoDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de imágenes (vacía si hubo error)
        return lista;
    }

    /**
     * Crea una nueva imagen de producto en la base de datos.
     * @param imagenProducto Objeto ImagenProducto con los datos a insertar
     * @return ImagenProducto creada con su ID generado o null si falló
     */
    public static ImagenProducto create(ImagenProducto imagenProducto) {
        // Consulta SQL para insertar una nueva imagen de producto
        String sql = "INSERT INTO imagenes_producto (url, fecha_registro, producto_id) VALUES (?, ?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer la URL de la imagen como primer parámetro
            consulta.setString(1, imagenProducto.getUrl());
            // Establecer la fecha de registro convertida a java.sql.Date como segundo parámetro
            consulta.setDate(2, Date.valueOf(imagenProducto.getFechaRegistro()));
            // Establecer el ID del producto asociado como tercer parámetro
            consulta.setInt(3, imagenProducto.getProductoId());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto imagenProducto
                if (clavesGeneradas.next()) imagenProducto.setImagenProducto(clavesGeneradas.getInt(1));
                // Retornar la imagen creada con su ID asignado
                return imagenProducto;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error ImagenProductoDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza una imagen de producto existente en la base de datos.
     * @param imagenProducto Objeto ImagenProducto con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(ImagenProducto imagenProducto) {
        // Consulta SQL para actualizar URL, fecha y producto de una imagen
        String sql = "UPDATE imagenes_producto SET url = ?, fecha_registro = ?, producto_id = ? WHERE imagen_producto = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer la nueva URL como primer parámetro
            consulta.setString(1, imagenProducto.getUrl());
            // Establecer la nueva fecha de registro convertida a java.sql.Date como segundo parámetro
            consulta.setDate(2, Date.valueOf(imagenProducto.getFechaRegistro()));
            // Establecer el nuevo ID del producto como tercer parámetro
            consulta.setInt(3, imagenProducto.getProductoId());
            // Establecer el ID de la imagen a actualizar como cuarto parámetro
            consulta.setInt(4, imagenProducto.getImagenProducto());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error ImagenProductoDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina una imagen de producto de la base de datos por su ID.
     * @param id ID de la imagen a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar una imagen por su ID
        String sql = "DELETE FROM imagenes_producto WHERE imagen_producto = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID de la imagen a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error ImagenProductoDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Cuenta cuántas imágenes tiene un producto.
     * @param productoId ID del producto
     * @return número de imágenes registradas
     */
    public static int countByProductoId(int productoId) {
        // SQL para contar las imágenes de un producto
        String sql = "SELECT COUNT(*) FROM imagenes_producto WHERE producto_id = ?";
        // Abrir conexión y preparar consulta con auto-cierre
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar ID del producto
            consulta.setInt(1, productoId);
            // Ejecutar consulta y retornar el conteo
            ResultSet resultado = consulta.executeQuery();
            if (resultado.next()) return resultado.getInt(1);
        } catch (Exception excepcion) {
            // Registrar error en consola
            System.out.println("Error ImagenProductoDAO.countByProductoId: " + excepcion.getMessage());
        }
        return 0;
    }

    /**
     * Mapea una fila del ResultSet a un objeto ImagenProducto.
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto ImagenProducto con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static ImagenProducto mapRow(ResultSet resultado) throws SQLException {
        // Crear y retornar un nuevo objeto ImagenProducto con los valores de las columnas
        return new ImagenProducto(
                resultado.getInt("imagen_producto"),              // Obtener el ID de la imagen
                resultado.getString("url"),                        // Obtener la URL de la imagen
                resultado.getDate("fecha_registro").toLocalDate(), // Obtener la fecha convertida a LocalDate
                resultado.getInt("producto_id"));                  // Obtener el ID del producto asociado
    }
}
