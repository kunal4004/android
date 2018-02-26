package za.co.woolworths.financial.services.android.models.service.event;


public class BadgeState {

	public static final int CART_COUNT = 0;
	public static final int REWARD_COUNT = 1;
	public static final int MESSAGE_COUNT = 2;
	private int position;
	private int count;

	public BadgeState(int position, int count) {
		this.position = position;
		this.count = count;
	}

	public int getPosition() {
		return position;
	}

	public int getCount() {
		return count;
	}
}
