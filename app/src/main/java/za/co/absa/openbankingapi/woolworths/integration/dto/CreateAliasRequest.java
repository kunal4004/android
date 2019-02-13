package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class CreateAliasRequest {

	@SerializedName("header")
	private Header header;

	@SerializedName("deviceId")
	private String deviceId;

	@SerializedName("symmetricKey")
	private String symmetricKey;

	public CreateAliasRequest(String deviceId, String symmetricKey){
		this.header = new Header();
		this.deviceId = deviceId;
		this.symmetricKey = symmetricKey;
	}



	public final String getJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
