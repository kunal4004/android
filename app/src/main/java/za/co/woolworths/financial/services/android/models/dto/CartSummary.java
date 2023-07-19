package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails;

public class CartSummary {
	@SerializedName("totalItemsCount")
	@Expose
	public Integer totalItemsCount;
	@SerializedName("total")
	@Expose
	public Float total;
	@SerializedName("suburbId")
	@Expose
	public String suburbId;
	@SerializedName("suburbName")
	@Expose
	public String suburbName;
	@SerializedName("provinceName")
	@Expose
	public String provinceName;
	@SerializedName("suburb")
	@Expose
	public Suburb suburb;
	@SerializedName("provinceId")
	@Expose
	public String provinceId;
	@SerializedName("store")
	@Expose
	public Store store;
	@SerializedName("fulfillmentDetails")
	@Expose
	public FulfillmentDetails fulfillmentDetails;
	@SerializedName("deliveryDetails")
	@Expose
	public String deliveryDetails;
}
