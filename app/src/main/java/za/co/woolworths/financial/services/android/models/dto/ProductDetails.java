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
	@SerializedName("nutritionalInformation")
	@Expose
	public List<Object> nutritionalInformation = null;
	@SerializedName("longDescription")
	@Expose
	public String longDescription;
	@SerializedName("otherSkus")
	@Expose
	public ArrayList<OtherSkus> otherSkus = null;
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
	@SerializedName("externalImageRef")
	@Expose
	public String externalImageRef;
	@SerializedName("fulfillmentType")
	@Expose
	public String fulfillmentType;
	@SerializedName("ingredients")
	@Expose
	public String ingredients;
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

}
