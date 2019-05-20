package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class ValidateSureCheckRequest {


	@SerializedName("connectionTimeout")
	private long connectionTimeout;

	@SerializedName("readTimeout")
	private long readTimeout;

	@SerializedName("maxConnections")
	private long maxConnections;

	@SerializedName("urlString")
	private String urlString;

	@SerializedName("parameters")
	private String parameters;

	@SerializedName("headers")
	private Headers headers;

	@SerializedName("requestMethod")
	private String requestMethod;

	@SerializedName("rawJSessionId")
	private String rawJSessionId;

	@SerializedName("proxyEnabled")
	private boolean proxyEnabled;

	@SerializedName("proxyServerAddress")
	private String proxyServerAddress;

	@SerializedName("proxyServerPort")
	private long proxyServerPort;

	@SerializedName("proxyAuthenticationEnabled")
	private boolean proxyAuthenticationEnabled;

	@SerializedName("proxyUser")
	private String proxyUser;

	@SerializedName("proxyUserPassword")
	private String proxyUserPassword;

	@SerializedName("contentMIMEType")
	private String contentMIMEType;

	@SerializedName("responseMIMEType")
	private String responseMIMEType;

	@SerializedName("httpNotOkExceptionEnabled")
	private boolean httpNotOkExceptionEnabled;

	public ValidateSureCheckRequest(){

		connectionTimeout = 5000;
		readTimeout = 180000;
		maxConnections = 200;
		proxyServerPort = 0;
		urlString = "http://LAB10PF0V94B5.ds1.ad.absa.co.za/axob/wfsMobileRegistration";
		requestMethod = "POST";
		contentMIMEType = "json";
		responseMIMEType = "json";
		proxyEnabled = false;
		proxyAuthenticationEnabled = false;
		httpNotOkExceptionEnabled = false;
		headers = new Headers();
		}

	public final String getJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}

	public class Headers {

		@SerializedName("action")
		private String[] action;

		public Headers(){
			action = new String[1];
			action[0] = "validateSurecheck";
		}
	}
}
