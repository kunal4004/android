package za.co.woolworths.financial.services.android.ui.views.dialog;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;

public interface AddToListInterface {
	void onItemClick(ShoppingList shoppingList, boolean activate);
}
