package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddItemToCartResponse {
	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("data")
	@Expose
	public List<AddToCartDaTum> data = null;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;
}
