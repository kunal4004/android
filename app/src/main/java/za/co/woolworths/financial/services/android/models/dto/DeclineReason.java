package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeclineReason {

	@SerializedName("id")
	@Expose
	public Integer id;
	@SerializedName("reasonId")
	@Expose
	public String reasonId;
	@SerializedName("reasonDesc")
	@Expose
	public String reasonDesc;

}