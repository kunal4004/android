package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.SubstituteInfoDetails;
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap;

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
	@SerializedName("productCountMap")
	@Expose
	public ProductCountMap productCountMap;
	@SerializedName("substitutionInfo")
	@Expose
	public List<SubstituteInfoDetails> substitutionInfoList = null;
}
