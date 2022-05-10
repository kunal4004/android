package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherSkus {

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
	@SerializedName("displayName")
	@Expose
	public String displayName;
	@SerializedName("size")
	@Expose
	public String size;
	@SerializedName("colour")
	@Expose
	public String colour;
	@SerializedName("imagePath")
	@Expose
	public String imagePath;
	@SerializedName("externalImageRefV2")
	@Expose
	public String externalImageRefV2;

	@SerializedName("wasPrice")
	@Expose
	public String wasPrice;
	public int quantity = -1;
	@SerializedName("kilogramPrice")
	@Expose
	public String kilogramPrice;

	@SerializedName("styleIdOnSale")
	@Expose
	public Boolean styleIdOnSale;
}