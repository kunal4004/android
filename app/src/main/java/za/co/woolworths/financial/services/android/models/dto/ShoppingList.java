package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

public class ShoppingList {

	@SerializedName("id")
	public String listId;
	@SerializedName("name")
	public String listName;
	@SerializedName("itemCount")
	public String listCount;

	public boolean viewIsSelected;
}
