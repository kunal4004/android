package za.co.woolworths.financial.services.android.models.service.event;

import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;

public class ProductState {

	public static final String POST_ADD_ITEM_TO_CART = "POST_ADD_ITEM_TO_CART";
	public static final String DETERMINE_LOCATION_POPUP = "DETERMINE_LOCATION_POPUP";
	public static final String USE_MY_LOCATION = "USE_MY_LOCATION";
	public static final String SET_SUBURB = "SET_SUBURB";
	public static final String SET_SUBURB_API = "SET_SUBURB_API";
	public static final String CANCEL_DIALOG_TAPPED = "CANCEL_DIALOG_TAPPED";
	public static final String OPEN_ADD_TO_SHOPPING_LIST_VIEW = "OPEN_ADD_TO_SHOPPING_LIST_VIEW";
	public static final String INDEX_SEARCH_FROM_LIST = "INDEX_SEARCH_FROM_LIST";
	public static final String CLOSE_PDP_FROM_ADD_TO_LIST = "CLOSE_PDP_FROM_ADD_TO_LIST";
	public static final String SHOW_ADDED_TO_SHOPPING_LIST_TOAST = "SHOW_ADDED_TO_SHOPPING_LIST_TOAST";
	public static final String OPEN_GET_LIST_SCREEN = "OPEN_GET_LIST_SCREEN";

	private String state;
	private int quantity;
	private int count;

	public ProductState(String state) {
		this.state = state;
	}

	public ProductState(String state, int quantity) {
		this.state = state;
		this.quantity = quantity;
	}

	public ProductState(int count, String state) {
		this.state = state;
		this.count = count;
	}

	public String getState() {
		return state;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getCount() {
		return count;
	}

}
