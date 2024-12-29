package co.paydeck.model.deposit;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class CheckoutResponseData {
    private String checkoutUrl;
    private String transactionId;
    private String providerReference;
    private Map<String, String> providerMetadata;
}
