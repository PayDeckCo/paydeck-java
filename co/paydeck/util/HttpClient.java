package co.paydeck.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    private final String baseUrl;
    private final Map<String, String> defaultHeaders;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;
    private static final MediaType JSON = MediaType.parse("application/json");

    // Private constructor to force builder usage
    private HttpClient(String baseUrl, Map<String, String> defaultHeaders, 
                      ObjectMapper objectMapper, OkHttpClient client) {
        this.baseUrl = baseUrl;
        this.defaultHeaders = defaultHeaders;
        this.objectMapper = objectMapper;
        this.client = client;
    }

    // Builder class
    public static class Builder {
        private String baseUrl;
        private Map<String, String> defaultHeaders = new HashMap<>();
        private ObjectMapper objectMapper = new ObjectMapper();
        private int connectTimeout = 30;
        private int readTimeout = 30;
        private int writeTimeout = 30;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder addDefaultHeader(String key, String value) {
            this.defaultHeaders.put(key, value);
            return this;
        }

        public Builder defaultHeaders(Map<String, String> headers) {
            this.defaultHeaders = headers;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder connectTimeout(int seconds) {
            this.connectTimeout = seconds;
            return this;
        }

        public Builder readTimeout(int seconds) {
            this.readTimeout = seconds;
            return this;
        }

        public Builder writeTimeout(int seconds) {
            this.writeTimeout = seconds;
            return this;
        }

        public HttpClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalStateException("baseUrl is required");
            }

            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();

            return new HttpClient(baseUrl, defaultHeaders, objectMapper, client);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // GET request
    public <T> T get(String path, Class<T> responseType) throws IOException {
        return get(path, responseType, null);
    }

    public <T> T get(String path, Class<T> responseType, Map<String, String> headers) throws IOException {
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .headers(buildHeaders(headers))
            .get()
            .build();

        return executeRequest(request, responseType);
    }

    // POST request
    public <T> T post(String path, Object body, Class<T> responseType) throws IOException {
        return post(path, body, responseType, null);
    }

    public <T> T post(String path, Object body, Class<T> responseType, Map<String, String> headers) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(body);
        
        RequestBody requestBody = RequestBody.create(
            jsonBody, 
            JSON
        );

        Request request = new Request.Builder()
            .url(baseUrl + path)
            .headers(buildHeaders(headers))
            .post(requestBody)
            .build();

        return executeRequest(request, responseType);
    }

    // PUT request
    public <T> T put(String path, Object body, Class<T> responseType) throws IOException {
        return put(path, body, responseType, null);
    }

    public <T> T put(String path, Object body, Class<T> responseType, Map<String, String> headers) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(body);
        
        RequestBody requestBody = RequestBody.create(
            jsonBody, 
            JSON
        );

        Request request = new Request.Builder()
            .url(baseUrl + path)
            .headers(buildHeaders(headers))
            .put(requestBody)
            .build();

        return executeRequest(request, responseType);
    }

    // PATCH request
    public <T> T patch(String path, Object body, Class<T> responseType) throws IOException {
        return patch(path, body, responseType, null);
    }

    public <T> T patch(String path, Object body, Class<T> responseType, Map<String, String> headers) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(body);
        
        RequestBody requestBody = RequestBody.create(
            jsonBody, 
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(baseUrl + path)
            .headers(buildHeaders(headers))
            .patch(requestBody)
            .build();

        return executeRequest(request, responseType);
    }

    // DELETE request
    public <T> T delete(String path, Class<T> responseType) throws IOException {
        return delete(path, responseType, null);
    }

    public <T> T delete(String path, Class<T> responseType, Map<String, String> headers) throws IOException {
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .headers(buildHeaders(headers))
            .delete()
            .build();

        return executeRequest(request, responseType);
    }

    // Helper methods
    private Headers buildHeaders(Map<String, String> additionalHeaders) {
        Headers.Builder headersBuilder = new Headers.Builder();
        
        // Add default headers
        if (defaultHeaders != null) {
            defaultHeaders.forEach(headersBuilder::add);
        }
        
        // Add additional headers
        if (additionalHeaders != null) {
            additionalHeaders.forEach(headersBuilder::add);
        }
        
        return headersBuilder.build();
    }

    private <T> T executeRequest(Request request, Class<T> responseType) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, responseType);
        }
    }

    private void handleErrorResponse(Response response) throws IOException {
        String errorBody = response.body().string();
        throw new HttpException(
            String.format("HTTP %d Error: %s", response.code(), errorBody),
            response.code(),
            errorBody
        );
    }
}

