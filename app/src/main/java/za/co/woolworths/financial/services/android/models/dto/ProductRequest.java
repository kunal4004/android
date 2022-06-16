package za.co.woolworths.financial.services.android.models.dto;

public class ProductRequest {
	private String productId;
	private String skuId;
	private Boolean isUserBrowsing;

	public ProductRequest(String productId, String skuId, Boolean isUserBrowsing) {
		this.productId = productId;
		this.skuId = skuId;
		this.isUserBrowsing = isUserBrowsing;
	}

	public String getProductId() {
		return productId;
	}

	public String getSkuId() {
		return skuId;
	}

	public Boolean isUserBrowsing() {
		return isUserBrowsing;
	}
}
