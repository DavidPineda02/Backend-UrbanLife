package com.backend.helpers;

// Libreria Gson para la serializacion y deserializacion de JSON
import com.google.gson.Gson;
// Para leer el cuerpo de la peticion como flujo de bytes
import java.io.InputStream;
// Para envolver el InputStream en un lector de texto con codificacion
import java.io.InputStreamReader;
// Para especificar la codificacion UTF-8 al leer el flujo
import java.nio.charset.StandardCharsets;

// Clase auxiliar que centraliza las operaciones de conversion JSON
public class JsonHelper {

    // Instancia compartida de Gson (es thread-safe y reusable)
    private static final Gson gson = new Gson();

    // Deserializa un flujo de bytes (cuerpo HTTP) en un objeto del tipo indicado
    // Parametro T: tipo generico del objeto destino
    public static <T> T fromJson(InputStream cuerpo, Class<T> clase) {
        // Envolver el InputStream con InputStreamReader para leerlo como texto UTF-8
        return gson.fromJson(new InputStreamReader(cuerpo, StandardCharsets.UTF_8), clase);
    }

    // Serializa cualquier objeto Java a su representacion JSON como String
    public static String toJson(Object objeto) {
        return gson.toJson(objeto);
    }
}
