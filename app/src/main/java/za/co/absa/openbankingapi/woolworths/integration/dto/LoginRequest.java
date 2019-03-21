package za.co.absa.openbankingapi.woolworths.integration.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginRequest {

	private int sat;
	private String applicationId;
	private String aliasId;
	private String deviceId;
	private String credential;
	private int aliasType;
	private int type;
	private String symmetricKey;
    private String symmetricKeyIV;

	public LoginRequest(String aliasId, String deviceId, String credential, String symmetricKey, String symmetricKeyIV){
		this.sat = 5;
		this.aliasType = 2;
		this.type = 2;
		this.applicationId = "WCOBMOBAPP";

		this.aliasId = aliasId;
		this.deviceId = deviceId;
		this.credential = credential;
		this.symmetricKey = symmetricKey;
		this.symmetricKeyIV = symmetricKeyIV;
	}

	public final String getUrlEncodedFormData() throws UnsupportedEncodingException {

		final String utfEncodingType = StandardCharsets.UTF_8.name();
		StringBuilder sb = new StringBuilder()
				.append("SAT=").append(this.sat)
				.append("&ApplicationId=").append(this.applicationId)
				.append("&aliasId=").append(URLEncoder.encode(this.aliasId, utfEncodingType))
				.append("&deviceid=").append(this.deviceId)
				.append("&credential=").append(URLEncoder.encode(this.credential, utfEncodingType))
				.append("&aliasType=").append(this.aliasType)
				.append("&type=").append(this.type)
				//TODO
                //.append("&symmetricKeyIV=").append(this.symmetricKeyIV)
				.append("&symmetrickey=").append(URLEncoder.encode(this.symmetricKey, utfEncodingType));

		return sb.toString();
	}
}
