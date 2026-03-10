// Paquete de acceso a datos (Data Access Object) de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Entidad que representa una categoría del sistema
import com.backend.models.Categoria;

// Para manejar la conexión con la base de datos
import java.sql.Connection;
// Para ejecutar consultas SQL con parámetros seguros
import java.sql.PreparedStatement;
// Para obtener los resultados de las consultas SELECT
import java.sql.ResultSet;
// Para indicar que se retornen las claves generadas tras un INSERT
import java.sql.Statement;
// Para manejar excepciones propias de SQL
import java.sql.SQLException;
// Para construir listas dinámicas de categorías
import java.util.ArrayList;
// Para usar el tipo genérico List en el retorno de findAll
import java.util.List;

/**
 * DAO que centraliza todas las operaciones SQL de la tabla Categoria.
 * Provee métodos para buscar, crear y actualizar categorías en la base de datos.
 * No se elimina físicamente ninguna categoría (soft delete por estado).
 */
public class CategoriaDAO {

    /**
     * Busca una categoría por su ID.
     * @param id ID de la categoría a buscar
     * @return Categoria encontrada o null si no existe
     */
    public static Categoria findById(int id) {
        // Consulta SQL para buscar la categoría por su ID
        String sql = "SELECT * FROM categoria WHERE id_categoria = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente con try-with-resources)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si hay un registro, mapearlo a objeto Categoria y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error CategoriaDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró la categoría o hubo un error
        return null;
    }

    /**
     * Retorna todas las categorías ordenadas por ID ascendente.
     * @return Lista de todas las categorías (vacía si no hay registros)
     */
    public static List<Categoria> findAll() {
        // Lista donde se acumularán las categorías encontradas
        List<Categoria> lista = new ArrayList<>();
        // Consulta SQL para obtener todas las categorías ordenadas por ID
        String sql = "SELECT * FROM categoria ORDER BY id_categoria ASC";
        // Abrir conexión, preparar y ejecutar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Iterar cada fila del resultado y agregarlo a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error CategoriaDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de categorías (puede estar vacía si no hay registros)
        return lista;
    }

    /**
     * Busca una categoría por su nombre exacto.
     * Se usa para verificar unicidad antes de crear o actualizar.
     * @param nombre Nombre a buscar
     * @return Categoria encontrada o null si no existe
     */
    public static Categoria findByNombre(String nombre) {
        // Consulta SQL para buscar la categoría por nombre exacto
        String sql = "SELECT * FROM categoria WHERE nombre = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nombre como parámetro de la consulta
            consulta.setString(1, nombre);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si existe un registro con ese nombre, mapearlo y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error CategoriaDAO.findByNombre: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró ninguna categoría con ese nombre
        return null;
    }

    /**
     * Inserta una nueva categoría en la base de datos.
     * @param categoria Objeto Categoria con los datos a persistir
     * @return Categoria con el ID generado asignado, o null si falló
     */
    public static Categoria create(Categoria categoria) {
        // Consulta SQL para insertar una nueva categoría con sus tres campos
        String sql = "INSERT INTO categoria (nombre, descripcion, estado) VALUES (?, ?, ?)";
        // Abrir conexión y preparar la consulta solicitando que retorne las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre de la categoría al primer parámetro
            consulta.setString(1, categoria.getNombre());
            // Asignar la descripción de la categoría al segundo parámetro
            consulta.setString(2, categoria.getDescripcion());
            // Asignar el estado de la categoría al tercer parámetro
            consulta.setBoolean(3, categoria.isEstado());
            // Ejecutar el INSERT y verificar que afectó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por el INSERT (el ID autoincremental)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto categoría
                if (clavesGeneradas.next()) categoria.setIdCategoria(clavesGeneradas.getInt(1));
                // Retornar la categoría ya con su ID asignado
                return categoria;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error CategoriaDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si el INSERT no afectó ninguna fila o hubo un error
        return null;
    }

    /**
     * Actualiza todos los campos de una categoría existente.
     * Se usa tanto para PUT (actualización completa) como para PATCH (solo estado).
     * @param categoria Objeto Categoria con los datos actualizados
     * @return true si la actualización fue exitosa, false si falló
     */
    public static boolean update(Categoria categoria) {
        // Consulta SQL para actualizar los tres campos de la categoría por su ID
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ?, estado = ? WHERE id_categoria = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo nombre al primer parámetro
            consulta.setString(1, categoria.getNombre());
            // Asignar la nueva descripción al segundo parámetro
            consulta.setString(2, categoria.getDescripcion());
            // Asignar el nuevo estado al tercer parámetro
            consulta.setBoolean(3, categoria.isEstado());
            // Asignar el ID de la categoría a actualizar al cuarto parámetro (cláusula WHERE)
            consulta.setInt(4, categoria.getIdCategoria());
            // Ejecutar el UPDATE y retornar true si afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error CategoriaDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si el UPDATE no afectó ninguna fila o hubo un error
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Categoria.
     * Método privado reutilizado por todos los métodos de consulta.
     * @param resultado Fila del ResultSet con los datos de la categoría
     * @return Objeto Categoria con los datos mapeados
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private static Categoria mapRow(ResultSet resultado) throws SQLException {
        // Construir y retornar un objeto Categoria leyendo cada columna de la fila
        return new Categoria(
                // Leer el ID de la categoría
                resultado.getInt("id_categoria"),
                // Leer el nombre de la categoría
                resultado.getString("nombre"),
                // Leer la descripción de la categoría
                resultado.getString("descripcion"),
                // Leer el estado activo/inactivo de la categoría
                resultado.getBoolean("estado"));
    }
}
