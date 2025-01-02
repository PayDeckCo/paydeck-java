package co.paydeck.model;

public enum Provider {
  FLUTTERWAVE,
  PAYSTACK;

  public String getCode() {
      return name().toLowerCase();
  }
}
