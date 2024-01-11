package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

/**
 * Created by W7099877 on 2018/03/13.
 */

public class ShoppingListItem {
	public String description;

	public String internalImageURL;

	public String externalImageRefV2;

	public String Id;

	public int quantityDesired;

	public String displayName;

	public String catalogRefId;

	public String productURL;

	public String size;

	public String productId;

	public boolean isSelected = false;

	public String price;

	public int userQuantity = 0;

	@SerializedName("colourDescription")
	public String color;

	public String fulfillmentType;

	@SerializedName("availability")
	public String availability;

	//quantityInStock amount value is set from Inventory stock call
	public int quantityInStock = -1;

	//Grey out the quantity counter so it cannot be clicked until inventory call done
	public boolean inventoryCallCompleted = false;

	@Nullable
	public String visibility;
	public boolean isAddToCartInProgress = false;
}
