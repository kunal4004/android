package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public class CEKDResponse {

    @SerializedName("header")
    private Header header;
    @SerializedName("keyId")
    public String keyId;

}
