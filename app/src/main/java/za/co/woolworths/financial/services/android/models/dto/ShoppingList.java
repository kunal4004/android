package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

public class ShoppingList {

    @SerializedName("id")
    public String listId;
    @SerializedName("name")
    public String listName;
    @SerializedName("itemCount")
    public int listCount;

    public boolean shoppingListRowWasSelected;
    public boolean wasSentToServer;
}
