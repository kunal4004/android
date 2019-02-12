package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class Header {

	@SerializedName("statusode")
	private String statusCode;

	@SerializedName("userAgent")
	private String userAgent;

	@SerializedName("wfpt")
	private String wfpt;

	@SerializedName("jsessionid")
	private String jsessionId;

	@SerializedName("esessionid")
	private String esessionId;

	@SerializedName("xfpt")
	private String xfpt;

	@SerializedName("service")
	private String service;

	@SerializedName("operation")
	private String operation;

	@SerializedName("channel")
	private String channel;

	@SerializedName("timestamp")
	private String timestamp;

	@SerializedName("sourceip")
	private String sourceIp;

	@SerializedName("accessAccount")
	private String accessAccount;

	@SerializedName("userNumber")
	private String userNumber;

	@SerializedName("nonce")
	private String nonce;

	@SerializedName("language")
	private String language;

	@SerializedName("organization")
	private String organization;

	@SerializedName("brand")
	private String brand;

	@SerializedName("applicationID")
	private String applicationId;

	@SerializedName("applicationKeyID")
	private String applicationKeyId;

	@SerializedName("requestToken")
	private String requestToken;

	@SerializedName("resultMessages")
	private ResultMessage[] resultMessages;

	@SerializedName("paginationContext")
	private String paginationContext;


	public String getStatusCode() {
		return statusCode;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getWfpt() {
		return wfpt;
	}

	public String getJsessionId() {
		return jsessionId;
	}

	public String getEsessionId() {
		return esessionId;
	}

	public String getXfpt() {
		return xfpt;
	}

	public String getService() {
		return service;
	}

	public String getOperation() {
		return operation;
	}

	public String getChannel() {
		return channel;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public String getAccessAccount() {
		return accessAccount;
	}

	public String getUserNumber() {
		return userNumber;
	}

	public String getNonce() {
		return nonce;
	}

	public String getLanguage() {
		return language;
	}

	public String getOrganization() {
		return organization;
	}

	public String getBrand() {
		return brand;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getApplicationKeyId() {
		return applicationKeyId;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public ResultMessage[] getResultMessages() {
		return resultMessages;
	}

	public String getPaginationContext() {
		return paginationContext;
	}

	public class ResultMessage {

		@SerializedName("responseSeverity")
		private String responseSeverity;

		@SerializedName("responseMessage")
		private String responseMessage;

		public String getResponseSeverity() {
			return responseSeverity;
		}

		public String getResponseMessage() {
			return responseMessage;
		}
	}
}
