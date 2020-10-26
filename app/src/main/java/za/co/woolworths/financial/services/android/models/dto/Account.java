package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.npc.PrimaryCard;

public class Account implements Serializable {

	@SerializedName("productOfferingId")
	@Expose
	public int productOfferingId;
	@SerializedName("creditLimit")
	@Expose
	public int creditLimit;
	@SerializedName("currentBalance")
	@Expose
	public int currentBalance;
	@SerializedName("availableFunds")
	@Expose
	public int availableFunds;
	@SerializedName("minimumAmountDue")
	@Expose
	public int minimumAmountDue;
	@SerializedName("paymentDueDate")
	@Expose
	public String paymentDueDate;
	@SerializedName("minDrawDownAmount")
	@Expose
	public int minDrawDownAmount;
	@SerializedName("rpCreditLimitThreshold")
	@Expose
	public int rpCreditLimitThreshold;
	@SerializedName("productGroupCode")
	@Expose
	public String productGroupCode;
	@SerializedName("accountNumberBin")
	@Expose
	public String accountNumberBin;
	@SerializedName("productOfferingStatus")
	@Expose
	public String productOfferingStatus;
	@SerializedName("productOfferingGoodStanding")
	@Expose
	public boolean productOfferingGoodStanding;
	@SerializedName("totalAmountDue")
	@Expose
	public int totalAmountDue;
	@SerializedName("amountOverdue")
	@Expose
	public int amountOverdue;
	@SerializedName("paymentMethods")
	@Expose
	public List<PaymentMethod> paymentMethods;
	@SerializedName("bankingDetails")
	@Expose
	public JsonElement bankingDetails;
	@SerializedName("debitOrder")
	@Expose
	public DebitOrder debitOrder;
	@SerializedName("insuranceCovered")
	@Expose
	public boolean insuranceCovered;
	@SerializedName("insuranceTypes")
	@Expose
	public List<InsuranceType> insuranceTypes;
	public List<Card> cards = new ArrayList<>();
	@SerializedName("accountNumber")
	@Expose
	public String accountNumber;
	@SerializedName("primaryCard")
	@Expose
	public PrimaryCard primaryCard;
}

