package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Card implements Serializable {
    @SerializedName("productCategory")
    @Expose
    public String productCategory;
    @SerializedName("productStatus")
    @Expose
    public String productStatus;
    @SerializedName("cardStatus")
    @Expose
    public String cardStatus;
    @SerializedName("absaCardToken")
    @Expose
    public String absaCardToken;
    @SerializedName("absaAccountToken")
    @Expose
    public String absaAccountToken;
}
