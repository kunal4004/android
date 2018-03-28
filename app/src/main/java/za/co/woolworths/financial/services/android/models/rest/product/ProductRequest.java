package za.co.woolworths.financial.services.android.models.rest.product;

public class ProductRequest {
	private String productId;
	private String skuId;

	public ProductRequest(String productId, String skuId) {
		this.productId = productId;
		this.skuId = skuId;
	}

	public String getProductId() {
		return productId;
	}

	public String getSkuId() {
		return skuId;
	}
}
