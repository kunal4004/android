package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/23.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfferResponse {

    @SerializedName("code")
    @Expose
    public String code;
    @SerializedName("desc")
    @Expose
    public String desc;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("stsParams")
    @Expose
    public String stsParams;

}