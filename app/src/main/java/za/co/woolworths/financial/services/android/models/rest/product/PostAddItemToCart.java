package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;

public class PostAddItemToCart extends HttpAsyncTask<String, String, AddItemToCartResponse> {

	private OnEventListener<AddItemToCartResponse> mCallBack;
	private String mException;
	private List<AddItemToCart> addItemToCart;

	public PostAddItemToCart(List<AddItemToCart> addItemToCart, OnEventListener callback) {
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

		// Ensure counter is always updated after a successful add to cart
		switch (addItemToCartResponse.httpCode) {
			case 200:
				QueryBadgeCounter.getInstance().queryCartSummaryCount();
				break;
			default:
				break;
		}
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(addItemToCartResponse);
			}
		}
	}
}
