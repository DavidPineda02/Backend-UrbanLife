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

// Clase auxiliar para generar y validar JSON Web Tokens (JWT)
public class JwtHelper {

    // Leer el secreto JWT desde el archivo .env (debe tener minimo 32 caracteres / 256 bits)
    private static final String SECRET = Dotenv.load().get("JWT_SECRET");
    // Construir la clave HMAC usando el secreto convertido a bytes UTF-8
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    // Tiempo de vida del token: 24 horas expresado en milisegundos
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24 horas

    // Genera y retorna un JWT firmado con los datos de identidad del usuario
    public static String generateToken(int userId, String correo, String rol) {
        return Jwts.builder()
                .subject(String.valueOf(userId))  // Subject = id del usuario como String
                .claim("correo", correo)           // Claim personalizado: correo electronico
                .claim("rol", rol)                 // Claim personalizado: rol del sistema
                .issuedAt(new Date())              // Fecha de emision: momento actual
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION)) // Expira en 24h
                .signWith(KEY)                     // Firmar con la clave HMAC
                .compact();                        // Serializar al formato JWT compacto (header.payload.signature)
    }

    // Valida la firma y expiracion de un JWT y retorna su payload con los claims
    // Lanza ExpiredJwtException si el token expiro, JwtException si es invalido
    public static Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)          // Configurar la clave para verificar la firma
                .build()                  // Construir el parser configurado
                .parseSignedClaims(token) // Parsear y validar el token (lanza excepcion si falla)
                .getPayload();            // Extraer y retornar el payload con todos los claims
    }
}
