package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetInventorySkusForStore extends HttpAsyncTask<String, String, SkusInventoryForStoreResponse> {

	private WoolworthsApplication mWoolworthsApplication;
	private OnEventListener mCallBack;
	private String mException, mMultipleSku, mStoreId;

	public GetInventorySkusForStore(String storeId, String multipleSKU, OnEventListener callback) {
		this.mMultipleSku = multipleSKU;
		this.mCallBack = callback;
		this.mStoreId = storeId;
		this.mWoolworthsApplication = WoolworthsApplication.getInstance();
	}

	@Override
	protected SkusInventoryForStoreResponse httpDoInBackground(String... params) {
		return mWoolworthsApplication.getApi().getInventorySkuForStore(mStoreId, mMultipleSku);
	}

	@Override
	protected SkusInventoryForStoreResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new SkusInventoryForStoreResponse();
	}

	@Override
	protected Class<SkusInventoryForStoreResponse> httpDoInBackgroundReturnType() {
		return SkusInventoryForStoreResponse.class;
	}

	@Override
	protected void onPostExecute(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
		super.onPostExecute(skusInventoryForStoreResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(skusInventoryForStoreResponse);
			}
		}
	}
}
