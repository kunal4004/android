package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

public class TransactionHistoryResponse {

    public List<Transaction> transactions;
    public Response response;
    public int httpCode;
}
