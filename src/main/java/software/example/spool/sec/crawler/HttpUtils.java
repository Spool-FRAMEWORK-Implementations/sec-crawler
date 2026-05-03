package software.example.spool.sec.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class HttpUtils {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private HttpUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String get(String url) throws RuntimeException {
        return get(url, Map.of());
    }

    public static String get(String url, Map<String, String> headers) throws RuntimeException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET();

            if (headers != null) {
                headers.forEach(builder::header);
            }

            HttpRequest request = builder.build();

            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(
                        "HTTP GET failed. Status: " + response.statusCode() + ", URL: " + url
                );
            }

            return response.body();

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error during HTTP GET: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("HTTP GET interrupted: " + url, e);
        }
    }

    public static HttpResponse<String> getResponse(String url, Map<String, String> headers) throws RuntimeException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET();

            if (headers != null) {
                headers.forEach(builder::header);
            }

            HttpRequest request = builder.build();

            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error during HTTP GET: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("HTTP GET interrupted: " + url, e);
        }
    }
}