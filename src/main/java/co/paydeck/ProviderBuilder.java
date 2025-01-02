package co.paydeck;

import co.paydeck.core.BaseDepositProvider;
import co.paydeck.model.Provider;
import co.paydeck.provider.deposit.FlutterwaveProvider;
import co.paydeck.provider.deposit.PaystackProvider;

public class ProviderBuilder {

    public static BaseDepositProvider buildProvider(Provider provider, String apiKey) {
        return switch (provider.getCode()) {
            case "flutterwave" -> new FlutterwaveProvider(apiKey);
            case "paystack" -> new PaystackProvider(apiKey);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
