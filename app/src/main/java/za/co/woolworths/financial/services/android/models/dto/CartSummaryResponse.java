package za.co.woolworths.financial.services.android.models.dto;

public class CartSummaryResponse {

	private AddItemToCartResponse addItemToCartResponse;

	public CartSummaryResponse(AddItemToCartResponse addItemToCartResponse) {
		this.addItemToCartResponse = addItemToCartResponse;
	}

	public AddItemToCartResponse getAddItemToCartResponse() {
		return addItemToCartResponse;
	}
}
