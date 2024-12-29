package co.paydeck.model.deposit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutCustomization {
    private String title;
    private String description;
    private String logoUrl;
    private String returnUrl;
    private String cancelUrl;
    private String webhookUrl;
}
