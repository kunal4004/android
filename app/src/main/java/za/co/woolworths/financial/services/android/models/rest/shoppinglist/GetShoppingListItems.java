package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2018/03/13.
 */

public class GetShoppingListItems extends HttpAsyncTask<String,String,ShoppingListItemsResponse> {
	private OnEventListener mCallBack;
	private String mException;
	private String listId;
	public GetShoppingListItems(OnEventListener mCallBack,String listId){
		this.mCallBack=mCallBack;
		this.listId=listId;
	}
	@Override
	protected ShoppingListItemsResponse httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().getShoppingListItems(listId);
	}

	@Override
	protected ShoppingListItemsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new ShoppingListItemsResponse();
	}

	@Override
	protected Class<ShoppingListItemsResponse> httpDoInBackgroundReturnType() {
		return ShoppingListItemsResponse.class;
	}

	@Override
	protected void onPostExecute(ShoppingListItemsResponse shoppingListItemsResponse) {
		super.onPostExecute(shoppingListItemsResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(shoppingListItemsResponse);
			}
		}
	}
}
