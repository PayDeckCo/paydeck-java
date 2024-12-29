package co.paydeck.model.deposit;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import co.paydeck.model.TransactionStatus;

@Data
@Builder
public class TransactionResponseData {
    private String transactionId;
    private String providerReference;
    private String merchantReference;
    private TransactionStatus status;
    private BigDecimal amount;
    private BigDecimal chargedAmount;
    private BigDecimal settledAmount;
    private BigDecimal feeAmount;
    private String currency;
    private LocalDateTime transactionDate;
    private String paymentMethod;
    private Map<String, String> providerMetadata;
}
