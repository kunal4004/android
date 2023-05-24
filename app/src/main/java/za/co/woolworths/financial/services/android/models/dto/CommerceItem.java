package za.co.woolworths.financial.services.android.models.dto;

import java.io.Serializable;

import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.SubstitutionInfo;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class CommerceItem implements Serializable{

	public PriceInfo priceInfo;
	public CommerceItemInfo commerceItemInfo;
	//local values to show ProgressBar on load
	private String size;
	private String color;
	private boolean quantityUploading = false;
	private boolean deleteSingleItem = false;
	private boolean deleteRowPressed = false;
	private CommerceItem deletedCommerceItemId;
	public boolean isGWP;
	public String fulfillmentType;
	public String fulfillmentStoreId;
	public int quantityInStock;
	public boolean isStockChecked = false;
	public boolean isItemRemoved = false;
	public String commerceItemClassType;
	private boolean deletePressed = false;
	public int lowStockThreshold;

	public SubstitutionInfo substitutionInfo;

	public void setDeleteSingleItem(boolean deleteSingleItem) {
		this.deleteSingleItem = deleteSingleItem;
	}

	public boolean deleteSingleItem() {
		return deleteSingleItem;
	}

	public void setDeleteIconWasPressed(boolean deleteRowPressed) {
		this.deleteRowPressed = deleteRowPressed;
	}

	public boolean deleteIconWasPressed() {
		return deleteRowPressed;
	}

	public void commerceItemDeletedId(CommerceItem commerceItem) {
		this.deletedCommerceItemId = commerceItem;
	}

	public CommerceItem getDeletedCommerceItemId() {
		return deletedCommerceItemId;
	}

	public void setQuantityUploading(boolean upload) {
		this.quantityUploading = upload;
	}

	public boolean getQuantityUploading() {
		return quantityUploading;
	}

	public PriceInfo getPriceInfo() {
		return priceInfo;
	}

	public void setPriceInfo(PriceInfo priceInfo) {
		this.priceInfo = priceInfo;
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

	public boolean isDeletePressed() {
		return deletePressed;
	}

	public void setDeletePressed(boolean deletePressed) {
		this.deletePressed = deletePressed;
	}
}

