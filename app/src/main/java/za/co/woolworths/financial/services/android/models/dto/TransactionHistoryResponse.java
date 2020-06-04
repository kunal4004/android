package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem;

public class TransactionHistoryResponse {

    public List<TransactionItem> transactions;
    public Response response;
    public int httpCode;
}
