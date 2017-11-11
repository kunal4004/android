package za.co.woolworths.financial.services.android.models.service.event;


import za.co.woolworths.financial.services.android.models.dto.OfferActive;

public class BusStation {

	private int intValue;
	private OfferActive offerActive;

	public BusStation(int intValue) {
		this.intValue = intValue;
	}

	public BusStation(OfferActive offerActive) {
		this.offerActive = offerActive;
	}

	public int getNumber() {
		return intValue;
	}

	public OfferActive getOfferActive() {
		return offerActive;
	}
}
