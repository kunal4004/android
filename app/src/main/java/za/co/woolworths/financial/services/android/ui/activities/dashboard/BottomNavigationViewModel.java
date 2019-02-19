package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class BottomNavigationViewModel extends BaseViewModel<BottomNavigator> {

	public BottomNavigationViewModel() {
		super();
	}

	public BottomNavigationViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void openShoppingListOnToastClick(List<ShoppingList> shoppingList, BottomNavigator navigator) {
		List<ShoppingList> newList = new ArrayList<>();
		if (shoppingList != null) {
			for (ShoppingList shopList : shoppingList) {
				if (shopList.shoppingListRowWasSelected) {
					newList.add(shopList);
				}
			}
		}
		int shoppingListSize = newList.size();
		if (shoppingListSize == 1) {
			ShoppingList shop = newList.get(0);
			Bundle bundle = new Bundle();
			bundle.putString("listId", shop.listId);
			bundle.putString("listName", shop.listName);
			ShoppingListItemsFragment shoppingListItemsFragment = new ShoppingListItemsFragment();
			shoppingListItemsFragment.setArguments(bundle);
			navigator.pushFragmentSlideUp(shoppingListItemsFragment);
		} else if (shoppingListSize > 1) {
			Bundle bundle = new Bundle();
			ShoppingListsResponse shoppingListsResponse = new ShoppingListsResponse();
			bundle.putString("ShoppingList", Utils.objectToJson(shoppingListsResponse));
			ShoppingListFragment shoppingListFragment = new ShoppingListFragment();
			shoppingListFragment.setArguments(bundle);
			navigator.pushFragmentSlideUp(shoppingListFragment);
		}
	}
}
