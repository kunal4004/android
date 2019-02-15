package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class RegisterCredentialRequest {
	@SerializedName("header")
	private Header header;

	@SerializedName("aliasId")
	private String aliasId;

	@SerializedName("deviceId")
	private String deviceId;

	@SerializedName("credentialVOs")
	private CredentialVO[] credentialVOs;

	@SerializedName("symmetricKey")
	private String symmetricKey;


	public static class CredentialVO {
		@SerializedName("aliasId")
		private String aliasId;

		@SerializedName("type")
		private String type;

		@SerializedName("credential")
		private String credential;

		public CredentialVO(String aliasId, String type, String credential){
			this.aliasId = aliasId;
			this.type = type;
			this.credential = credential;
		}
	}

	public RegisterCredentialRequest(String aliasId, String deviceId, CredentialVO[] credentialVOs, String symmetricKey){
		this.header = new Header();
		this.aliasId = aliasId;
		this.deviceId = deviceId;
		this.credentialVOs = credentialVOs;
		this.symmetricKey = symmetricKey;
	}

	public final String getJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
