package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddToCartDaTum {
	@SerializedName("suburbId")
	@Expose
	public String suburbId;
	@SerializedName("formexceptions")
	@Expose
	public List<FormException> formexceptions = null;
	@SerializedName("message")
	@Expose
	public String message;
	@SerializedName("totalCommerceIteItemCount")
	@Expose
	public Integer totalCommerceIteItemCount;
}
