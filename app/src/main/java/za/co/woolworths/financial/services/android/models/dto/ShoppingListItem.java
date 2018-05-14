package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Created by W7099877 on 2018/03/13.
 */

public class ShoppingListItem {
	public String description;

	public String internalImageURL;

	public String externalImageURL;

	public String Id;

	public int quantityDesired;

	public String displayName;

	public String catalogRefId;

	public String productURL;

	public String size;

	public String productId;

	public boolean isSelected = false;

	public double price;

	public int userQuantity = 0;

	@SerializedName("colourDescription")
	public String color;
}
