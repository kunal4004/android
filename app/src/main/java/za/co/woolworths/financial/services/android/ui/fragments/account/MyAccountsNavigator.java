package za.co.woolworths.financial.services.android.ui.fragments.account;

import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;

public interface MyAccountsNavigator {
	void onMessageResponse(MessageResponse messageResponse);
	void onShoppingListsResponse(ShoppingListsResponse shoppingListsResponse);
}
