package com.backend.helpers;

// Libreria jBCrypt para el hashing seguro de contrasenas
import org.mindrot.jbcrypt.BCrypt;

// Clase auxiliar para encriptar y verificar contrasenas con BCrypt
public class PasswordHelper {

    // Genera el hash BCrypt de una contrasena en texto plano
    // Factor de costo 12: balance entre seguridad y tiempo de computo
    public static String hashPassword(String password) {
        // gensalt(12) genera una sal aleatoria con factor de costo 12
        // hashpw aplica el algoritmo BCrypt y retorna el hash resultante
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verifica si una contrasena en texto plano coincide con un hash BCrypt almacenado
    // Retorna true si coinciden, false si no (nunca compara texto plano directamente)
    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
