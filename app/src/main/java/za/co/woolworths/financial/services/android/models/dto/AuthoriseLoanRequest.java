package za.co.woolworths.financial.services.android.models.dto;

public class AuthoriseLoanRequest {
    public int productOfferingId;
    public int drawDownAmount;
    public int repaymentPeriod;
    public int installmentAmount;
    public int creditLimit;

    public AuthoriseLoanRequest(int productOfferingId, int drawDownAmount, int repaymentPeriod, int installmentAmount, int creditLimit) {
        this.productOfferingId = productOfferingId;
        this.drawDownAmount = drawDownAmount;
        this.repaymentPeriod = repaymentPeriod;
        this.installmentAmount = installmentAmount;
        this.creditLimit = creditLimit;
    }
}
