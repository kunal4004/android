package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

	@SerializedName("header")
	private Header header;

	@SerializedName("result")
	private String result;

	@SerializedName("nonce")
	private String nonce;

	@SerializedName("esessionid")
	public String esessionid;

	@SerializedName("timestamp")
	private String timestamp;

	@SerializedName("lastLogin")
	private String lastLogin;

	@SerializedName("surePhrase")
	private String surePhrase;

	@SerializedName("limitsEnabled")
	private boolean limitsEnabled;

	@SerializedName("simSwapHold")
	private boolean simSwapHold;

	@SerializedName("rvnBranchHold")
	private boolean rvnBranchHold;

	@SerializedName("ficaStatus")
	private String ficaStatus;

	@SerializedName("userPreferredLanguage")
	private String userPreferredLanguage;

	@SerializedName("registrationDate")
	private String registrationDate;

	@SerializedName("resultMessage")
	private String resultMessage;

	@SerializedName("landingPage")
	private String landingPage;

	public String getNonce() {
		return nonce;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public Header getHeader() {
		return header;
	}

	public String getResult() {
		return result;
	}
}
