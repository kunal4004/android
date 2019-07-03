package za.co.woolworths.financial.services.android.models.service.event;

import za.co.woolworths.financial.services.android.models.dto.OfferActive;

public class BusStation {

	private String mObject;
	private Integer intValue,drawnDownAmount;
	private OfferActive offerActive;
	private boolean makeApiCall;

	public BusStation(int intValue) {
		this.intValue = intValue;
	}

	public BusStation(int intValue,int drawnDownAmount) {
		this.intValue = intValue;
		this.drawnDownAmount = drawnDownAmount;
	}

	public BusStation(boolean makeApiCall) {
		this.makeApiCall = makeApiCall;
	}

	public BusStation(OfferActive offerActive) {
		this.offerActive = offerActive;
	}

	public BusStation(String object) {
		mObject = object;
	}

	public Integer getNumber() {
		return intValue;
	}

	public OfferActive getOfferActive() {
		return offerActive;
	}

	public boolean makeApiCall() {
		return makeApiCall;
	}

	public String getString() {
		return mObject;
	}

	public Integer getDrawnDownAmount() {
		return drawnDownAmount;
	}
}
