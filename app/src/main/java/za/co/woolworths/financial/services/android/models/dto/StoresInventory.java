package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoresInventory {

	@SerializedName("storeId")
	@Expose
	public String storeId;
	@SerializedName("quantity")
	@Expose
	public Integer quantity;

}