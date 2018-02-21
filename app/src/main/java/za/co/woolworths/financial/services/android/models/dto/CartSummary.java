package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CartSummary {
	@SerializedName("totalItemsCount")
	@Expose
	public Integer totalItemsCount;
	@SerializedName("total")
	@Expose
	public Float total;
	@SerializedName("suburbId")
	@Expose
	public Integer suburbId;
	@SerializedName("suburbName")
	@Expose
	public String suburbName;
	@SerializedName("provinceName")
	@Expose
	public String provinceName;
}
