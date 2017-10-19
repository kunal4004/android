package za.co.woolworths.financial.services.android.models.dto;

public class CreateOfferDecision {

	public Integer productOfferingId;
	public Integer cliOfferId;
	public String decision;
	public Integer creditLimit;

	/**
	 * No args constructor for use in serialization
	 */
	public CreateOfferDecision() {
	}

	/**
	 * @param cliOfferId
	 * @param creditLimit
	 * @param productOfferingId
	 * @param decision
	 */
	public CreateOfferDecision(Integer productOfferingId, Integer cliOfferId, String decision, Integer creditLimit) {
		super();
		this.productOfferingId = productOfferingId;
		this.cliOfferId = cliOfferId;
		this.decision = decision;
		this.creditLimit = creditLimit;
	}
}