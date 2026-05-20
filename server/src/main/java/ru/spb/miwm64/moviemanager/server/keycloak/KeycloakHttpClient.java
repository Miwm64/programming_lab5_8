package ru.spb.miwm64.moviemanager.server.keycloak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class KeycloakHttpClient {

    private final String baseUrl;

    public KeycloakHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String get(String path) {
        return get(path, null);
    }

    public String get(String path, Map<String, String> headers) {
        try {
            HttpURLConnection connection = buildConnection(
                    "GET",
                    path,
                    headers
            );

            return readResponse(connection);

        } catch (Exception e) {
            throw new RuntimeException("GET request failed", e);
        }
    }

    public String post(String path, String body) {
        return post(path, body, null);
    }

    public String post(
            String path,
            String body,
            Map<String, String> headers
    ) {
        try {
            HttpURLConnection connection = buildConnection(
                    "POST",
                    path,
                    headers
            );

            writeBody(connection, body);

            return readResponse(connection);

        } catch (Exception e) {
            throw new RuntimeException("POST request failed", e);
        }
    }

    public String put(String path, String body) {
        return put(path, body, null);
    }

    public String put(
            String path,
            String body,
            Map<String, String> headers
    ) {
        try {
            HttpURLConnection connection = buildConnection(
                    "PUT",
                    path,
                    headers
            );

            writeBody(connection, body);

            return readResponse(connection);

        } catch (Exception e) {
            throw new RuntimeException("PUT request failed", e);
        }
    }

    public String delete(String path) {
        return delete(path, null);
    }

    public String delete(
            String path,
            Map<String, String> headers
    ) {
        try {
            HttpURLConnection connection = buildConnection(
                    "DELETE",
                    path,
                    headers
            );

            return readResponse(connection);

        } catch (Exception e) {
            throw new RuntimeException("DELETE request failed", e);
        }
    }

    private HttpURLConnection buildConnection(
            String method,
            String path,
            Map<String, String> headers
    ) throws IOException {

        URL url = new URL(baseUrl + path);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }

        if (
                method.equals("POST") ||
                        method.equals("PUT") ||
                        method.equals("PATCH")
        ) {
            connection.setDoOutput(true);
        }

        return connection;
    }

    private void writeBody(
            HttpURLConnection connection,
            String body
    ) throws IOException {

        if (body == null || body.isEmpty()) {
            return;
        }

        try (OutputStream os = connection.getOutputStream()) {

            byte[] input =
                    body.getBytes(StandardCharsets.UTF_8);

            os.write(input);
        }
    }

    private String readResponse(
            HttpURLConnection connection
    ) throws IOException {

        int status = connection.getResponseCode();

        InputStream stream;

        if (status >= 200 && status < 300) {
            stream = connection.getInputStream();
        } else {
            stream = connection.getErrorStream();
        }

        String response = readStream(stream);

        if (status < 200 || status >= 300) {

            throw new RuntimeException(
                    "HTTP " + status + "\n" + response
            );
        }

        return response;
    }

    private String readStream(InputStream stream)
            throws IOException {

        if (stream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}