package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherSkus {

    @SerializedName("sku")
    @Expose
    public String sku;
    @SerializedName("price")
    @Expose
    public String price;

}