package com.backend.server.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ApiRequest {

    public HttpExchange exchange;

    public ApiRequest(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String readBody() throws IOException {
        InputStream bodyStream = exchange.getRequestBody();
        return new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
