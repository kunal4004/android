package za.co.woolworths.financial.services.android.models.service.event;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;

public class ShopState {

	private String listId;
	private String state;
	private List<ShoppingListItem> updateList;
	private int quantity;
	public static final int CHANGE_QUANTITY = 1;
	private int count = 0;

	public ShopState(String state) {
		this.state = state;
	}

	public ShopState(String state, String listId) {
		this.state = state;
		this.listId = listId;
	}

	public ShopState(List<ShoppingListItem> updateList, int count) {
		this.updateList = updateList;
		this.count = count;
	}

	public String getState() {
		return state;
	}

	public String getListId() {
		return listId;
	}

	public int getQuantity() {
		return quantity;
	}


	public List<ShoppingListItem> getUpdatedList() {
		return updateList;
	}

	public int getCount() {
		return count;
	}
}
