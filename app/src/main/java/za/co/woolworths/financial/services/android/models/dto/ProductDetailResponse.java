package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductDetailResponse {

	@SerializedName("product")
	@Expose
	public ProductDetails product;
	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;

	public void setProduct(ProductDetails product) {
		this.product = product;
	}
}
