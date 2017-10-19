package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Application {

	@SerializedName("channel")
	@Expose
	public String channel;
	@SerializedName("debtDisclosed")
	@Expose
	public Boolean debtDisclosed;
	@SerializedName("canObtainCreditInfo")
	@Expose
	public Boolean canObtainCreditInfo;
	@SerializedName("canObtainBankStatements")
	@Expose
	public Boolean canObtainBankStatements;
	@SerializedName("applicationInfoIsCorrect")
	@Expose
	public Boolean applicationInfoIsCorrect;
	@SerializedName("staffMember")
	@Expose
	public Boolean staffMember;
	@SerializedName("automaticCreditIncrease")
	@Expose
	public Boolean automaticCreditIncrease;
	@SerializedName("maxCreditRequested")
	@Expose
	public Boolean maxCreditRequested;
	@SerializedName("creditRequested")
	@Expose
	public Integer creditRequested;
	@SerializedName("grossMonthlyIncome")
	@Expose
	public Integer grossMonthlyIncome;
	@SerializedName("netMonthlyIncome")
	@Expose
	public Integer netMonthlyIncome;
	@SerializedName("additionalIncomeAmount")
	@Expose
	public Integer additionalIncomeAmount;
	@SerializedName("mortgagePaymentAmount")
	@Expose
	public Integer mortgagePaymentAmount;
	@SerializedName("rentalPaymentAmount")
	@Expose
	public Integer rentalPaymentAmount;
	@SerializedName("maintenanceExpenseAmount")
	@Expose
	public Integer maintenanceExpenseAmount;
	@SerializedName("totalCreditExpenseAmount")
	@Expose
	public Integer totalCreditExpenseAmount;
	@SerializedName("otherExpenseAmount")
	@Expose
	public Integer otherExpenseAmount;

}