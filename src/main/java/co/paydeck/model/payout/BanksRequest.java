package co.paydeck.model.payout;

import java.util.Currency;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BanksRequest {
    private String countryCode;
    private Boolean useCursor;
    private Integer page;
    private Integer pageSize;
    private Boolean payWithBank;
    private Boolean payWithBankTransfer;
    private String nextPage;
    private String previousPage; 
    private Currency currency;
    private String channelType;
    private Boolean includeNIPSortCode;    
}
