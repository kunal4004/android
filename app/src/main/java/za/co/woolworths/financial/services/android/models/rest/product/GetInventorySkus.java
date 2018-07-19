package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SkuInventoryResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetInventorySkus extends HttpAsyncTask<String, String, SkuInventoryResponse> {

	private WoolworthsApplication mWoolworthsApplication;
	private OnEventListener mCallBack;
	private String mException, mMultipleSku;

	public GetInventorySkus(String multipleSKU, OnEventListener callback) {
		this.mMultipleSku = multipleSKU;
		this.mCallBack = callback;
		this.mWoolworthsApplication = WoolworthsApplication.getInstance();
	}

	@Override
	protected SkuInventoryResponse httpDoInBackground(String... params) {
		return mWoolworthsApplication.getApi().getInventorySku(mMultipleSku);
	}

	@Override
	protected SkuInventoryResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new SkuInventoryResponse();
	}

	@Override
	protected Class<SkuInventoryResponse> httpDoInBackgroundReturnType() {
		return SkuInventoryResponse.class;
	}

	@Override
	protected void onPostExecute(SkuInventoryResponse skuInventoryResponse) {
		super.onPostExecute(skuInventoryResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(skuInventoryResponse);
			}
		}
	}
}
