package co.paydeck.model;

public enum PaymentMethod {
    CARD,
    BANK_TRANSFER,
    USSD,
    MOBILE_MONEY,
    BANK_ACCOUNT;

    public String toFlutterwaveMethod() {
        return switch (this) {
            case CARD -> "card";
            case BANK_TRANSFER -> "banktransfer";
            case USSD -> "ussd";
            case MOBILE_MONEY -> "mobilemoney";
            case BANK_ACCOUNT -> "bank_account";
        };
    }

    public String toPaystackMethod() {
        return switch (this) {
            case CARD -> "card";
            case BANK_TRANSFER -> "bank_transfer";
            default -> throw new UnsupportedOperationException(
                "Payment method " + this + " not supported by Paystack"
            );
        };
    }
}
