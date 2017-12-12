package za.co.woolworths.financial.services.android.models.dto;

public class CreateOfferRequest {

	private String channel = "WWOneApp";
	private boolean maxCreditRequested = true;
	private int productOfferingId;
	private int grossMonthlyIncome;
	private int nettMonthlyIncome;
	private int additionalIncomeAmount;
	private int mortgagePaymentAmount;
	private int rentalPaymentAmount;
	private int maintenanceExpenseAmount;
	private int totalCreditExpenseAmount;
	private int otherExpenseAmount;

	public CreateOfferRequest(int productOfferingId, int grossMonthlyIncome, int nettMonthlyIncome, int additionalIncomeAmount, int mortgagePaymentAmount, int rentalPaymentAmount, int maintenanceExpenseAmount, int totalCreditExpenseAmount, int otherExpenseAmount) {
		super();
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
