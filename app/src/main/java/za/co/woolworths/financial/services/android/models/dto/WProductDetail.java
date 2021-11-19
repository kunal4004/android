package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WProductDetail {
	public String productId;
	public String productName;
	public String externalImageRefV2;
	public String categoryName;
	public String categoryId;
	public boolean isnAvailable;
	public String imagePath;
	public Float fromPrice;
	public String sku;
	public String fulfillmentType;
	public String saveText;
	public String checkOutLink;
	//public AuxiliaryImages auxiliaryImages;
	public String productType;
	// public PromotionImages promotionImages;
	public String longDescription;
	//@SerializedName("otherSkus")
	@SerializedName("otherSkus")
	@Expose
	public List<OtherSkus> otherSkus = null;
	@SerializedName("promotionImages")
	@Expose
	public PromotionImages promotionImages;
}
