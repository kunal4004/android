package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class PostAddItemToCart extends HttpAsyncTask<String, String, AddItemToCartResponse> {

	private OnEventListener<AddItemToCartResponse> mCallBack;
	private String mException;
	private AddItemToCart addItemToCart;

	public PostAddItemToCart(AddItemToCart addItemToCart, OnEventListener callback) {
		this.addItemToCart = addItemToCart;
		this.mCallBack = callback;
	}

	@Override
	protected AddItemToCartResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().addItemToCart(addItemToCart);
	}

	@Override
	protected AddItemToCartResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new AddItemToCartResponse();
	}

	@Override
	protected Class<AddItemToCartResponse> httpDoInBackgroundReturnType() {
		return AddItemToCartResponse.class;
	}

	@Override
	protected void onPostExecute(AddItemToCartResponse addItemToCartResponse) {
		super.onPostExecute(addItemToCartResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(addItemToCartResponse);
			}
		}
	}
}
