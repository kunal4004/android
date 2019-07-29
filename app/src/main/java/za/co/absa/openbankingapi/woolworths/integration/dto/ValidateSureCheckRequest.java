package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class ValidateSureCheckRequest {

	@SerializedName("header")
	private Header header;

	@SerializedName("securityNotificationType")
	private SecurityNotificationType securityNotificationType;

	@SerializedName("otpToBeVerified")
	private String otpToBeVerified;

    public ValidateSureCheckRequest(SecurityNotificationType securityNotificationType) {
        this.securityNotificationType = securityNotificationType;
        this.header = new Header();
    }

	public final String getJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}

}
