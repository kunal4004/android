package za.co.woolworths.financial.services.android.models.service.event;


import za.co.woolworths.financial.services.android.models.dto.OfferActive;

public class BusStation {

	private int intValue;
	private OfferActive offerActive;
	private boolean makeApiCall;

	public BusStation(int intValue) {
		this.intValue = intValue;
	}

	public BusStation(boolean makeApiCall) {
		this.makeApiCall = makeApiCall;
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

	public boolean makeApiCall() {
		return makeApiCall;
	}
}
