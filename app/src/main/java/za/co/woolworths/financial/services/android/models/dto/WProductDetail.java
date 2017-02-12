package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WProductDetail {

    public String productId;
    public String productName;
    public String externalImageRef;
    public String categoryName;
    public String categoryId;
    public String imagePath;
    public Float fromPrice;
    public String sku;
    public String saveText;
    public String checkOutLink;
    //public AuxiliaryImages auxiliaryImages;
    public String productType;
    // public PromotionImages promotionImages;
    public String longDescription;
    //@SerializedName("otherSkus")
    @SerializedName("otherSkus")
    @Expose
    public List<OtherSku> otherSkus = null;
    @SerializedName("promotionImages")
    @Expose
    public PromotionImages promotionImages;

}
