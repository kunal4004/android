package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SuburbFulfillment {

	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("id")
	@Expose
	public String id;
	@SerializedName("fulfillmentStores")
	@Expose
	public JsonElement fulfillmentStores;
}
