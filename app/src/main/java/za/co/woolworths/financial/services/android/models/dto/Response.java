package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {

	@SerializedName("code")
	@Expose
	public String code;
	@SerializedName("desc")
	@Expose
	public String desc;
	@SerializedName("stsParams")
	@Expose
	public String stsParams;
	@SerializedName("message")
	@Expose
	public String message;
	@SerializedName("version")
	@Expose
	public String version;
}