package co.paydeck.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaydeckResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private ErrorData error;

    @Data
    @Builder
    public static class ErrorData {
        private String code;
        private String message;
        private String providerCode;
        private String providerMessage;
    }

    public static <T> PaydeckResponse<T> success(T data) {
        return PaydeckResponse.<T>builder()
            .success(true)
            .data(data)
            .build();
    }

    public static <T> PaydeckResponse<T> error(String code, String message) {
        return PaydeckResponse.<T>builder()
            .success(false)
            .error(ErrorData.builder()
                .code(code)
                .message(message)
                .build())
            .build();
    }

    public static <T> PaydeckResponse<T> providerError(String code, String message, 
            String providerCode, String providerMessage) {
        return PaydeckResponse.<T>builder()
            .success(false)
            .error(ErrorData.builder()
                .code(code)
                .message(message)
                .providerCode(providerCode)
                .providerMessage(providerMessage)
                .build())
            .build();
    }
}
