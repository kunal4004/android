package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Cli {
	@SerializedName("cliId")
	@Expose
	public Integer cliId;
	@SerializedName("cliStatus")
	@Expose
	public String cliStatus;
	@SerializedName("nextStep")
	@Expose
	public String nextStep;
	@SerializedName("messageSummary")
	@Expose
	public String messageSummary;
	@SerializedName("messageDetail")
	@Expose
	public String messageDetail;
	@SerializedName("application")
	@Expose
	public Application application;
	@SerializedName("offer")
	@Expose
	public Offer offer;

}