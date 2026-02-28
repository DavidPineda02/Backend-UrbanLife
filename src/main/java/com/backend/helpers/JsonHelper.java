package com.backend.helpers;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonHelper {

    private static final Gson gson = new Gson();

    public static <T> T fromJson(InputStream cuerpo, Class<T> clase) {
        return gson.fromJson(new InputStreamReader(cuerpo, StandardCharsets.UTF_8), clase);
    }

    public static String toJson(Object objeto) {
        return gson.toJson(objeto);
    }
}
