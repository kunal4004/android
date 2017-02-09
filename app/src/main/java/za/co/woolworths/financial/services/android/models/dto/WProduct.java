package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WProduct {

    @SerializedName("product")
    @Expose
    public WProductDetail product;
    @SerializedName("response")
    @Expose
    public Response response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;

}