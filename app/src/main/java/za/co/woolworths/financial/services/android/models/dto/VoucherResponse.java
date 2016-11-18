package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

public class VoucherResponse {
    public VoucherCollection voucherCollection;
    public TierInfo tierInfo;
    public List<TierHistory> tierHistoryList;
    public List<Tier> tiers;
    public Response response;
    public int httpCode;
}
