package co.paydeck.util;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public HttpException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
