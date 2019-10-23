package za.co.woolworths.financial.services.android.ui.fragments.account;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse;

public interface MyAccountsNavigator {
	void onShoppingListsResponse(ShoppingListsResponse shoppingListsResponse);

	void onMessageResponse(int unreadCount);

	void onGetStoreCardsResponse(StoreCardsResponse storeCardsResponse);
}
