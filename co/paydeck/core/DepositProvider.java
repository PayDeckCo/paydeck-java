package co.paydeck.core;

import co.paydeck.model.deposit.*;
import co.paydeck.model.common.PaydeckResponse;
import co.paydeck.model.PaymentMethod;
import java.util.Set;

public interface DepositProvider {

    String getProviderName();

    Set<PaymentMethod> getSupportedPaymentMethods();

    boolean supportsPaymentMethod(PaymentMethod method);

    PaydeckResponse<CheckoutResponseData> initiateCheckout(CheckoutRequest request);

    PaydeckResponse<TransactionResponseData> getTransactionStatus(String transactionId);
}
