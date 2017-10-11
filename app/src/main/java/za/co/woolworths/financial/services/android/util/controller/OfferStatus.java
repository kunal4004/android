package za.co.woolworths.financial.services.android.util.controller;


public enum OfferStatus {
	APPLY_NOW("apply_now"),
	OFFER_AVAILABLE("offer_available"),
	IN_PROGRESS("InProgress"),
	PLEASE_TRY_AGAIN("please_try_again"),
	POI_PROBLEM("poi_problem"),
	POI_REQUIRED("poi_required"),
	UNAVAILABLE("unavailable");

	private final String name;

	private OfferStatus(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		// (otherName == null) check is not needed because name.equals(null) returns false
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}