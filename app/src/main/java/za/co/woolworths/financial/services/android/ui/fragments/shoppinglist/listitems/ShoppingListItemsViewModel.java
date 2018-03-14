package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingListItems;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

/**
 * Created by W7099877 on 2018/03/08.
 */

public class ShoppingListItemsViewModel extends BaseViewModel<ShoppingListItemsNavigator> {
	public ShoppingListItemsViewModel() {
		super();
	}

	public ShoppingListItemsViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public GetShoppingListItems getShoppingListItems(String listId)
	{
		return new GetShoppingListItems(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse shoppingListItemsResponse = (ShoppingListItemsResponse) object;
				getNavigator().onShoppingListItemsResponse(shoppingListItemsResponse);
			}

			@Override
			public void onFailure(String e) {

			}
		},listId);
	}

}
