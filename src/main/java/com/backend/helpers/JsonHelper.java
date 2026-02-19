package com.backend.helpers;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonHelper {

    private static final Gson gson = new Gson();

    public static <T> T fromJson(InputStream body, Class<T> clazz) {
        return gson.fromJson(new InputStreamReader(body, StandardCharsets.UTF_8), clazz);
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
