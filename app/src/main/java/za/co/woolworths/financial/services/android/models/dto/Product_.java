package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Product_ {

    @SerializedName("auxiliaryImages")
    @Expose
    public AuxiliaryImages auxiliaryImages;
    @SerializedName("brandImage")
    @Expose
    public String brandImage;
    @SerializedName("categoryName")
    @Expose
    public String categoryName;
    @SerializedName("checkOutLink")
    @Expose
    public String checkOutLink;
    @SerializedName("externalImageRef")
    @Expose
    public String externalImageRef;
    @SerializedName("fromPrice")
    @Expose
    public Integer fromPrice;
    @SerializedName("imagePath")
    @Expose
    public String imagePath;
    @SerializedName("longDescription")
    @Expose
    public String longDescription;
    @SerializedName("otherSkus")
    @Expose
    public List<OtherSku> otherSkus = null;
    @SerializedName("productId")
    @Expose
    public String productId;
    @SerializedName("productName")
    @Expose
    public String productName;
    @SerializedName("productType")
    @Expose
    public String productType;
    @SerializedName("promotionImages")
    @Expose
    public PromotionImages promotionImages;
    @SerializedName("saveText")
    @Expose
    public String saveText;
    @SerializedName("sku")
    @Expose
    public String sku;

}