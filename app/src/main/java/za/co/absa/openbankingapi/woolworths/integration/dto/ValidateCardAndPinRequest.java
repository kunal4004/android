package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class ValidateCardAndPinRequest {

	@SerializedName("header")
	private Header header;

	@SerializedName("cardToken")
	private String cardToken;

	@SerializedName("cardPIN")
	private String cardPin;

	@SerializedName("symmetricKey")
	private String symmetricKey;

	public ValidateCardAndPinRequest(final String cardToken, final String cardPin, final String symmetricKey){
		this.header = new Header();
		this.cardToken = cardToken;
		this.cardPin = cardPin;
		this.symmetricKey = symmetricKey;
	}

	public final String getJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
