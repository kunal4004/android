package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by W7099877 on 2018/03/13.
 */

public class ShoppingListsResponse {
	public int httpCode;
	public Response response;
	@SerializedName("sharedLists")
	public List<ShoppingList> sharedLists;
	@SerializedName("data")
	public List<ShoppingList> lists;
}
