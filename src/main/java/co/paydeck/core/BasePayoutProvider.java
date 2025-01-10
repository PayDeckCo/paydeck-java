package co.paydeck.core;

import co.paydeck.model.payout.BanksResponseData;
import co.paydeck.model.payout.PayoutRequest;
import co.paydeck.model.common.PaydeckResponse;
import co.paydeck.model.deposit.TransactionResponseData;
import co.paydeck.model.PaymentMethod;

import java.util.EnumSet;

public interface BasePayoutProvider {

    String getProviderName();

    boolean supportsPaymentMethods(EnumSet<PaymentMethod> methods);

    PaydeckResponse<BanksResponseData> getBanks();
    
    PaydeckResponse<TransactionResponseData> initiatePayout(PayoutRequest request);

    PaydeckResponse<TransactionResponseData> fetchTransaction(String merchantTransactionReference);

}
