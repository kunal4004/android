package za.co.woolworths.financial.services.android.models.rest.shop;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetCartSummary extends HttpAsyncTask<String, String, CartSummaryResponse> {

	private OnEventListener mCallBack;
	private String mException;
	private String locationId;

	public GetCartSummary(OnEventListener callback) {
		this.locationId = locationId;
		mCallBack = callback;
	}

	@Override
	protected CartSummaryResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getCartSummary();
	}

	@Override
	protected CartSummaryResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new CartSummaryResponse();
	}

	@Override
	protected Class<CartSummaryResponse> httpDoInBackgroundReturnType() {
		return CartSummaryResponse.class;
	}

	@Override
	protected void onPostExecute(CartSummaryResponse cartSummaryResponse) {
		super.onPostExecute(cartSummaryResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(cartSummaryResponse);
			}
		}
	}
}
