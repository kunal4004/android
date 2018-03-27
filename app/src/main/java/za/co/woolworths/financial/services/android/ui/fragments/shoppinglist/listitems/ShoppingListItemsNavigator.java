package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;

/**
 * Created by W7099877 on 2018/03/08.
 */

public interface ShoppingListItemsNavigator {

	void onShoppingListItemsResponse(ShoppingListItemsResponse shoppingListItemsResponse);

	void onItemSelectionChange(List<ShoppingListItem> items);

	void onShoppingListItemDelete(ShoppingListItemsResponse shoppingListItemsResponse);

	void onItemDeleteClick(String id, String productId, String catalogRefId);

	void onShoppingSearchClick();

	void onAddToCartPreExecute();

	void onAddToCartSuccess(AddItemToCartResponse addItemToCartResponse);

	void onSessionTokenExpired(Response response);

	void otherHttpCode(Response response);

	void onAddItemToCartFailure(String errorMessage);

	void onQuantityChangeClick(int position);
}
