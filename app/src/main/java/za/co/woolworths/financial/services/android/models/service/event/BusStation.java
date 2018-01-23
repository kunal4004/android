package za.co.woolworths.financial.services.android.models.service.event;

import za.co.woolworths.financial.services.android.models.dto.OfferActive;

public class BusStation {

	private int intValue;
	private OfferActive offerActive;
	private boolean makeApiCall;
	private String searchProductBrand;

	public BusStation(int intValue) {
		this.intValue = intValue;
	}

	public BusStation(boolean makeApiCall) {
		this.makeApiCall = makeApiCall;
	}

	public BusStation(OfferActive offerActive) {
		this.offerActive = offerActive;
	}

	public BusStation(String searchProductBrand) {
		this.searchProductBrand = searchProductBrand;
	}

	public String getSearchProductBrand() {
		return searchProductBrand;
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
