package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class ValidateCardAndPinResponse {

	@SerializedName("header")
	private Header header;

	@SerializedName("result")
	private String result;

	@SerializedName("securityNotificationType")
	public SecurityNotificationType securityNotificationType;

	@SerializedName("cellNumber")
	public String cellNumber;

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
