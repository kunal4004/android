package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Product_ {

    @SerializedName("productId")
    @Expose
    public String productId;
    @SerializedName("productName")
    @Expose
    public String productName;
    @SerializedName("externalImageRef")
    @Expose
    public String externalImageRef;
    @SerializedName("imagePath")
    @Expose
    public String imagePath;
    @SerializedName("fromPrice")
    @Expose
    public Integer fromPrice;
    @SerializedName("sku")
    @Expose
    public String sku;
    @SerializedName("productType")
    @Expose
    public String productType;
    @SerializedName("promotionImages")
    @Expose
    public PromotionImages promotionImages;
    @SerializedName("otherSkus")
    @Expose
    public List<OtherSku> otherSkus = null;
    @SerializedName("brandImage")
    @Expose
    public String brandImage;

}
