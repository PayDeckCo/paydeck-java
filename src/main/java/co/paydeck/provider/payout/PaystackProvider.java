package co.paydeck.provider.payout;

import co.paydeck.core.BasePayoutProvider;
import co.paydeck.model.payout.BanksRequest;
import co.paydeck.model.payout.BanksResponseData;
import co.paydeck.model.payout.PayoutRequest;
import co.paydeck.util.HttpClient;
import co.paydeck.model.common.PaydeckResponse;
import co.paydeck.model.deposit.TransactionResponseData;
import co.paydeck.model.CountryProviderMap;
import co.paydeck.model.PaymentMethod;
import co.paydeck.model.Provider;

import java.util.EnumSet;
import java.util.Map;

public class PaystackProvider implements BasePayoutProvider {

    private final HttpClient httpClient;

    public PaystackProvider(String secretKey) {
        this.httpClient = HttpClient.builder()
            .baseUrl("https://api.paystack.co")
            .addDefaultHeader("Authorization", "Bearer " + secretKey)
            .addDefaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public String getProviderName() {
        return Provider.PAYSTACK.getCode();
    }

    @Override
    public boolean supportsPaymentMethods(EnumSet<PaymentMethod> methods) {
        return methods.contains(PaymentMethod.BANK_TRANSFER);
    }

    @Override
    public PaydeckResponse<BanksResponseData> getBanks(BanksRequest request) {
        String path = buildBankPath(request);
        Map<String, Object> response = httpClient.get(path, Map.class);

        Boolean status = (Boolean) response.get("status");
        String message = (String) response.get("message");

        if (!status.booleanValue()) {
            return PaydeckResponse.providerError(message, message, path, message);
        }

        BanksResponseData data = new BanksResponseData();
        data.setBanks((Map<String, String>) response.get("data"));

        return PaydeckResponse.success(data);
    }

    @Override
    public PaydeckResponse<TransactionResponseData> initiatePayout(PayoutRequest request) {
        return null;
    }

    @Override
    public PaydeckResponse<TransactionResponseData> fetchTransaction(String merchantTransactionReference) {
        return null;
    }

    private String buildBankPath(BanksRequest request) {
        String path = "/bank";
        String countryCode = request.getCountryCode();
        if (countryCode != null) {
            String countryName = CountryProviderMap.getProviderCountryMap().get(this.getProviderName()).get(countryCode);
            path += "?country=" + countryName;
        }

        return path;
    }
}