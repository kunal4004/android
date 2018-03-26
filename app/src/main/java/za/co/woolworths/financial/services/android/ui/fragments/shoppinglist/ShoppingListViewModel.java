package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

/**
 * Created by W7099877 on 2018/03/07.
 */

public class ShoppingListViewModel extends BaseViewModel<ShoppingListNavigator> {
	public ShoppingListViewModel() {
		super();
	}

	public ShoppingListViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public DeleteShoppingList deleteShoppingList(String listId)
	{
		return new DeleteShoppingList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListsResponse shoppingListsResponse = (ShoppingListsResponse) object;
				getNavigator().onDeleteShoppingList(shoppingListsResponse);
			}

			@Override
			public void onFailure(String e) {

			}
		},listId);
	}
}
