package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/24.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfferActive {

    @SerializedName("offerActive")
    @Expose
    public Boolean offerActive;
    @SerializedName("response")
    @Expose
    public OfferActiveResponse response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;

}
