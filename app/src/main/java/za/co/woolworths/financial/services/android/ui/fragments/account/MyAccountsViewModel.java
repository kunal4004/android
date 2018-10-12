package za.co.woolworths.financial.services.android.ui.fragments.account;

import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.message.GetMessage;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingLists;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class MyAccountsViewModel extends BaseViewModel<MyAccountsNavigator> {

	public MyAccountsViewModel() {
		super();
	}

	public MyAccountsViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}


	public GetMessage loadMessageCount() {
		return new GetMessage(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				MessageResponse messageResponse = (MessageResponse) object;
				getNavigator().onMessageResponse(messageResponse.unreadCount);
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
	}

	public GetShoppingLists getShoppingListsResponse() {
		return new GetShoppingLists(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListsResponse shoppingListsResponse = (ShoppingListsResponse) object;
				getNavigator().onShoppingListsResponse(shoppingListsResponse);
			}

			@Override
			public void onFailure(String e) {

			}
		});
	}
}
