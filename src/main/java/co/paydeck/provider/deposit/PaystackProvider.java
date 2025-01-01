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

public class PaystackProvider implements DepositProvider {
    private final HttpClient httpClient;
    private static final Set<PaymentMethod> SUPPORTED_METHODS = EnumSet.of(
        PaymentMethod.CARD,
        PaymentMethod.BANK_TRANSFER,
        PaymentMethod.USSD,
        PaymentMethod.QR
    );
    private static final String PROVIDER_ERROR = "PROVIDER_ERROR";

    public PaystackProvider(String secretKey) {
        this.httpClient = HttpClient.builder()
            .baseUrl("https://api.paystack.co")
            .addDefaultHeader("Authorization", "Bearer " + secretKey)
            .addDefaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public String getProviderName() {
        return "Paystack";
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
            Map<String, Object> response = httpClient.post("/transaction/initialize", payload, Map.class);

            String status = (String) response.get("status");
            String message = (String) response.get("message");
    
            if (!Boolean.TRUE.equals(response.get("status"))) {
                return PaydeckResponse.providerError(
                    PROVIDER_ERROR,
                    "Paystack request failed",
                    status,
                    message
                );
            }
    
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            return PaydeckResponse.success(buildCheckoutResponseData(data));
        } catch (IOException e) {
            return PaydeckResponse.error(
                PROVIDER_ERROR,
                "Failed to communicate with Paystack: " + e.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PaydeckResponse<TransactionResponseData> getTransactionStatus(String transactionId) {
        try {
            Map<String, Object> response = httpClient.get(
                "/transaction/verify/" + transactionId,
                Map.class
            );

            String status = (String) response.get("status");
            String message = (String) response.get("message");

            if (!Boolean.TRUE.equals(response.get("status"))) {
                return PaydeckResponse.providerError(
                    PROVIDER_ERROR,
                    "Paystack transaction verification failed",
                    status,
                    message
                );
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");

            return PaydeckResponse.success(buildTransactionResponseData(data));
        } catch (IOException e) {
            return PaydeckResponse.error(
                PROVIDER_ERROR,
                "Failed to get transaction status from Paystack: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> buildCheckoutPayload(CheckoutRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference", request.getReference());
        payload.put("amount", request.getAmount().multiply(new BigDecimal("100")).intValue()); // Convert to kobo
        payload.put("currency", request.getCurrency());
        payload.put("callback_url", request.getCustomization().getReturnUrl());
        
        // Add channels based on payment method
        List<String> channels = new ArrayList<>();
        channels.add(request.getPaymentMethod().toPaystackMethod());
        payload.put("channels", channels);
        
        payload.put("customer", buildCustomerData(request));
        payload.put("customization", buildCustomizationData(request));
        payload.put("metadata", request.getMetadata());

        return payload;
    }

    private Map<String, String> buildCustomerData(CheckoutRequest request) {
        Map<String, String> customer = new HashMap<>();
        customer.put("email", request.getCustomer().getEmail());
        customer.put("phone", request.getCustomer().getPhoneNumber());
        customer.put("first_name", request.getCustomer().getFirstName());
        customer.put("last_name", request.getCustomer().getLastName());
        return customer;
    }

    private Map<String, String> buildCustomizationData(CheckoutRequest request) {
        Map<String, String> customization = new HashMap<>();
        customization.put("title", request.getCustomization().getTitle());
        customization.put("description", request.getCustomization().getDescription());
        customization.put("logo", request.getCustomization().getLogoUrl());
        return customization;
    }

    private CheckoutResponseData buildCheckoutResponseData(Map<String, Object> data) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("access_code", (String) data.get("access_code"));
        metadata.put("reference", (String) data.get("reference"));

        return CheckoutResponseData.builder()
            .checkoutUrl((String) data.get("authorization_url"))
            .transactionId((String) data.get("reference"))
            .providerReference((String) data.get("access_code"))
            .providerMetadata(metadata)
            .build();
    }

    private TransactionResponseData buildTransactionResponseData(Map<String, Object> data) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("authorization_code", (String) data.get("authorization_code"));
        metadata.put("card_type", (String) data.get("card_type"));
        metadata.put("last4", (String) data.get("last4"));
        metadata.put("channel", (String) data.get("channel"));

        BigDecimal amount = new BigDecimal(data.get("amount").toString())
            .divide(new BigDecimal("100")); // Convert from kobo to main currency

        return TransactionResponseData.builder()
            .transactionId((String) data.get("id"))
            .merchantReference((String) data.get("reference"))
            .providerReference((String) data.get("authorization_code"))
            .status(mapTransactionStatus((String) data.get("status")))
            .amount(amount)
            .chargedAmount(amount)
            .settledAmount(amount)
            .feeAmount(new BigDecimal(data.get("fees").toString()).divide(new BigDecimal("100")))
            .currency((String) data.get("currency"))
            .transactionDate(parseTransactionDate((String) data.get("paid_at")))
            .paymentMethod((String) data.get("channel"))
            .providerMetadata(metadata)
            .build();
    }

    private TransactionStatus mapTransactionStatus(String paystackStatus) {
        return switch (paystackStatus.toLowerCase()) {
            case "success" -> TransactionStatus.SUCCESSFUL;
            case "failed" -> TransactionStatus.FAILED;
            case "abandoned" -> TransactionStatus.CANCELLED;
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