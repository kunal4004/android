package za.co.woolworths.financial.services.android.models.dto;


public class ChangeQuantity {

	private String commerceId;
	private int quantity;

	public ChangeQuantity() {
	}

	public ChangeQuantity(int quantity, String commerceId) {
		this.quantity = quantity;
		this.commerceId = commerceId;
	}

	public void setCommerceId(String commerceId) {
		this.commerceId = commerceId;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getCommerceId() {
		return commerceId;
	}
}
