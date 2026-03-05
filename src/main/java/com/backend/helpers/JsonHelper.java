// Paquete de clases auxiliares de la aplicación
package com.backend.helpers;

// Libreria Gson para la serializacion y deserializacion de JSON
import com.google.gson.Gson;
// Para leer el cuerpo de la peticion como flujo de bytes
import java.io.InputStream;
// Para envolver el InputStream en un lector de texto con codificacion
import java.io.InputStreamReader;
// Para especificar la codificacion UTF-8 al leer el flujo
import java.nio.charset.StandardCharsets;

/**
 * Clase auxiliar que centraliza las operaciones de conversión JSON.
 * Proporciona métodos estáticos para serializar y deserializar objetos.
 * Utiliza la biblioteca Gson para el manejo de JSON.
 */
public class JsonHelper {

    // Instancia compartida de Gson (es thread-safe y reusable)
    private static final Gson gson = new Gson();

    /**
     * Deserializa un flujo de bytes (cuerpo HTTP) en un objeto del tipo indicado.
     * Convierte un InputStream directamente a un objeto Java tipado.
     * @param <T> Tipo genérico del objeto destino
     * @param cuerpo Flujo de entrada que contiene el JSON a deserializar
     * @param clase Clase del objeto destino para la conversión
     * @return Objeto del tipo especificado con los datos del JSON
     */
    public static <T> T fromJson(InputStream cuerpo, Class<T> clase) {
        // Envolver el InputStream con InputStreamReader para leerlo como texto UTF-8
        return gson.fromJson(new InputStreamReader(cuerpo, StandardCharsets.UTF_8), clase);
    }

    /**
     * Serializa cualquier objeto Java a su representación JSON como String.
     * Convierte objetos Java a cadenas de texto en formato JSON.
     * @param objeto Objeto Java a serializar
     * @return String con la representación JSON del objeto
     */
    public static String toJson(Object objeto) {
        // Convertir objeto a JSON string
        return gson.toJson(objeto);
    }
}
