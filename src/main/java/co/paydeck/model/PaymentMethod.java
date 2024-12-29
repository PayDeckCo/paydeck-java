package co.paydeck.model;

public enum PaymentMethod {
    CARD,
    BANK_TRANSFER,
    USSD,
    MOBILE_MONEY,
    BANK_ACCOUNT,
    QR;

    public String toFlutterwaveMethod() {
        return switch (this) {
            case CARD -> "card";
            case BANK_TRANSFER -> "banktransfer";
            case USSD -> "ussd";
            case MOBILE_MONEY -> "mobilemoney";
            case BANK_ACCOUNT -> "bank_account";
            default -> throw new UnsupportedOperationException(
                "Payment method " + this + " not supported by Flutterwave"
            );
        };
    }

    public String toPaystackMethod() {
        return switch (this) {
            case CARD -> "card";
            case BANK_TRANSFER -> "bank_transfer";
            case USSD -> "ussd";
            case QR -> "qr";
            default -> throw new UnsupportedOperationException(
                "Payment method " + this + " not supported by Paystack"
            );
        };
    }
}
