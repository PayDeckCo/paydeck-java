package co.paydeck.core;

import co.paydeck.model.deposit.*;
import co.paydeck.model.common.PaydeckResponse;
import co.paydeck.model.PaymentMethod;

import java.util.EnumSet;

public interface DepositProvider {

    String getProviderName();

    boolean supportsPaymentMethods(EnumSet<PaymentMethod> methods);

    PaydeckResponse<CheckoutResponseData> initiateCheckout(CheckoutRequest request);

    PaydeckResponse<TransactionResponseData> getTransactionStatus(String transactionId);
}
