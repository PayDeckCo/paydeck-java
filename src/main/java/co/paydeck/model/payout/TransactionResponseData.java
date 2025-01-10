package co.paydeck.model.payout;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponseData {
    private String id;
    private String transactionReference;
    private String merchantTransactionReference;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String provider;
    private BigDecimal fee ;
    private String feeCurrency;
    private String message;
    private String narration;
    private String source;
    private String sourceReference;
    private String createdAt;
    private String updatedAt;
}
