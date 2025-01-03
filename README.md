# Paydeck Java SDK

> **Note**: This SDK is in active development. APIs might change without notice. Not recommended for production use yet.

A unified payment gateway integration for multiple payment providers in Africa.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Transaction Types](#transaction-types)
  - [Deposit](#deposit)
    - [Provider Initialization](#provider-initialization)
    - [Creating Checkout Request](#creating-checkout-request)
    - [Transaction Verification](#transaction-verification)
    - [Response Handling](#response-handling)
    - [Error Handling](#error-handling)
    - [Supported Payment Methods](#supported-payment-methods)
  - [Withdrawal or Payout (Coming Soon)](#withdrawal)
- [Supported Providers](#supported-providers)
- [Upcomming Providers](#upcomming-providers)
- [Advanced Configuration](#advanced-configuration)
- [Development Status](#development-status)
- [Contributing](#contributing)
- [License](#license)

## Features

- Single API interface for multiple payment providers
- Support for Flutterwave and Paystack
- Modular architecture for different transaction types (Deposits, Withdrawals, etc.)
- Type-safe enum-based provider selection
- Comprehensive error handling with provider-specific details
- Transaction status verification

## Installation

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>co.paydeck</groupId>
  <artifactId>paydeck-java</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

```java
import co.paydeck.ProviderBuilder;
import co.paydeck.model.Provider;
import co.paydeck.core.BaseDepositProvider;
import co.paydeck.model.common.PaydeckResponse;

// Initialize provider
BaseDepositProvider provider = ProviderBuilder.buildProvider(
    Provider.FLUTTERWAVE, 
    "YOUR-SECRET-KEY"
);

// Verify transaction
PaydeckResponse<TransactionResponseData> response = provider.fetchTransaction("TXN_REF");
if (response.isSuccess()) {
    TransactionResponseData data = response.getData();
    System.out.println("Status: " + data.getStatus());
} else {
    System.out.println("Error: " + response.getError().getMessage());
}
```

## Transaction Types

### Deposit

#### Provider Initialization

```java
import co.paydeck.ProviderBuilder;
import co.paydeck.model.Provider;
import co.paydeck.core.BaseDepositProvider;

// Available providers
Provider.FLUTTERWAVE
Provider.PAYSTACK

// Initialize provider
BaseDepositProvider provider = ProviderBuilder.buildProvider(
    Provider.FLUTTERWAVE, 
    "YOUR-SECRET-KEY"
);
```

#### Creating Checkout Request

```java
import co.paydeck.model.Customer;
import co.paydeck.model.PaymentMethod;
import co.paydeck.model.deposit.*;
import java.util.EnumSet;
import java.util.UUID;

// Build customer details
Customer customer = Customer.builder()
    .email("customer@email.com")
    .firstName("John")
    .lastName("Doe")
    .phoneNumber("+2348012345678")  // Optional
    .build();

// Build checkout customization
CheckoutCustomization customization = CheckoutCustomization.builder()
    .title("Payment for Order #123")  // Optional
    .description("Purchase of items")  // Optional
    .logoUrl("https://your-domain.com/logo.png")  // Optional
    .returnUrl("https://your-domain.com/callback")
    .build();

// Additional metadata (optional)
Map<String, String> metadata = new HashMap<>();
metadata.put("orderId", "123456");
metadata.put("customerType", "regular");

// Create checkout request
CheckoutRequest request = CheckoutRequest.builder()
    .reference(UUID.randomUUID().toString())  // Your unique transaction reference
    .amount(new BigDecimal("5000.00"))
    .currency(Currency.NGN)  // Available: NGN, USD, GHS, KES, etc.
    .paymentMethods(EnumSet.of(
        PaymentMethod.CARD,
        PaymentMethod.BANK_TRANSFER,
        PaymentMethod.USSD,
        PaymentMethod.MOBILE_MONEY
    ))
    .customer(customer)
    .customization(customization)
    .metadata(metadata)  // Optional
    .build();

// Process payment
PaydeckResponse<CheckoutResponseData> response = provider.initiateCheckout(request);
```

#### Response Handling

```java
// Checkout Response Handling
PaydeckResponse<CheckoutResponseData> response = provider.initiateCheckout(request);
if (response.isSuccess()) {
    CheckoutResponseData data = response.getData();
    String checkoutUrl = data.getCheckoutUrl();  // Redirect customer to this URL
    String transactionId = data.getTransactionId();
    String providerRef = data.getProviderTransactionReference();
    Map<String, String> providerMetadata = data.getProviderMetadata();
} else {
    // Handle error
    PaydeckResponse.ErrorData error = response.getError();
    String errorCode = error.getCode();            // e.g., PROVIDER_ERROR
    String errorMessage = error.getMessage();      // Error description
    String providerCode = error.getProviderCode(); // Provider-specific error code
    String providerMessage = error.getProviderMessage(); // Provider-specific message
}

// Transaction Verification Response Handling
PaydeckResponse<TransactionResponseData> response = provider.fetchTransaction("TXN_REF");
if (response.isSuccess()) {
    TransactionResponseData data = response.getData();
    
    // Basic transaction info
    String transactionId = data.getTransactionId();
    String merchantRef = data.getMerchantTransactionReference();
    String providerRef = data.getProviderTransactionReference();
    
    // Status
    TransactionStatus status = data.getStatus();  // SUCCESSFUL, FAILED, PENDING, CANCELLED
    
    // Amount details
    BigDecimal amount = data.getAmount();            // Original amount
    BigDecimal charged = data.getChargedAmount();    // Amount charged to customer
    BigDecimal settled = data.getSettledAmount();    // Amount to be settled
    BigDecimal fee = data.getFeeAmount();            // Provider fee
    String currency = data.getCurrency();
    
    // Time and method
    LocalDateTime txnDate = data.getTransactionDate();
    String paymentMethod = data.getPaymentMethod();
    
    // Provider-specific data
    Map<String, String> providerMetadata = data.getProviderMetadata();
} else {
    PaydeckResponse.ErrorData error = response.getError();
    // Handle specific error cases
    switch (error.getCode()) {
        case "PROVIDER_ERROR" -> handleProviderError(error);
        case "UNSUPPORTED_PAYMENT_METHOD" -> handleUnsupportedMethod(error);
        default -> handleGenericError(error);
    }
}
```

#### Error Handling

PaydeckResponse provides built-in error handling with provider-specific details:

```java
if (!response.isSuccess()) {
    PaydeckResponse.ErrorData error = response.getError();
    
    // Generic error information
    String errorCode = error.getCode();
    String message = error.getMessage();
    
    // Provider-specific error details (available when using providerError())
    String providerCode = error.getProviderCode();
    String providerMessage = error.getProviderMessage();
}

// Creating Error Responses:
// Basic error
PaydeckResponse<T> errorResponse = PaydeckResponse.error(
    "ERROR_CODE",
    "Error description"
);

// Provider-specific error
PaydeckResponse<T> providerErrorResponse = PaydeckResponse.providerError(
    "ERROR_CODE",
    "Error description",
    "PROVIDER_CODE",
    "Provider error message"
);

// Success response
PaydeckResponse<T> successResponse = PaydeckResponse.success(data);
```

#### Supported Payment Methods

Provider-specific payment methods for deposits:

**Flutterwave**
```java
EnumSet<PaymentMethod> SUPPORTED_METHODS = EnumSet.of(
    PaymentMethod.CARD,
    PaymentMethod.BANK_TRANSFER,
    PaymentMethod.USSD,
    PaymentMethod.MOBILE_MONEY
);
```

**Paystack**
```java
EnumSet<PaymentMethod> SUPPORTED_METHODS = EnumSet.of(
    PaymentMethod.CARD,
    PaymentMethod.BANK_TRANSFER,
    PaymentMethod.USSD,
    PaymentMethod.QR
);
```

### Withdrawal or Payout
🚧 Coming Soon
- Bank transfers
- Mobile Money payouts
- International transfers
- Bank Account Verification
- Bank List Retrieval
- Account Name Resolution

## Supported Providers

Currently supports:
- FLUTTERWAVE (`Provider.FLUTTERWAVE`)
- PAYSTACK (`Provider.PAYSTACK`)

## Upcomming Providers
🚧 Coming Soon
- Korapay
- Budpay
- Fincra

## Advanced Configuration

The SDK uses a configurable HTTP client under the hood:

```java
HttpClient client = HttpClient.builder()
    .baseUrl("https://api.example.com")
    .addDefaultHeader("Authorization", "Bearer " + apiKey)
    .connectTimeout(30)  // in seconds
    .readTimeout(30)     // in seconds
    .writeTimeout(30)    // in seconds
    .objectMapper(customObjectMapper)  // Optional: custom Jackson ObjectMapper
    .build();
```

## Development Status

Current Status: **Alpha**

Available Features:
- ✅ Deposit transactions
- ✅ Transaction status verification
- ✅ Multiple provider support (Flutterwave, Paystack)
- ✅ Error handling with provider-specific details
- ✅ Builder patterns
- ✅ Configurable HTTP client

Coming Soon:
- 🚧 Withdrawal transactions
- 🚧 Bank list retrieval
- 🚧 Card tokenization
- 🚧 Webhook handling
- 🚧 Transfer endpoints
- 🚧 Comprehensive test coverage
- 🚧 CI/CD pipeline
- 🚧 Documentation website

## Contributing

1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License

MIT License - see [LICENSE](LICENSE) for details
