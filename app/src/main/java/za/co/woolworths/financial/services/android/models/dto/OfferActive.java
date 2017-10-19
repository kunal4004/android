package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfferActive {

	@SerializedName("offerActive")
	@Expose
	public Boolean offerActive;
	@SerializedName("cli")
	@Expose
	public Cli cli;
	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;

}

