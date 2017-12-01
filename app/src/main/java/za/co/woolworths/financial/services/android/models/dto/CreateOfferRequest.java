package za.co.woolworths.financial.services.android.models.dto;

public class CreateOfferRequest {

	private int productOfferingId;
	private int grossMonthlyIncome;
	private int netMonthlyIncome;
	private int additionalIncomeAmount;
	private int mortgagePaymentAmount;
	private int rentalPaymentAmount;
	private int maintenanceExpenseAmount;
	private int totalCreditExpenseAmount;
	private int otherExpenseAmount;
	private boolean maxCreditRequested = true;

	public CreateOfferRequest(boolean maxCreditRequested, int productOfferingId, int grossMonthlyIncome, int netMonthlyIncome, int additionalIncomeAmount, int mortgagePaymentAmount, int rentalPaymentAmount, int maintenanceExpenseAmount, int totalCreditExpenseAmount, int otherExpenseAmount) {
		super();
		this.maxCreditRequested = maxCreditRequested;
		this.productOfferingId = productOfferingId;
		this.grossMonthlyIncome = grossMonthlyIncome;
		this.netMonthlyIncome = netMonthlyIncome;
		this.additionalIncomeAmount = additionalIncomeAmount;
		this.mortgagePaymentAmount = mortgagePaymentAmount;
		this.rentalPaymentAmount = rentalPaymentAmount;
		this.maintenanceExpenseAmount = maintenanceExpenseAmount;
		this.totalCreditExpenseAmount = totalCreditExpenseAmount;
		this.otherExpenseAmount = otherExpenseAmount;
	}
}
