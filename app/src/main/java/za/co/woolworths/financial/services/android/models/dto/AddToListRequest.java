package za.co.woolworths.financial.services.android.models.dto;

public class AddToListRequest {

	private String skuID;
	private String giftListId;
	private String catalogRefId;
	private String quantity;

	public void setSkuID(String skuID) {
		this.skuID = skuID;
	}

	public void setGiftListId(String giftListId) {
		this.giftListId = giftListId;
	}

	public void setCatalogRefId(String catalogRefId) {
		this.catalogRefId = catalogRefId;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getSkuID() {
		return skuID;
	}

	public String getCatalogRefId() {
		return catalogRefId;
	}

	public String getGiftListId() {
		return giftListId;
	}

	public String getQuantity() {
		return quantity;
	}
}
