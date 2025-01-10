package co.paydeck.model.payout;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BanksResponseData {
    private Bank[] banks;
    
    @Data
    @Builder
    public static class Bank {
        private String bankCode;
        private String bankName;
        private String countryCode;
    }
}
