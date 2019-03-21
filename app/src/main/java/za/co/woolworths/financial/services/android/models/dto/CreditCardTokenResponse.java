package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CreditCardTokenResponse {
    @SerializedName("cards")
    @Expose
    public ArrayList<Card> cards = null;
    @SerializedName("response")
    @Expose
    public Response response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;
}
