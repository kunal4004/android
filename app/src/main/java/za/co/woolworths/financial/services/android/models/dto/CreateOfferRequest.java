package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dimitrij on 2016/12/23.
 */

public class CreateOfferRequest {

    @SerializedName("productOfferingId")
    @Expose
    public int productOfferingId;
    @SerializedName("creditLimitRequested")
    @Expose
    public int creditLimitRequested;
    @SerializedName("grossMonthlyIncome")
    @Expose
    public int grossMonthlyIncome;
    @SerializedName("nettMonthlyIncome")
    @Expose
    public int nettMonthlyIncome;
    @SerializedName("additionalIncomeAmount")
    @Expose
    public int additionalIncomeAmount;
    @SerializedName("mortgagePaymentAmount")
    @Expose
    public int mortgagePaymentAmount;
    @SerializedName("rentalPaymentAmount")
    @Expose
    public int rentalPaymentAmount;
    @SerializedName("maintenanceExpenseAmount")
    @Expose
    public int maintenanceExpenseAmount;
    @SerializedName("totalCreditExpenseAmount")
    @Expose
    public int totalCreditExpenseAmount;
    @SerializedName("otherExpenseAmount")
    @Expose
    public int otherExpenseAmount;

    public CreateOfferRequest(int productOfferingId, int creditLimitRequested, int grossMonthlyIncome, int nettMonthlyIncome, int additionalIncomeAmount, int mortgagePaymentAmount, int rentalPaymentAmount, int maintenanceExpenseAmount, int totalCreditExpenseAmount, int otherExpenseAmount) {
        this.productOfferingId = productOfferingId;
        this.creditLimitRequested = creditLimitRequested;
        this.grossMonthlyIncome = grossMonthlyIncome;
        this.nettMonthlyIncome = nettMonthlyIncome;
        this.additionalIncomeAmount = additionalIncomeAmount;
        this.mortgagePaymentAmount = mortgagePaymentAmount;
        this.rentalPaymentAmount = rentalPaymentAmount;
        this.maintenanceExpenseAmount = maintenanceExpenseAmount;
        this.totalCreditExpenseAmount = totalCreditExpenseAmount;
        this.otherExpenseAmount = otherExpenseAmount;
    }
}
