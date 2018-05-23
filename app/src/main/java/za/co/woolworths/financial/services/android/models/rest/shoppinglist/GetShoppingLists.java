package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2018/03/13.
 */

public class GetShoppingLists extends HttpAsyncTask<String, String, ShoppingListsResponse> {
	public OnEventListener mCallBack;
	private String mException;

	public GetShoppingLists(OnEventListener mCallBack) {
		this.mCallBack = mCallBack;
	}

	@Override
	protected ShoppingListsResponse httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().getShoppingLists();
	}

	@Override
	protected ShoppingListsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new ShoppingListsResponse();
	}

	@Override
	protected Class<ShoppingListsResponse> httpDoInBackgroundReturnType() {
		return ShoppingListsResponse.class;
	}

	@Override
	protected void onPostExecute(ShoppingListsResponse shoppingListsResponse) {
		super.onPostExecute(shoppingListsResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(shoppingListsResponse);
			}
		}
	}
}
