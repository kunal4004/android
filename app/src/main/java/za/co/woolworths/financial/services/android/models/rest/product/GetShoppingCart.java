package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART;

public class GetShoppingCart extends HttpAsyncTask<String, String, ShoppingCartResponse> {

	private OnEventListener mCallBack;
	private String mException;

	public GetShoppingCart(OnEventListener callback) {
		this.mCallBack = callback;
	}

	@Override
	protected ShoppingCartResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getShoppingCart();
	}

	@Override
	protected ShoppingCartResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new ShoppingCartResponse();
	}

	@Override
	protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
		return ShoppingCartResponse.class;
	}

	@Override
	protected void onPostExecute(ShoppingCartResponse shoppingCartResponse) {
		super.onPostExecute(shoppingCartResponse);
		/***
		 * Update cart count even if CartActivity close down
		 */
		if (shoppingCartResponse != null)
			if (shoppingCartResponse.data != null)
				if (shoppingCartResponse.data[0] != null)
					if (shoppingCartResponse.data[0].orderSummary != null)
						QueryBadgeCounter.getInstance().setCartCount(shoppingCartResponse.data[0].orderSummary.totalItemsCount, INDEX_CART);

		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(shoppingCartResponse);
			}
		}
	}

}
