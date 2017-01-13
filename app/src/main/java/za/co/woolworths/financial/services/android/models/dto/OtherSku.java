package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherSku {

    @SerializedName("sku")
    @Expose
    public String sku;
    @SerializedName("externalColourRef")
    @Expose
    public String externalColourRef;
    @SerializedName("colourImagePath")
    @Expose
    public String colourImagePath;
    @SerializedName("price")
    @Expose
    public String price;
    @SerializedName("wasPrice")
    @Expose
    public String wasPrice;

}