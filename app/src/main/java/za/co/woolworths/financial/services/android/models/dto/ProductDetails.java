package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductDetails {

	@SerializedName("productId")
	@Expose
	public String productId;
	@SerializedName("productName")
	@Expose
	public String productName;
	@SerializedName("range")
	@Expose
	public String range;
	@SerializedName("categoryName")
	@Expose
	public String categoryName;
	@SerializedName("categoryId")
	@Expose
	public String categoryId;
	@SerializedName("isnAvailable")
	@Expose
	public String isnAvailable;
	@SerializedName("auxiliaryImages")
	@Expose
	public JsonElement auxiliaryImages;
	@SerializedName("promotionImages")
	@Expose
	public PromotionImages promotionImages;
	@SerializedName("longDescription")
	@Expose
	public String longDescription;
	@SerializedName("otherSkus")
	@Expose
	public ArrayList<OtherSkus> otherSkus = null;
	@SerializedName("promotions")
	@Expose
	public ArrayList<Promotions> promotionsList = null;
	@SerializedName("checkOutLink")
	@Expose
	public String checkOutLink;
	@SerializedName("productType")
	@Expose
	public String productType;
	@SerializedName("imagePath")
	@Expose
	public String imagePath;
	@SerializedName("fromPrice")
	@Expose
	public Float fromPrice;
	@SerializedName("sku")
	@Expose
	public String sku;
	@SerializedName("externalImageRefV2")
	@Expose
	public String externalImageRefV2;
	@SerializedName("fulfillmentType")
	@Expose
	public String fulfillmentType;
	@SerializedName("ingredients")
	@Expose
	public String ingredients;
	@Expose
	@SerializedName("dietary")
	public List<String> dietary;
	@Expose
	@SerializedName("allergens")
	public List<String> allergens;
	@Expose
	@SerializedName("categories")
	public List<String> categories;
	@SerializedName("saveText")
	@Expose
	public String saveText;
	@SerializedName("price")
	@Expose
	public String price;
	@SerializedName("priceType")
	@Expose
	public String priceType;
	@SerializedName("wasPrice")
	@Expose
	public String wasPrice;
	@SerializedName("kilogramPrice")
	@Expose
	public String kilogramPrice;
	@SerializedName("nutritionalInformationDetails")
	@Expose
	public NutritionalInformationDetails nutritionalInformationDetails;
	@SerializedName("freeGiftText")
	@Expose
	public String freeGiftText;
	@SerializedName("freeGift")
	@Expose
	public String freeGift;
	@SerializedName("brandText")
	@Expose
	public String brandText;
	@SerializedName("sizeGuideId")
	@Expose
	public String sizeGuideId;
	@SerializedName("colourSizeVariants")
	@Expose
	public String colourSizeVariants;
	@SerializedName("isLiquor")
	public boolean isLiquor;
	@SerializedName("virtualTryOn")
	@Expose
	public String virtualTryOn;

	@SerializedName("isRnREnabled")
	@Expose
	public Boolean isRnREnabled = null;
	@SerializedName("averageRating")
	@Expose
	public float averageRating;
	@SerializedName("reviewCount")
	@Expose
	public int reviewCount;
	@SerializedName("lowStockThreshold")
	@Expose
	public Integer lowStockIndicator;

	@SerializedName("network")
	@Expose
	public String network;

}
