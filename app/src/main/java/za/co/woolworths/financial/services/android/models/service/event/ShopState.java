package za.co.woolworths.financial.services.android.models.service.event;

public class ShopState {

	private String listId;
	private String state;

	public ShopState(String state) {
		this.state = state;
	}

	public ShopState(String state, String listId) {
		this.state = state;
		this.listId = listId;
	}

	public String getState() {
		return state;
	}

	public String getListId() {
		return listId;
	}
}
