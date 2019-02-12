package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class ValidateCardAndPinResponse {

	@SerializedName("header")
	private Header header;

	public Header getHeader() {
		return header;
	}
}
