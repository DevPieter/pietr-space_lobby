package nl.devpieter.api.request;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class RequestHelper {

    public static final String USER_AGENT_HEADER = "User-Agent";

    protected static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * Sends a simple GET request to the specified URI.
     *
     * @param uri the URI to send the GET request to
     * @return the HttpResponse containing the response body as a String
     * @throws IOException          if an I/O error occurs when sending or receiving
     * @throws InterruptedException if the operation is interrupted
     */
    public static @NotNull HttpResponse<String> simpleGet(@NotNull URI uri) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(uri).GET().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Sends a simple POST request to the specified URI with the given body.
     *
     * @param uri           the URI to send the POST request to
     * @param bodyPublisher the body of the POST request
     * @return the HttpResponse containing the response body as a String
     * @throws IOException          if an I/O error occurs when sending or receiving
     * @throws InterruptedException if the operation is interrupted
     */
    public static @NotNull HttpResponse<String> simplePost(@NotNull URI uri, @NotNull HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(uri).POST(bodyPublisher).build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Sends a simple PUT request to the specified URI with the given body.
     *
     * @param uri           the URI to send the PUT request to
     * @param bodyPublisher the body of the PUT request
     * @return the HttpResponse containing the response body as a String
     * @throws IOException          if an I/O error occurs when sending or receiving
     * @throws InterruptedException if the operation is interrupted
     */
    public static @NotNull HttpResponse<String> simplePut(@NotNull URI uri, @NotNull HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(uri).PUT(bodyPublisher).build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Sends a simple DELETE request to the specified URI.
     *
     * @param uri the URI to send the DELETE request to
     * @return the HttpResponse containing the response body as a String
     * @throws IOException          if an I/O error occurs when sending or receiving
     * @throws InterruptedException if the operation is interrupted
     */
    public static @NotNull HttpResponse<String> simpleDelete(@NotNull URI uri) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(uri).DELETE().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Creates a new HttpRequest.Builder with the specified URI and default headers.
     *
     * @param uri the URI to set in the request builder
     * @return the HttpRequest.Builder with the specified URI and default headers
     */
    public static HttpRequest.Builder createRequestBuilder(@NotNull URI uri) {
        String agent = String.format("PietrSpaceLobby/%s (Java %s on %s)", "1.0", System.getProperty("java.version"), System.getProperty("os.name"));

        return HttpRequest.newBuilder()
                .header(USER_AGENT_HEADER, agent)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .uri(uri);
    }
}
