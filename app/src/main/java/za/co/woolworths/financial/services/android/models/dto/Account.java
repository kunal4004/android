package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.npc.PrimaryCard;
import za.co.woolworths.financial.services.android.models.dto.npc.SecondaryCard;

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
    public List<PaymentMethod> paymentMethods;
    public JsonElement bankingDetails;
    public DebitOrder debitOrder;
    public boolean insuranceCovered;
    public List<InsuranceType> insuranceTypes;
    public List<Card> cards = null;
    @SerializedName("accountNumber")
    @Expose
    public String accountNumber;
    @SerializedName("primaryCard")
    @Expose
    public PrimaryCard primaryCard;
    @SerializedName("secondaryCard")
    @Expose
    public SecondaryCard secondaryCard;

}
