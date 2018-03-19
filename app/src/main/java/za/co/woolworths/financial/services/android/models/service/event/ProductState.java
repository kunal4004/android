package za.co.woolworths.financial.services.android.models.service.event;

public class ProductState {

	public static final String POST_ADD_ITEM_TO_CART = "POST_ADD_ITEM_TO_CART";
	public static final String DETERMINE_LOCATION_POPUP = "DETERMINE_LOCATION_POPUP";
	public static final String ADD_TO_SHOPPING_LIST = "ADD_TO_SHOPPING_LIST";
	public static final String USE_MY_LOCATION = "USE_MY_LOCATION";
	public static final String SET_SUBURB = "SET_SUBURB";
	public static final String SET_SUBURB_API = "SET_SUBURB_API";
	public static final String CANCEL_CALL = "CANCEL_CALL";

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
