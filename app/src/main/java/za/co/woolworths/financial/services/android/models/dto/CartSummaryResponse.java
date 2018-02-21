package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CartSummaryResponse {

	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("data")
	@Expose
	public List<CartSummary> data = null;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;

	private AddItemToCartResponse addItemToCartResponse;

	public CartSummaryResponse() {
	}

	public CartSummaryResponse(AddItemToCartResponse addItemToCartResponse) {
		this.addItemToCartResponse = addItemToCartResponse;
	}

	public AddItemToCartResponse getAddItemToCartResponse() {
		return addItemToCartResponse;
	}
}
