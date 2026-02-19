package com.backend.helpers;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHelper {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
