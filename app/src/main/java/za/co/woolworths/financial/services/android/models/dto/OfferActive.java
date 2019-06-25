package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

public class OfferActive {

	public Boolean offerActive;
	public Integer cliId;
	public String cliStatus;
	public String nextStepColour;
	public String nextStep;
	public String messageSummary;
	public String messageDetail;
	public Application application;
	public Offer offer;
	public Response response;

	@SerializedName("httpCode")
	public Integer httpCode;
}

