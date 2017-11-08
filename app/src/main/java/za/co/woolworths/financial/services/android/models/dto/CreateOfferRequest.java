package za.co.woolworths.financial.services.android.models.dto;

public class CreateOfferRequest {

	public boolean maxCreditRequested;
	public int productOfferingId;
	public int grossMonthlyIncome;
	public int nettMonthlyIncome;
	public int additionalIncomeAmount;
	public int mortgagePaymentAmount;
	public int rentalPaymentAmount;
	public int maintenanceExpenseAmount;
	public int totalCreditExpenseAmount;
	public int otherExpenseAmount;

	public CreateOfferRequest() {
	}

	/**
	 * @param mortgagePaymentAmount
	 * @param rentalPaymentAmount
	 * @param grossMonthlyIncome
	 * @param nettMonthlyIncome
	 * @param totalCreditExpenseAmount
	 * @param maintenanceExpenseAmount
	 * @param additionalIncomeAmount
	 * @param productOfferingId
	 * @param otherExpenseAmount
	 */

	public CreateOfferRequest(boolean maxCreditRequested, int productOfferingId, int grossMonthlyIncome, int nettMonthlyIncome, int additionalIncomeAmount, int mortgagePaymentAmount, int rentalPaymentAmount, int maintenanceExpenseAmount, int totalCreditExpenseAmount, int otherExpenseAmount) {
		super();
		this.maxCreditRequested = maxCreditRequested;
		this.productOfferingId = productOfferingId;
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
