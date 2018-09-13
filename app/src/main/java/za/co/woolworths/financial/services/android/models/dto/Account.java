package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;

import java.util.List;

public class Account {
    public int productOfferingId;
    public int creditLimit;
    public int currentBalance;
    public int availableFunds;
    public int minimumAmountDue;
    public String paymentDueDate;
    public int minDrawDownAmount;
    public int rpCreditLimitThreshold;
    public boolean retrievalError;
    public String productGroupCode;
    public String accountNumberBin;
    public String productOfferingStatus;
    public boolean productOfferingGoodStanding;
    public int totalAmountDue;
    public int amountOverdue;
    public String insuranceCovered;
    public List<PaymentMethod> paymentMethods;
    public JsonElement bankingDetails;
    public DebitOrder debitOrder;

}
