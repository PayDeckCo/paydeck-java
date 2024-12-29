package co.paydeck.provider.deposit;

import co.paydeck.core.DepositProvider;
import co.paydeck.model.PaymentMethod;
import co.paydeck.model.TransactionStatus;
import co.paydeck.model.deposit.*;
import co.paydeck.model.common.PaydeckResponse;
import co.paydeck.util.HttpClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class FlutterwaveProvider implements DepositProvider {
    private final HttpClient httpClient;
    private static final Set<PaymentMethod> SUPPORTED_METHODS = EnumSet.of(
        PaymentMethod.CARD,
        PaymentMethod.BANK_TRANSFER,
        PaymentMethod.USSD,
        PaymentMethod.MOBILE_MONEY
    );
    private static final String PROVIDER_ERROR = "PROVIDER_ERROR";

    public FlutterwaveProvider(String secretKey) {
        this.httpClient = HttpClient.builder()
            .baseUrl("https://api.flutterwave.com/v3")
            .addDefaultHeader("Authorization", "Bearer " + secretKey)
            .addDefaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public String getProviderName() {
        return "Flutterwave";
    }

    @Override
    public Set<PaymentMethod> getSupportedPaymentMethods() {
        return SUPPORTED_METHODS;
    }

    @Override
    public boolean supportsPaymentMethod(PaymentMethod method) {
        return SUPPORTED_METHODS.contains(method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PaydeckResponse<CheckoutResponseData> initiateCheckout(CheckoutRequest request) {
        if (!supportsPaymentMethod(request.getPaymentMethod())) {
            return PaydeckResponse.error(
                "UNSUPPORTED_PAYMENT_METHOD",
                "Payment method " + request.getPaymentMethod() + 
                " not supported by " + getProviderName()
            );
        }

        try {
            Map<String, Object> payload = buildCheckoutPayload(request);
            Map<String, Object> response = httpClient.post("/payments", payload, Map.class);
            return processCheckoutResponse(response);
        } catch (IOException e) {
            return PaydeckResponse.error(
              PROVIDER_ERROR,
                "Failed to communicate with Flutterwave: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> buildCheckoutPayload(CheckoutRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tx_ref", request.getReference());
        payload.put("amount", request.getAmount());
        payload.put("currency", request.getCurrency());
        payload.put("payment_options", request.getPaymentMethod().toFlutterwaveMethod());
        payload.put("redirect_url", request.getCustomization().getReturnUrl());
        
        payload.put("customer", buildCustomerData(request));
        payload.put("customization", buildCustomizationData(request));
        payload.put("meta", request.getMetadata());

        return payload;
    }

    private Map<String, String> buildCustomerData(CheckoutRequest request) {
        Map<String, String> customer = new HashMap<>();
        String fullName = request.getCustomer().getFirstName() + " " + request.getCustomer().getLastName();
        customer.put("email", request.getCustomer().getEmail());
        customer.put("phone_number", request.getCustomer().getPhoneNumber());
        customer.put("name", fullName);
        return customer;
    }

    private Map<String, String> buildCustomizationData(CheckoutRequest request) {
        Map<String, String> customization = new HashMap<>();
        customization.put("title", request.getCustomization().getTitle());
        customization.put("description", request.getCustomization().getDescription());
        customization.put("logo", request.getCustomization().getLogoUrl());
        return customization;
    }

    @SuppressWarnings("unchecked")
    private PaydeckResponse<CheckoutResponseData> processCheckoutResponse(Map<String, Object> response) {
        String status = (String) response.get("status");
        String message = (String) response.get("message");

        if (!"success".equalsIgnoreCase(status)) {
            return PaydeckResponse.providerError(
              PROVIDER_ERROR,
                "Flutterwave request failed",
                status,
                message
            );
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return PaydeckResponse.success(buildCheckoutResponseData(data));
    }

    private CheckoutResponseData buildCheckoutResponseData(Map<String, Object> data) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("flw_ref", (String) data.get("flw_ref"));
        metadata.put("tx_ref", (String) data.get("tx_ref"));

        return CheckoutResponseData.builder()
            .checkoutUrl((String) data.get("link"))
            .transactionId((String) data.get("transaction_id"))
            .providerReference((String) data.get("flw_ref"))
            .providerMetadata(metadata)
            .build();
    }

     @SuppressWarnings("unchecked")
    @Override
    public PaydeckResponse<TransactionResponseData> getTransactionStatus(String transactionId) {
        try {
            Map<String, Object> response = httpClient.get(
                "/transactions/" + transactionId + "/verify",
                Map.class
            );
            return processTransactionResponse(response);
        } catch (IOException e) {
            return PaydeckResponse.error(
              PROVIDER_ERROR,
                "Failed to get transaction status from Flutterwave: " + e.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private PaydeckResponse<TransactionResponseData> processTransactionResponse(Map<String, Object> response) {
        String status = (String) response.get("status");
        String message = (String) response.get("message");

        if (!"success".equalsIgnoreCase(status)) {
            return PaydeckResponse.providerError(
              PROVIDER_ERROR,
                "Flutterwave transaction verification failed",
                status,
                message
            );
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return PaydeckResponse.success(buildTransactionResponseData(data));
    }

    private TransactionResponseData buildTransactionResponseData(Map<String, Object> data) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("flw_ref", (String) data.get("flw_ref"));
        metadata.put("processor_response", (String) data.get("processor_response"));
        metadata.put("card_type", (String) data.get("card_type"));

        return TransactionResponseData.builder()
            .transactionId((String) data.get("id"))
            .providerReference((String) data.get("flw_ref"))
            .status(mapTransactionStatus((String) data.get("status")))
            .amount(new BigDecimal(data.get("amount").toString()))
            .currency((String) data.get("currency"))
            .transactionDate(parseTransactionDate((String) data.get("created_at")))
            .paymentMethod((String) data.get("payment_type"))
            .providerMetadata(metadata)
            .build();
    }

    private TransactionStatus mapTransactionStatus(String flwStatus) {
        return switch (flwStatus.toLowerCase()) {
            case "successful" -> TransactionStatus.SUCCESSFUL;
            case "failed" -> TransactionStatus.FAILED;
            case "cancelled" -> TransactionStatus.CANCELLED;
            case "pending" -> TransactionStatus.PENDING;
            default -> TransactionStatus.FAILED;
        };
    }

    private LocalDateTime parseTransactionDate(String dateStr) {
    try {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
    } catch (DateTimeParseException e) {
        return LocalDateTime.now();
    }
}
}
