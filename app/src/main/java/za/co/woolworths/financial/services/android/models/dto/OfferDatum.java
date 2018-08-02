package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfferDatum {

	@SerializedName("creditLimit")
	@Expose
	public Integer creditLimit;
	@SerializedName("term")
	@Expose
	public String term;
	@SerializedName("approxInstallment")
	@Expose
	public Integer approxInstallment;

}