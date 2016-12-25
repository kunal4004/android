package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/23.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateOfferResponse {

    @SerializedName("response")
    @Expose
    public OfferResponse response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;

}
