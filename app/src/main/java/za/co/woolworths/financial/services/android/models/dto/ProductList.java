package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType;

public class ProductList {

    @SerializedName("productId")
    @Expose
    public String productId;
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
    @SerializedName("promotions")
    @Expose
    public ArrayList<Promotions> promotionsList = null;
    @SerializedName("saveText")
    @Expose
    public String saveText;
    @SerializedName("priceType")
    @Expose
    public String priceType;
    @SerializedName("kilogramPrice")
    @Expose
    public Float kilogramPrice;
    @SerializedName("price")
    @Expose
    public Float price;
    @SerializedName("wasPrice")
    @Expose
    public Float wasPrice;
    @SerializedName("brandText")
    @Expose
    public String brandText;
    @SerializedName("productVariants")
    @Expose
    public String productVariants;

    public ProductListingViewType rowType = ProductListingViewType.PRODUCT;
    public Integer numberOfItems;
    public boolean itemWasChecked;
    public boolean viewIsLoading;
    public String displayColorSizeText;
    public String fulfillmentType;
    public boolean quickShopButtonWasTapped = false;
}