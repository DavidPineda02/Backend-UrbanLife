// Paquete de acceso a datos de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Modelo que representa un teléfono asociado a un usuario
import com.backend.models.TelefonoUsuario;

// Para gestionar la conexión a la base de datos
import java.sql.*;
// Para crear listas dinámicas de teléfonos
import java.util.ArrayList;
// Para manejar colecciones de teléfonos
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar operaciones CRUD y consultas
 * relacionadas con los teléfonos de usuarios en la base de datos.
 * Un usuario puede tener múltiples teléfonos asociados con ES_PRINCIPAL:
 * TRUE = teléfono principal (uno por usuario), NULL = secundario (ilimitados).
 */
public class TelefonoUsuarioDAO {

    /**
     * Busca un teléfono de usuario por su ID en la base de datos.
     * @param id ID del teléfono a buscar
     * @return TelefonoUsuario encontrado o null si no existe
     */
    public static TelefonoUsuario findById(int id) {
        // Consulta SQL para buscar un teléfono por su ID
        String sql = "SELECT * FROM Telefonos_Usuarios WHERE ID_TELEFONO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del teléfono como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si se encontró un registro, mapearlo a un objeto TelefonoUsuario y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el teléfono
        return null;
    }

    /**
     * Busca todos los teléfonos asociados a un usuario específico.
     * @param usuarioId ID del usuario a consultar
     * @return Lista de teléfonos del usuario especificado
     */
    public static List<TelefonoUsuario> findByUsuarioId(int usuarioId) {
        // Crear lista vacía para almacenar los teléfonos encontrados
        List<TelefonoUsuario> lista = new ArrayList<>();
        // Consulta SQL para buscar teléfonos por ID del usuario
        String sql = "SELECT * FROM Telefonos_Usuarios WHERE USUARIO_ID = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del usuario como parámetro de la consulta
            consulta.setInt(1, usuarioId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Recorrer cada registro y agregarlo a la lista como objeto TelefonoUsuario
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.findByUsuarioId: " + excepcion.getMessage());
        }
        // Retornar la lista de teléfonos (vacía si hubo error o no hay datos)
        return lista;
    }

    /**
     * Obtiene todos los teléfonos de usuarios registrados en la base de datos.
     * @return Lista de teléfonos ordenados por ID ascendente
     */
    public static List<TelefonoUsuario> findAll() {
        // Crear lista vacía para almacenar los teléfonos encontrados
        List<TelefonoUsuario> lista = new ArrayList<>();
        // Consulta SQL para obtener todos los teléfonos ordenados por ID
        String sql = "SELECT * FROM Telefonos_Usuarios ORDER BY ID_TELEFONO ASC";
        // Abrir conexión, preparar y ejecutar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Recorrer cada registro y agregarlo a la lista como objeto TelefonoUsuario
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de teléfonos (vacía si hubo error)
        return lista;
    }

    /**
     * Verifica si un teléfono ya está registrado para un usuario específico.
     * @param telefono Número de teléfono a verificar
     * @param usuarioId ID del usuario al que pertenece el teléfono
     * @return true si el teléfono ya existe para ese usuario, false si no
     */
    public static boolean existsByTelefonoAndUsuarioId(String telefono, int usuarioId) {
        // Consulta SQL para verificar si el teléfono ya existe para el usuario
        String sql = "SELECT COUNT(*) FROM Telefonos_Usuarios WHERE TELEFONO_USUARIO = ? AND USUARIO_ID = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el número de teléfono como primer parámetro
            consulta.setString(1, telefono);
            // Establecer el ID del usuario como segundo parámetro
            consulta.setInt(2, usuarioId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si hay resultado, verificar si el conteo es mayor a 0
            if (resultado.next()) return resultado.getInt(1) > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.existsByTelefonoAndUsuarioId: " + excepcion.getMessage());
        }
        // Retornar false si hubo error (asumir que no existe)
        return false;
    }

