package za.co.woolworths.financial.services.android.models.service.event;

public class CartState {
	private String state;
	private int indexState;
	private int quantity;
	public static final int CHANGE_QUANTITY = 1;

	public CartState(String state) {
		this.state = state;
	}

	public CartState(int index, int quantity) {
		this.indexState = index;
		this.quantity = quantity;
	}

	public String getState() {
		return state;
	}

	public int getIndexState() {
		return indexState;
	}

	public int getQuantity() {
		return quantity;
	}
}
