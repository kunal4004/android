package za.co.woolworths.financial.services.android.util.controller;


public enum EventStatus {
	CREATE_OFFER(1), UPDATE_OFFER(2), NONE(3);
	private int value;

	public static final EventStatus values[] = values();

	private EventStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	//Just for testing from some SO answers, but no use
	public void setValue(int value) {
		this.value = value;
	}
}