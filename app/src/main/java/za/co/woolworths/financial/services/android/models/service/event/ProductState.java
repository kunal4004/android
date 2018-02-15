package za.co.woolworths.financial.services.android.models.service.event;


public class ProductState {

	public static String POST_ADD_ITEM_TO_CART = "POST_ADD_ITEM_TO_CART";

	private String state;
	private int quantity;

	public ProductState(String state) {
		this.state = state;
	}

	public ProductState(String state, int quantity) {
		this.state = state;
		this.quantity = quantity;
	}

	public String getState() {
		return state;
	}

	public int getQuantity() {
		return quantity;
	}
}
