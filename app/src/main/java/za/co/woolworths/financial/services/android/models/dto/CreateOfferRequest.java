package za.co.woolworths.financial.services.android.models.dto;


public class CreateOfferRequest {

	public String channel;
	public boolean debtDisclosed;
	public boolean canObtainCreditInfo;
	public boolean canObtainBankStatements;
	public boolean applicationInfoIsCorrect;
	public boolean staffMember;
	public boolean automaticCreditIncrease;
	public boolean maxCreditRequested;
	public int productOfferingId;
	public int creditLimitRequested;
	public int grossMonthlyIncome;
	public int nettMonthlyIncome;
	public int additionalIncomeAmount;
	public int mortgagePaymentAmount;
	public int rentalPaymentAmount;
	public int maintenanceExpenseAmount;
	public int totalCreditExpenseAmount;
	public int otherExpenseAmount;

	/**
	 * No args constructor for use in serialization
	 */
	public CreateOfferRequest() {
	}

	/**
	 * @param otherExpenseAmount
	 * @param canObtainCreditInfo
	 * @param maxCreditRequested
	 * @param staffMember
	 * @param debtDisclosed
	 * @param mortgagePaymentAmount
	 * @param canObtainBankStatements
	 * @param rentalPaymentAmount
	 * @param applicationInfoIsCorrect
	 * @param grossMonthlyIncome
	 * @param nettMonthlyIncome
	 * @param totalCreditExpenseAmount
	 * @param automaticCreditIncrease
	 * @param maintenanceExpenseAmount
	 * @param additionalIncomeAmount
	 * @param productOfferingId
	 * @param channel
	 * @param creditLimitRequested
	 */
	public CreateOfferRequest(String channel, boolean debtDisclosed, boolean canObtainCreditInfo, boolean canObtainBankStatements, boolean applicationInfoIsCorrect, boolean staffMember, boolean automaticCreditIncrease, boolean maxCreditRequested, int productOfferingId, int creditLimitRequested, int grossMonthlyIncome, int nettMonthlyIncome, int additionalIncomeAmount, int mortgagePaymentAmount, int rentalPaymentAmount, int maintenanceExpenseAmount, int totalCreditExpenseAmount, int otherExpenseAmount) {
		super();
		this.channel = channel;
		this.debtDisclosed = debtDisclosed;
		this.canObtainCreditInfo = canObtainCreditInfo;
		this.canObtainBankStatements = canObtainBankStatements;
		this.applicationInfoIsCorrect = applicationInfoIsCorrect;
		this.staffMember = staffMember;
		this.automaticCreditIncrease = automaticCreditIncrease;
		this.maxCreditRequested = maxCreditRequested;
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
