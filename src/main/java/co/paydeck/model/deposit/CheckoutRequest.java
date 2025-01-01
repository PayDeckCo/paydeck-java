package co.paydeck.model.deposit;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;

import co.paydeck.model.Currency;
import co.paydeck.model.Customer;
import co.paydeck.model.PaymentMethod;

@Data
@Builder
public class CheckoutRequest {
  private String reference;
  private BigDecimal amount;
  private Currency currency;
  private Customer customer;
  private EnumSet<PaymentMethod> paymentMethods;
  private Map<String, String> metadata;
  private CheckoutCustomization customization;
}
