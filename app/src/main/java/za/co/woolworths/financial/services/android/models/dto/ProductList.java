package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductList {


    @SerializedName("productId")
    @Expose
    public String productId;
    @SerializedName("brandImage")
    @Expose
    public String brandImage;
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
    public Float fromPrice;
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
    public List<OtherSkus> otherSkus = null;

}