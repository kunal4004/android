package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CLICreateOfferResponse {

	@SerializedName("response")
	@Expose
	public Response response;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;
	@SerializedName("cliOfferId")
	@Expose
	public int cliOfferId;
	@SerializedName("cli")
	@Expose
	public Cli cli;
}
