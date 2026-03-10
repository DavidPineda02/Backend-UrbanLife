// Paquete de clases auxiliares de la aplicación
package com.backend.helpers;

/**
 * Clase auxiliar con constantes y métodos de validación reutilizables.
 * Centraliza las expresiones regulares y políticas de validación del sistema.
 */
public class ValidationHelper {

    /** Expresión regular para validar el formato del correo electrónico */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    /** Política de contraseña: min 8 chars, al menos una mayúscula, una minúscula y un número */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    /** Solo letras (incluye acentos, ñ y espacios para nombres compuestos) */
    public static final String NOMBRE_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$";
}
