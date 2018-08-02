package za.co.woolworths.financial.services.android.ui.fragments.product.shop.list;

import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddList;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class NewListViewModel extends BaseViewModel<NewListNavigator> {
	public NewListViewModel() {
		super();
	}

	public NewListViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public PostAddList postCreateList(CreateList listName) {
		return new PostAddList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListsResponse shoppingListsResponse = (ShoppingListsResponse) object;
				int httpCode = shoppingListsResponse.httpCode;
				switch (httpCode) {
					case 200:
						getNavigator().onShoppingListSuccessResponse(shoppingListsResponse);
						break;
					default:
						if (shoppingListsResponse.response != null) {
							Response response = shoppingListsResponse.response;
							if (response.desc != null) {
								getNavigator().onShoppingListFailureResponse(response);
							}
						}
						break;
				}
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onFailure(e);

			}
		}, listName);
	}
}
