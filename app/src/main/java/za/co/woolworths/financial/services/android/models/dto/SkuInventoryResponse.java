package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SkuInventoryResponse {

	@SerializedName("skuInventory")
	@Expose
	public List<SkuInventory> skuInventory = null;
	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;

}