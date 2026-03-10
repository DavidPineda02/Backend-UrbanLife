// Paquete de acceso a datos (Data Access Object) de la aplicación
package com.backend.dao;

// Para obtener la conexión a la base de datos MySQL
import com.backend.config.dbConnection;
// Entidad que representa un usuario del sistema
import com.backend.models.Usuario;

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
// Para construir listas dinámicas de usuarios
import java.util.ArrayList;
// Para usar el tipo genérico List en el retorno de findAll
import java.util.List;

/**
 * DAO que centraliza todas las operaciones SQL de la tabla Usuarios.
 * Provee métodos para buscar, crear, actualizar y desactivar usuarios.
 * No se elimina físicamente ningún usuario (soft delete por campo estado).
 */
public class UsuarioDAO {

    /**
     * Busca un usuario por su correo electrónico.
     * Se usa en login, registro (verificar duplicados) y recuperación de contraseña.
     * @param correo Correo electrónico a buscar
     * @return Usuario encontrado o null si no existe
     */
    public static Usuario findByCorreo(String correo) {
        // Consulta SQL para buscar usuario por correo exacto
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente con try-with-resources)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el correo como parámetro de la consulta
            consulta.setString(1, correo);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si hay un registro, mapearlo a objeto Usuario y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.findByCorreo: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el usuario o hubo un error
        return null;
    }

    /**
     * Busca un usuario por su ID.
     * Se usa para verificar existencia antes de actualizar y en el endpoint getById.
     * @param id ID del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
    public static Usuario findById(int id) {
        // Consulta SQL para buscar usuario por su ID
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID como parámetro de la consulta
            consulta.setInt(1, id);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si hay un registro, mapearlo a objeto Usuario y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.findById: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el usuario o hubo un error
        return null;
    }

    /**
     * Busca un usuario por su ID de Google (autenticación OAuth2).
     * Se usa en el login con Google para identificar usuarios ya registrados con Google.
     * @param googleId ID de Google del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
    public static Usuario findByGoogleId(String googleId) {
        // Consulta SQL para buscar usuario por su Google ID
        String sql = "SELECT * FROM usuarios WHERE google_id = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el Google ID como parámetro de la consulta
            consulta.setString(1, googleId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si hay un registro, mapearlo a objeto Usuario y retornarlo
            if (resultado.next()) return mapRow(resultado);
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.findByGoogleId: " + excepcion.getMessage());
        }
        // Retornar null si no se encontró el usuario o hubo un error
        return null;
    }

    /**
     * Retorna todos los usuarios ordenados por ID ascendente.
     * Se usa en el endpoint GET /api/users para listar todos los usuarios.
     * @return Lista de todos los usuarios (vacía si no hay registros)
     */
    public static List<Usuario> findAll() {
        // Lista donde se acumularán los usuarios encontrados
        List<Usuario> lista = new ArrayList<>();
        // Consulta SQL para obtener todos los usuarios ordenados por ID
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
        // Abrir conexión, preparar y ejecutar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            // Iterar cada fila del resultado y agregarlo a la lista
            while (resultado.next()) lista.add(mapRow(resultado));
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.findAll: " + excepcion.getMessage());
        }
        // Retornar la lista de usuarios (puede estar vacía si no hay registros)
        return lista;
    }

    /**
     * Inserta un nuevo usuario en la base de datos con contraseña hasheada.
     * Se usa en el registro de nuevos usuarios con correo y contraseña.
     * @param usuario Objeto Usuario con los datos a persistir
     * @return Usuario con el ID generado asignado, o null si falló
     */
    public static Usuario create(Usuario usuario) {
        // Consulta SQL para insertar un nuevo usuario con sus cinco campos
        String sql = "INSERT INTO usuarios (nombre, apellido, correo, contrasena, estado) VALUES (?, ?, ?, ?, ?)";
        // Abrir conexión y preparar la consulta solicitando que retorne las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre al primer parámetro
            consulta.setString(1, usuario.getNombre());
            // Asignar el apellido al segundo parámetro
            consulta.setString(2, usuario.getApellido());
            // Asignar el correo al tercer parámetro
            consulta.setString(3, usuario.getCorreo());
            // Asignar el hash BCrypt de la contraseña al cuarto parámetro
            consulta.setString(4, usuario.getContrasena());
            // Asignar el estado activo al quinto parámetro
            consulta.setBoolean(5, usuario.isEstado());
            // Ejecutar el INSERT y verificar que afectó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por el INSERT (el ID autoincremental)
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Si hay una clave generada, asignarla al objeto usuario
                if (clavesGeneradas.next()) usuario.setIdUsuario(clavesGeneradas.getInt(1));
                // Retornar el usuario ya con su ID asignado
                return usuario;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.create: " + excepcion.getMessage());
        }
        // Retornar null si el INSERT no afectó ninguna fila o hubo un error
        return null;
    }

    /**
     * Inserta un nuevo usuario creado con Google OAuth2 (sin contraseña).
     * Se usa cuando un usuario inicia sesión por primera vez con Google.
     * @param googleId ID único de Google del usuario
     * @param nombre Nombre extraído del token de Google
     * @param apellido Apellido extraído del token de Google
     * @param correo Correo electrónico de la cuenta Google
     * @return Usuario creado con su ID asignado, o null si falló
     */
    public static Usuario createWithGoogle(String googleId, String nombre, String apellido, String correo) {
        // Consulta SQL para insertar usuario de Google: contrasena es NULL y google_id se guarda
        String sql = "INSERT INTO usuarios (nombre, apellido, correo, contrasena, estado, google_id) VALUES (?, ?, ?, NULL, true, ?)";
        // Abrir conexión y preparar la consulta solicitando que retorne las claves generadas
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Asignar el nombre al primer parámetro
            consulta.setString(1, nombre);
            // Asignar el apellido al segundo parámetro
            consulta.setString(2, apellido);
            // Asignar el correo al tercer parámetro
            consulta.setString(3, correo);
            // Asignar el Google ID al cuarto parámetro
            consulta.setString(4, googleId);
            // Ejecutar el INSERT y verificar que afectó al menos una fila
            if (consulta.executeUpdate() > 0) {
                // Obtener las claves generadas por el INSERT
                ResultSet clavesGeneradas = consulta.getGeneratedKeys();
                // Construir el objeto usuario manualmente con los datos insertados
                Usuario nuevo = new Usuario();
                // Asignar el nombre al nuevo usuario
                nuevo.setNombre(nombre);
                // Asignar el apellido al nuevo usuario
                nuevo.setApellido(apellido);
                // Asignar el correo al nuevo usuario
                nuevo.setCorreo(correo);
                // Asignar el Google ID al nuevo usuario
                nuevo.setGoogleId(googleId);
                // Marcar el usuario como activo por defecto
                nuevo.setEstado(true);
                // Si hay una clave generada, asignar el ID al objeto usuario
                if (clavesGeneradas.next()) nuevo.setIdUsuario(clavesGeneradas.getInt(1));
                // Retornar el nuevo usuario ya con su ID asignado
                return nuevo;
            }
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.createWithGoogle: " + excepcion.getMessage());
        }
        // Retornar null si el INSERT no afectó ninguna fila o hubo un error
        return null;
    }

    /**
     * Vincula un ID de Google a un usuario existente que se registró con correo.
     * Permite que cuentas creadas con correo también puedan iniciar sesión con Google.
     * @param usuarioId ID del usuario al que se vinculará Google
     * @param googleId ID de Google a vincular
     * @return true si la vinculación fue exitosa, false si falló
     */
    public static boolean linkGoogleId(int usuarioId, String googleId) {
        // Consulta SQL para actualizar el google_id de un usuario existente
        String sql = "UPDATE usuarios SET google_id = ? WHERE id_usuario = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el Google ID al primer parámetro
            consulta.setString(1, googleId);
            // Asignar el ID del usuario al segundo parámetro (cláusula WHERE)
            consulta.setInt(2, usuarioId);
            // Ejecutar el UPDATE y retornar true si afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.linkGoogleId: " + excepcion.getMessage());
        }
        // Retornar false si el UPDATE no afectó ninguna fila o hubo un error
        return false;
    }

    /**
     * Actualiza los datos principales de un usuario (nombre, apellido, correo, estado).
     * No actualiza la contraseña (eso se hace con updatePassword por separado).
     * @param usuario Objeto Usuario con los datos actualizados
     * @return true si la actualización fue exitosa, false si falló
     */
    public static boolean update(Usuario usuario) {
        // Consulta SQL para actualizar los cuatro campos del usuario por su ID
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, correo = ?, estado = ? WHERE id_usuario = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el nuevo nombre al primer parámetro
            consulta.setString(1, usuario.getNombre());
            // Asignar el nuevo apellido al segundo parámetro
            consulta.setString(2, usuario.getApellido());
            // Asignar el nuevo correo al tercer parámetro
            consulta.setString(3, usuario.getCorreo());
            // Asignar el nuevo estado al cuarto parámetro
            consulta.setBoolean(4, usuario.isEstado());
            // Asignar el ID del usuario a actualizar al quinto parámetro (cláusula WHERE)
            consulta.setInt(5, usuario.getIdUsuario());
            // Ejecutar el UPDATE y retornar true si afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.update: " + excepcion.getMessage());
        }
        // Retornar false si el UPDATE no afectó ninguna fila o hubo un error
        return false;
    }

    /**
     * Actualiza únicamente la contraseña de un usuario.
     * Se usa tras la recuperación de contraseña y al cambiar contraseña desde el perfil.
     * @param id ID del usuario cuya contraseña se actualizará
     * @param contrasenaEncriptada Nueva contraseña ya hasheada con BCrypt
     * @return true si la actualización fue exitosa, false si falló
     */
    public static boolean updatePassword(int id, String contrasenaEncriptada) {
        // Consulta SQL para actualizar solo la contraseña del usuario por su ID
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el hash BCrypt de la nueva contraseña al primer parámetro
            consulta.setString(1, contrasenaEncriptada);
            // Asignar el ID del usuario al segundo parámetro (cláusula WHERE)
            consulta.setInt(2, id);
            // Ejecutar el UPDATE y retornar true si afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.updatePassword: " + excepcion.getMessage());
        }
        // Retornar false si el UPDATE no afectó ninguna fila o hubo un error
        return false;
    }

    /**
     * Desactiva un usuario cambiando su estado a false (soft delete).
     * El usuario no se elimina físicamente de la base de datos.
     * @param id ID del usuario a desactivar
     * @return true si la desactivación fue exitosa, false si falló
     */
    public static boolean updateStatus(int id) {
        // Consulta SQL para desactivar el usuario estableciendo estado = false
        String sql = "UPDATE usuarios SET estado = false WHERE id_usuario = ?";
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del usuario a desactivar al primer parámetro
            consulta.setInt(1, id);
            // Ejecutar el UPDATE y retornar true si afectó al menos una fila
            return consulta.executeUpdate() > 0;
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.updateStatus: " + excepcion.getMessage());
        }
        // Retornar false si el UPDATE no afectó ninguna fila o hubo un error
        return false;
    }

    /**
     * Busca el nombre del rol principal asignado a un usuario.
     * Se usa para incluir el rol en el JWT y en las respuestas de autenticación.
     * @param usuarioId ID del usuario cuyo rol se buscará
     * @return Nombre del rol (ej: "EMPLEADO", "ADMIN") o null si no tiene rol asignado
     */
    public static String findRolByUsuarioId(int usuarioId) {
        // Consulta SQL con JOIN para obtener el nombre del rol del usuario
        String sql = """
                SELECT r.nombre
                FROM roles r
                JOIN usuario_rol ur ON r.id_roles = ur.rol_id
                WHERE ur.usuario_id = ?
                LIMIT 1
                """;
        // Abrir conexión y preparar la consulta (se cierran automáticamente)
        try (Connection conexion = dbConnection.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            // Asignar el ID del usuario al parámetro de la consulta
            consulta.setInt(1, usuarioId);
            // Ejecutar la consulta y obtener el resultado
            ResultSet resultado = consulta.executeQuery();
            // Si hay un registro, retornar el nombre del rol encontrado
            if (resultado.next()) return resultado.getString("nombre");
        } catch (Exception excepcion) {
            // Imprimir el error en consola para identificar fallos de BD
            System.out.println("Error UsuarioDAO.findRolByUsuarioId: " + excepcion.getMessage());
        }
        // Retornar null si el usuario no tiene rol asignado o hubo un error
        return null;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Usuario.
     * Método privado reutilizado por todos los métodos de consulta.
     * Intenta leer google_id sin lanzar excepción si la columna no está disponible.
     * @param resultado Fila del ResultSet con los datos del usuario
     * @return Objeto Usuario con los datos mapeados
     * @throws SQLException si ocurre un error al leer las columnas principales
     */
    private static Usuario mapRow(ResultSet resultado) throws SQLException {
        // Construir el objeto Usuario leyendo las columnas principales de la fila
        Usuario usuario = new Usuario(
                // Leer el ID del usuario
                resultado.getInt("id_usuario"),
                // Leer el nombre del usuario
                resultado.getString("nombre"),
                // Leer el apellido del usuario
                resultado.getString("apellido"),
                // Leer el correo del usuario
                resultado.getString("correo"),
                // Leer el hash BCrypt de la contraseña (puede ser null para cuentas Google)
                resultado.getString("contrasena"),
                // Leer el estado activo/inactivo del usuario
                resultado.getBoolean("estado"));
        // Intentar leer el Google ID de forma segura (puede no estar presente en todas las consultas)
        try {
            // Asignar el Google ID si la columna existe y tiene valor
            usuario.setGoogleId(resultado.getString("google_id"));
        // Ignorar la excepción si la columna google_id no está en el ResultSet
        } catch (SQLException ignored) {}
        // Retornar el usuario con todos los campos mapeados
        return usuario;
    }
}
