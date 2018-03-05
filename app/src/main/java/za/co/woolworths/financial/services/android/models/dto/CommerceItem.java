package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class CommerceItem {
	public int quantity;
	public String productId;
	public String internalImageURL;
	public String externalImageURL;
	public String catalogRefId;
	public String productDisplayName;
	public PriceInfo priceInfo;
	@SerializedName("id")
	public String commerceId;

	//local values to show ProgressBar on load
	private boolean quantityUploading = false;
	private boolean deleteSingleItem = false;

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

	public String getExternalImageURL() {
		return externalImageURL;
	}

	public void setExternalImageURL(String externalImageURL) {
		this.externalImageURL = externalImageURL;
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

	public PriceInfo getPriceInfo() {
		return priceInfo;
	}

	public void setPriceInfo(PriceInfo priceInfo) {
		this.priceInfo = priceInfo;
	}

	public String getCommerceId() {
		return commerceId;
	}

	public void setCommerceId(String commerceId) {
		this.commerceId = commerceId;
	}

	public void setQuantityUploading(boolean upload) {
		this.quantityUploading = upload;
	}

	public boolean getQuantityUploading() {
		return quantityUploading;
	}

	public void setDeleteSingleItem(boolean deleteSingleItem) {
		this.deleteSingleItem = deleteSingleItem;
	}

	public boolean deleteSingleItem() {
		return deleteSingleItem;
	}
}
