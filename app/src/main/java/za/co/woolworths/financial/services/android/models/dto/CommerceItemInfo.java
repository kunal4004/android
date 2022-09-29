package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by W7099877 on 2018/03/19.
 */

public class CommerceItemInfo implements Serializable {
	public int quantity;
	public String productId;
	public String internalImageURL;
	public String externalImageRefV2;
	public String catalogRefId;
	public String productDisplayName;
	public boolean isGWP;
	@SerializedName("id")
	public String commerceId;
	private String size;
	private String color;


	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getInternalImageURL() {
		return internalImageURL;
	}

	public void setInternalImageURL(String internalImageURL) {
		this.internalImageURL = internalImageURL;
	}

	public String getExternalImageRefV2() {
		return externalImageRefV2;
	}

	public void setExternalImageRefV2(String externalImageRefV2) {
		this.externalImageRefV2 = externalImageRefV2;
	}

	public String getCatalogRefId() {
		return catalogRefId;
	}

	public void setCatalogRefId(String catalogRefId) {
		this.catalogRefId = catalogRefId;
	}

	public String getProductDisplayName() {
		return productDisplayName;
	}

	public void setProductDisplayName(String productDisplayName) {
		this.productDisplayName = productDisplayName;
	}

	public String getCommerceId() {
		return commerceId;
	}

	public void setCommerceId(String commerceId) {
		this.commerceId = commerceId;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
