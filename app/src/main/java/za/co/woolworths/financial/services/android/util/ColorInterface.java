package za.co.woolworths.financial.services.android.util;


import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;

public interface ColorInterface {
	void onUpdate(ArrayList<OtherSkus> otherSkuList, String viewType, boolean shouldShowPrice);

	void onUpdate(List<Integer> quantityList);

	void onUpdate(ShoppingListItem shoppingListItem);

}
