package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class ValidateSureCheckResponse {

	@SerializedName("header")
	private Header header;

	@SerializedName("result")
	private String result;

	@SerializedName("securityNotificationType")
	private SecurityNotificationType securityNotificationType;

	@SerializedName("cellNumber")
	private String cellNumber;

	@SerializedName("resendsRemaining")
	private int resendsRemaining;

	@SerializedName("otpRetriesLeft")
	private int otpRetriesLeft;

	public Header getHeader() {
		return header;
	}

	public String getResult() {
		return result;
	}
}
