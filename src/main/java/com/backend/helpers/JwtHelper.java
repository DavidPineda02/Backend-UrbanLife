// Paquete de clases auxiliares de la aplicación
package com.backend.helpers;

// Para leer el JWT_SECRET desde el archivo .env
import io.github.cdimascio.dotenv.Dotenv;
// Claims representa el payload deserializado de un JWT (id, correo, rol, etc.)
import io.jsonwebtoken.Claims;
// Clase principal de la libreria JJWT para construir y parsear tokens
import io.jsonwebtoken.Jwts;
// Para crear la clave criptografica HMAC a partir del secreto
import io.jsonwebtoken.security.Keys;

// Tipo de clave simetrica usada en la firma HMAC-SHA
import javax.crypto.SecretKey;
// Para convertir el secreto de String a bytes con codificacion UTF-8
import java.nio.charset.StandardCharsets;
// Para representar las fechas de emision y expiracion del token
import java.util.Date;

/**
 * Clase auxiliar para generar y validar JSON Web Tokens (JWT).
 * Proporciona métodos estáticos para crear tokens de autenticación
 * y validar tokens recibidos en las peticiones HTTP.
 */
public class JwtHelper {

    // Leer el secreto JWT desde el archivo .env (debe tener minimo 32 caracteres / 256 bits)
    private static final String SECRET = Dotenv.load().get("JWT_SECRET");
    // Construir la clave HMAC usando el secreto convertido a bytes UTF-8
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    // Tiempo de vida del token: 24 horas expresado en milisegundos
    private static final long EXPIRATION = 24 * 60 * 60 * 1000;

    /**
     * Genera y retorna un JWT firmado con los datos de identidad del usuario.
     * Incluye ID, correo y rol como claims personalizados.
     * @param userId ID del usuario a incluir en el token
     * @param correo Correo electrónico del usuario
     * @param rol Rol del usuario en el sistema
     * @return String con el token JWT generado y firmado
     */
    public static String generateToken(int userId, String correo, String rol) {
        return Jwts.builder()
                // Subject = id del usuario como String
                .subject(String.valueOf(userId))
                // Claim personalizado: correo electronico
                .claim("correo", correo)
                // Claim personalizado: rol del sistema
                .claim("rol", rol)
                // Fecha de emision: momento actual
                .issuedAt(new Date())
                // Expira en 24h
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                // Firmar con la clave HMAC
                .signWith(KEY)
                // Serializar al formato JWT compacto (header.payload.signature)
                .compact();
    }

    /**
     * Valida la firma y expiracion de un JWT y retorna su payload con los claims.
     * Verifica que el token no haya sido modificado y no esté expirado.
     * @param token String del token JWT a validar
     * @return Claims objeto con el payload del token y todos los claims
     * @throws io.jsonwebtoken.ExpiredJwtException Si el token ha expirado
     * @throws io.jsonwebtoken.JwtException Si el token es inválido o la firma no coincide
     */
    public static Claims validateToken(String token) {
        return Jwts.parser()
                // Configurar la clave para verificar la firma
                .verifyWith(KEY)
                // Construir el parser configurado
                .build()
                // Parsear y validar el token (lanza excepcion si falla)
                .parseSignedClaims(token)
                // Extraer y retornar el payload con todos los claims
                .getPayload();
    }
}
