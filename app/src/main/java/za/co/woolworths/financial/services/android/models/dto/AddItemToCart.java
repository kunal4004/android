package za.co.woolworths.financial.services.android.models.dto;

public class AddItemToCart {

	private String productId;
	private String catalogRefId;
	private int quantity;
	private String substitutionSelection;
	private String substitutionId;

	public AddItemToCart(String productId, String catalogRefId, int quantity) {
		this.productId = productId;
		this.catalogRefId = catalogRefId;
		this.quantity = quantity;
	}

	public AddItemToCart(String productId, String catalogRefId, int quantity, String substitutionSelection, String substitutionId) {
		this.productId = productId;
		this.catalogRefId = catalogRefId;
		this.quantity = quantity;
		this.substitutionSelection = substitutionSelection;
		this.substitutionId = substitutionId;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getProductId() {
		return productId;
	}

	public String getCatalogRefId() {
		return catalogRefId;
	}
}
