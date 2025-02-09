package co.paydeck.model;

import java.util.HashMap;
import java.util.Map;

public class CountryProviderMap {
    private CountryProviderMap() {
    }
    
    public static Map<String, Map<String, String>> getProviderCountryMap() {
        Map<String, Map<String, String>> providerCountryMap = new HashMap<>();
        Map<String, String> flutterwaveCountries = Map.of(
            "NG", "Nigeria",
            "GH", "Ghana",
            "KE", "Kenya",
            "ZA", "South Africa"
        );

        providerCountryMap.put(Provider.FLUTTERWAVE.getCode(), flutterwaveCountries);

        Map<String, String> paystackCountries = Map.of(
            "NG", "Nigeria",
            "GH", "Ghana",
            "KE", "Kenya",
            "ZA", "South Africa"
        );

        providerCountryMap.put(Provider.PAYSTACK.getCode(), paystackCountries);

        return providerCountryMap;
    }
}
