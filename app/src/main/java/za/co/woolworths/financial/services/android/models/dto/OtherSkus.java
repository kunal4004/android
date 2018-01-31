package za.co.woolworths.financial.services.android.models.dto;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherSkus implements Comparable<OtherSkus> {

	@SerializedName("sku")
	@Expose
	public String sku;
	@SerializedName("price")
	@Expose
	public String price;

	@SerializedName("wasPrice")
	@Expose
	public String wasPrice;

	@Override
	public int compareTo(@NonNull OtherSkus otherSku) {
		return this.wasPrice.compareTo(otherSku.wasPrice);
	}
}