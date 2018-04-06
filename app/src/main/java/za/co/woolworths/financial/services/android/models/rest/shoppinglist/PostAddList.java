package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class PostAddList extends HttpAsyncTask<String, String, ShoppingListsResponse> {
	private CreateList listName;
	private OnEventListener mCallBack;
	private String mException;

	public PostAddList(OnEventListener mCallBack, CreateList listName) {
		this.mCallBack = mCallBack;
		this.listName = listName;
	}

	@Override
	protected ShoppingListsResponse httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().createList(listName);
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
	protected void onPostExecute(ShoppingListsResponse ShoppingListsResponse) {
		super.onPostExecute(ShoppingListsResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(ShoppingListsResponse);
			}
		}
	}
}
