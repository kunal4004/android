package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SkuInventory {

	@SerializedName("sku")
	@Expose
	public String sku;
	@SerializedName("storesInventory")
	@Expose
	public List<StoresInventory> storesInventory = null;
	@SerializedName("quantity")
	@Expose
	public Integer quantity;

}
