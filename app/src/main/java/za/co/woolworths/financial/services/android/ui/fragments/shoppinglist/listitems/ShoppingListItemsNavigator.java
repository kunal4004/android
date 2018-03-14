package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;

/**
 * Created by W7099877 on 2018/03/08.
 */

public interface ShoppingListItemsNavigator {

	void onShoppingListItemsResponse(ShoppingListItemsResponse shoppingListItemsResponse);
	void onItemSelectionChange(boolean addToCartButtonAvailableStatus);
}
