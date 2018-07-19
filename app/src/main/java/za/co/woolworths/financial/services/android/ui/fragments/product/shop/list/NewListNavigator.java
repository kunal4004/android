package za.co.woolworths.financial.services.android.ui.fragments.product.shop.list;


import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;

public interface NewListNavigator {
	void onFailure(String e);

	void onShoppingListSuccessResponse(ShoppingListsResponse shoppingListsResponse);

	void onShoppingListFailureResponse(Response response);
}
