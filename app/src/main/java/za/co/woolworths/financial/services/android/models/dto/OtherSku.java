package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherSku {

    @SerializedName("colour")
    @Expose
    public String colour;
    @SerializedName("colourImagePath")
    @Expose
    public String colourImagePath;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("externalColourRef")
    @Expose
    public String externalColourRef;
    @SerializedName("externalImageRef")
    @Expose
    public String externalImageRef;
    @SerializedName("imagePath")
    @Expose
    public String imagePath;
    @SerializedName("price")
    @Expose
    public String price;
    @SerializedName("size")
    @Expose
    public String size;
    @SerializedName("sku")
    @Expose
    public String sku;
    @SerializedName("wasPrice")
    @Expose
    public String wasPrice;

}