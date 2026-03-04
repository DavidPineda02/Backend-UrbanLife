package com.backend.helpers; // Paquete de clases auxiliares de la aplicación

// Libreria jBCrypt para el hashing seguro de contrasenas
import org.mindrot.jbcrypt.BCrypt; // Biblioteca para encriptación BCrypt

/**
 * Clase auxiliar para encriptar y verificar contraseñas con BCrypt.
 * Proporciona métodos estáticos para hashear contraseñas de forma segura
 * y verificar contraseñas contra hashes almacenados.
 */
public class PasswordHelper {

    /**
     * Genera el hash BCrypt de una contraseña en texto plano.
     * Utiliza un factor de costo de 12 para balance entre seguridad y rendimiento.
     * @param password Contraseña en texto plano a hashear
     * @return String con el hash BCrypt generado (incluye la sal)
     */
    public static String hashPassword(String password) {
        // gensalt(12) genera una sal aleatoria con factor de costo 12
        // hashpw aplica el algoritmo BCrypt y retorna el hash resultante
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // Generar hash con sal aleatoria
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt almacenado.
     * Extrae la sal del hash y la utiliza para verificar la contraseña.
     * @param password Contraseña en texto plano a verificar
     * @param hash Hash BCrypt almacenado contra el que se verificará
     * @return true si la contraseña coincide, false si no coincide
     */
    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash); // Verificar contraseña contra hash
    }
}
