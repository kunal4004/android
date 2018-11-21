package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Data;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.Utils;

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
		if (shoppingCartResponse.data != null
				&& shoppingCartResponse.data[0] != null) {
			Data cartData = shoppingCartResponse.data[0];
			saveDeliveryLocation(cartData);
			//Update cart count even if CartActivity close down
			if (cartData.orderSummary != null)
				QueryBadgeCounter.getInstance().setCartCount(cartData.orderSummary.totalItemsCount, INDEX_CART);
		}
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(shoppingCartResponse);
			}
		}
	}

	private void saveDeliveryLocation(Data data) {
		// set delivery location
		if (!TextUtils.isEmpty(data.suburbName) && !TextUtils.isEmpty(data.provinceName)) {
			String suburbId = String.valueOf(data.suburbId);
			Province province = new Province();
			province.name = data.provinceName;
			province.id = suburbId;
			Suburb suburb = new Suburb();
			suburb.name = data.suburbName;
			suburb.id = suburbId;
			suburb.fulfillmentStores = data.orderSummary.suburb.fulfillmentStores;
			Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(province, suburb));
		}
	}
}
