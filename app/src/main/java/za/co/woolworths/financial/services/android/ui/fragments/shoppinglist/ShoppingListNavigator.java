package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;

/**
 * Created by W7099877 on 2018/03/07.
 */

public interface ShoppingListNavigator {
	void onListItemSelected(String listName,String listID);

	void onClickItemDelete(String listID);

	void onDeleteShoppingList(ShoppingListsResponse shoppingListsResponse);

}