    /**
     * Crea un nuevo teléfono de usuario en la base de datos.
     * @param telefonoUsuario Objeto TelefonoUsuario con los datos a insertar
     * @return TelefonoUsuario creado con su ID generado o null si falló
     */
    public static TelefonoUsuario create(TelefonoUsuario telefonoUsuario) {
        // Consulta SQL para insertar un nuevo teléfono de usuario con ES_PRINCIPAL
        String sql = "INSERT INTO Telefonos_Usuarios (TELEFONO_USUARIO, ES_PRINCIPAL, USUARIO_ID) VALUES (?, ?, ?)";
        // Abrir conexión y preparar la consulta solicitando las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer el número de teléfono como primer parámetro
            consulta.setString(1, telefonoUsuario.getTelefono());
            // Establecer ES_PRINCIPAL (TRUE para principal, NULL para secundario)
            if (telefonoUsuario.getEsPrincipal() != null) {
                // Asignar TRUE si es teléfono principal
                consulta.setBoolean(2, telefonoUsuario.getEsPrincipal());
            } else {
                // Asignar NULL si es teléfono secundario (el UNIQUE ignora NULLs)
                consulta.setNull(2, Types.BOOLEAN);
            }
            // Establecer el ID del usuario asociado como tercer parámetro
            consulta.setInt(3, telefonoUsuario.getUsuarioId());
            // Ejecutar la inserción y verificar que se insertó al menos un registro
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas automáticamente por la BD
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto telefonoUsuario
                if (clavesGeneradas.next()) telefonoUsuario.setIdTelefono(clavesGeneradas.getInt(1));
                // Retornar el teléfono creado con su ID asignado
                return telefonoUsuario;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si la creación falló
        return null;
    }

    /**
     * Actualiza un teléfono de usuario existente en la base de datos.
     * @param telefonoUsuario Objeto TelefonoUsuario con los datos actualizados
     * @return true si se actualizó correctamente, false si falló
     */
    public static boolean update(TelefonoUsuario telefonoUsuario) {
        // Consulta SQL para actualizar el número de teléfono
        String sql = "UPDATE Telefonos_Usuarios SET TELEFONO_USUARIO = ? WHERE ID_TELEFONO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el nuevo número de teléfono como primer parámetro
            consulta.setString(1, telefonoUsuario.getTelefono());
            // Establecer el ID del teléfono a actualizar como segundo parámetro
            consulta.setInt(2, telefonoUsuario.getIdTelefono());
            // Ejecutar la actualización y retornar true si se modificó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si la actualización falló
        return false;
    }

    /**
     * Elimina un teléfono de usuario de la base de datos por su ID.
     * @param id ID del teléfono a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
    public static boolean delete(int id) {
        // Consulta SQL para eliminar un teléfono por su ID
        String sql = "DELETE FROM Telefonos_Usuarios WHERE ID_TELEFONO = ?";
        // Abrir conexión y preparar la consulta con try-with-resources
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Establecer el ID del teléfono a eliminar como parámetro
            consulta.setInt(1, id);
            // Ejecutar la eliminación y retornar true si se eliminó al menos un registro
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para depuración
            System.out.println("Error TelefonoUsuarioDAO.delete: " + excepcion.getMessage());
        }
        // Retornar false si la eliminación falló
        return false;
    }

    /**
     * Mapea una fila del ResultSet a un objeto TelefonoUsuario.
     * Maneja ES_PRINCIPAL como Boolean nullable (TRUE o NULL).
     * @param resultado ResultSet posicionado en la fila a mapear
     * @return Objeto TelefonoUsuario con los datos de la fila
     * @throws SQLException Si ocurre un error al leer las columnas
     */
    private static TelefonoUsuario mapRow(ResultSet resultado) throws SQLException {
        // Leer ES_PRINCIPAL como boolean primitivo (retorna false si es NULL)
        boolean esPrincipalValor = resultado.getBoolean("ES_PRINCIPAL");
        // Convertir a Boolean nullable: null si era NULL en la BD, TRUE si era TRUE
        Boolean esPrincipal = resultado.wasNull() ? null : esPrincipalValor;
        // Crear y retornar un nuevo objeto TelefonoUsuario con los valores de las columnas
        return new TelefonoUsuario(
                // Obtener el ID del teléfono
                resultado.getInt("ID_TELEFONO"),
                // Obtener el número de teléfono desde TELEFONO_USUARIO
                resultado.getString("TELEFONO_USUARIO"),
                // Asignar si es principal (Boolean nullable)
                esPrincipal,
                // Obtener el ID del usuario asociado
                resultado.getInt("USUARIO_ID"));
    }
}
