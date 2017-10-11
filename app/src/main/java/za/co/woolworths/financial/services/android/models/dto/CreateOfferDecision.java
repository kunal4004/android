package za.co.woolworths.financial.services.android.models.dto;

public class CreateOfferDecision {

	private String cliOfferId;
	private String decision;
	private String creditLimit;

	public CreateOfferDecision(String cliOfferId, String decision, String creditLimit) {
		this.cliOfferId = cliOfferId;
		this.decision = decision;
		this.creditLimit = creditLimit;
	}

	public String getCliOfferId() {
		return cliOfferId;
	}
}
