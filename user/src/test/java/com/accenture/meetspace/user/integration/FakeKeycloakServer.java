package com.accenture.meetspace.user.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

final class FakeKeycloakServer implements AutoCloseable {

    record Request(String method, String path, String query, String contentType, String authorization,
                   String body) {
    }

    private record Response(int status, String body, Map<String, String> headers) {
    }

    private final HttpServer server;
    private final Map<String, Response> responses = new ConcurrentHashMap<>();
    private final List<Request> requests = new CopyOnWriteArrayList<>();

    FakeKeycloakServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    String url() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    void respond(String method, String path, int status, String body) {
        responses.put(method + " " + path, new Response(status, body, Map.of()));
    }

    void respondCreated(String method, String path, String location) {
        responses.put(method + " " + path, new Response(201, "", Map.of("Location", location)));
    }

    List<Request> requests() {
        return List.copyOf(requests);
    }

    List<Request> requests(String method, String path) {
        return requests.stream()
                .filter(request -> request.method().equals(method) && request.path().equals(path))
                .toList();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            var method = exchange.getRequestMethod();
            var path = exchange.getRequestURI().getPath();
            requests.add(new Request(method, path, exchange.getRequestURI().getQuery(),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            var response = responses.getOrDefault(method + " " + path, new Response(404, "", Map.of()));
            response.headers().forEach(exchange.getResponseHeaders()::add);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            var body = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(response.status(), body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                exchange.getResponseBody().write(body);
            }
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
