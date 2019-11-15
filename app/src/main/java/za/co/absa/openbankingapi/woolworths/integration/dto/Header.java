package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class Header {

	@SerializedName("statuscode")
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

	@SerializedName("applicationId")
	private String applicationId;

	@SerializedName("applicationKeyID")
	private String applicationKeyId;

	@SerializedName("requestToken")
	private String requestToken;

	@SerializedName("resultMessages")
	private ResultMessage[] resultMessages;

	@SerializedName("paginationContext")
	private JsonElement paginationContext;


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

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setWfpt(String wfpt) {
		this.wfpt = wfpt;
	}

	public void setJsessionId(String jsessionId) {
		this.jsessionId = jsessionId;
	}

	public void setEsessionId(String esessionId) {
		this.esessionId = esessionId;
	}

	public void setXfpt(String xfpt) {
		this.xfpt = xfpt;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}

	public void setAccessAccount(String accessAccount) {
		this.accessAccount = accessAccount;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void setApplicationKeyId(String applicationKeyId) {
		this.applicationKeyId = applicationKeyId;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

	public void setResultMessages(ResultMessage[] resultMessages) {
		this.resultMessages = resultMessages;
	}

	public void setPaginationContext(JsonElement paginationContext) {
		this.paginationContext = paginationContext;
	}

	public JsonElement getPaginationContext() {
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

	public Header(){
		applicationId = "WCOBMOBAPP";
		language = "en";
		brand = "WCOBMOBAPP";
		organization = "WCOBMOBAPP";
		channel = "I";
	}
}
