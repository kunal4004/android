package za.co.woolworths.financial.services.android.models.rest.shoppinglist;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2018/03/16.
 */

public class DeleteShoppingListItem extends HttpAsyncTask<String,String,ShoppingListItemsResponse> {
	private OnEventListener mCallBack;
	private String mException;
	private String listId;
	private String id;
	private String productId;
	private String catalogRefId;
	public DeleteShoppingListItem(OnEventListener mCallBack,String listId, String id, String productId, String catalogRefId){
		this.mCallBack=mCallBack;
		this.listId=listId;
		this.id=id;
		this.productId=productId;
		this.catalogRefId=catalogRefId;

	}
	@Override
	protected ShoppingListItemsResponse httpDoInBackground(String... strings) {
		return WoolworthsApplication.getInstance().getApi().deleteShoppingListItem(listId,id,productId,catalogRefId);
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
