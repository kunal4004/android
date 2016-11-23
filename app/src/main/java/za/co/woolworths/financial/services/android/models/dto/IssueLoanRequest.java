package za.co.woolworths.financial.services.android.models.dto;

public class IssueLoanRequest {
    public int productOfferingId;
    public int drawDownAmount;
    public int repaymentPeriod;
    public int creditLimit;

    public IssueLoanRequest(int productOfferingId, int drawDownAmount, int repaymentPeriod, int creditLimit) {
        this.productOfferingId = productOfferingId;
        this.drawDownAmount = drawDownAmount;
        this.repaymentPeriod = repaymentPeriod;
        this.creditLimit = creditLimit;
    }
}
