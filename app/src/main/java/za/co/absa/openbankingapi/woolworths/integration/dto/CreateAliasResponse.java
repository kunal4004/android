package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class CreateAliasResponse {

	@SerializedName("header")
	private Header header;

	@SerializedName("aliasId")
	private String aliasId;

	public Header getHeader() {
		return header;
	}

	public String getAliasId() {
		return aliasId;
	}

	public void setAliasId(String aliasId) {
		this.aliasId = aliasId;
	}
}
