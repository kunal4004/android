package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class CEKDRequest {

    @SerializedName("header")
    private Header header;

    @SerializedName("contentEncryptionSeed")
    private String contentEncryptionSeed;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("applicationId")
    private String applicationId;

    public CEKDRequest(String deviceId, String contentEncryptionSeed) {
        this.header = new Header();
        this.contentEncryptionSeed = contentEncryptionSeed;
        this.deviceId = deviceId;
        this.applicationId = "WCOBMOBAPP";
    }

    public final String getJson() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
