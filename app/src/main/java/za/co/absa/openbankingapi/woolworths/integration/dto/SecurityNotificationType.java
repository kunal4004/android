package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

public enum SecurityNotificationType {
    @SerializedName("OTP")
    OTP,
    @SerializedName("SureCheck")
    SureCheck;
}
