package za.co.woolworths.financial.services.android.models.dto;

public class AddItemToCart {

	private String productId;
	private String catalogRefId;
	private int quantity;

	public AddItemToCart(String productId, String catalogRefId, int quantity) {
		this.productId = productId;
		this.catalogRefId = catalogRefId;
		this.quantity = quantity;
	}
}
