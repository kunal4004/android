package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Account implements Serializable {
    public int productOfferingId;
    public int creditLimit;
    public int currentBalance;
    public int availableFunds;
    public int minimumAmountDue;
    public String paymentDueDate;
    public int minDrawDownAmount;
    public int rpCreditLimitThreshold;
    public String productGroupCode;
    public String accountNumberBin;
    public String productOfferingStatus;
    public boolean productOfferingGoodStanding;
    public int totalAmountDue;
    public int amountOverdue;
    public List<PaymentMethod> paymentMethods;
    public JsonElement bankingDetails;
    public DebitOrder debitOrder;
    public boolean insuranceCovered;
    public List<InsuranceType> insuranceTypes;
    @SerializedName("accountNumber")
    @Expose
    public String accountNumber;
}
