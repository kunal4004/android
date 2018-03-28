package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductDetail {

	@SerializedName("product")
	@Expose
	public Product product;
	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;
}
