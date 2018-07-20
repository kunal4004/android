package za.co.woolworths.financial.services.android.models.dto;

public class CLIOfferDecision {

	private Integer productOfferingId;
	private Integer creditLimitAccepted;
	private boolean offerAccepted;

	public CLIOfferDecision() {
	}

	public CLIOfferDecision(Integer productOfferingId, Integer creditLimitAccepted, boolean offerAccepted) {
		this.productOfferingId = productOfferingId;
		this.creditLimitAccepted = creditLimitAccepted;
		this.offerAccepted = offerAccepted;
	}
}