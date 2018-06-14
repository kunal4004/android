package za.co.woolworths.financial.services.android.models.rest.product;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

public class GetCartSummary extends HttpAsyncTask<String, String, CartSummaryResponse> {

	private Context mContext;
	private OnEventListener mCallBack;
	private String mException;

	public GetCartSummary(Context context, OnEventListener callback) {
		this.mCallBack = callback;
		this.mContext = context;
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
				/***
				 * Cache suburb response
				 */
				cacheSuburbFromCartSummary(cartSummaryResponse);
			}
		}
	}

	private void cacheSuburbFromCartSummary(CartSummaryResponse cartSummaryResponse) {
		if (cartSummaryResponse != null) {
			List<CartSummary> cartSummary = cartSummaryResponse.data;
			if (cartSummary != null) {
				ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
				CartSummary cart = cartSummary.get(0);
				if (shoppingDeliveryLocation == null) {
					Province province = getProvince(cart);
					Suburb suburb = getSuburb(cart);
					Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(province, suburb));
					return;
				}
				Province province = getProvince(cart);
				if (shoppingDeliveryLocation.suburb.id.equalsIgnoreCase(String.valueOf(cart.suburbId)))
					return;
				Suburb cartSuburb = getSuburb(cart);
				Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(province, cartSuburb));
			}
		}
	}

	@NonNull
	private Province getProvince(CartSummary cart) {
		Province province = new Province();
		province.name = cart.provinceName;
		return province;
	}

	@NonNull
	private Suburb getSuburb(CartSummary cart) {
		Suburb suburb = new Suburb();
		suburb.id = String.valueOf(cart.suburbId);
		suburb.name = cart.suburbName;
		suburb.fulfillmentStores = cart.suburb.fulfillmentStores;
		return suburb;
	}
}
