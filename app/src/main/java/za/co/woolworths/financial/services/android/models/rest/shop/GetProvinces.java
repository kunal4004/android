package za.co.woolworths.financial.services.android.models.rest.shop;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse;
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetProvinces extends HttpAsyncTask<String, String, ProvincesResponse> {

	private OnEventListener mCallBack;
	private String mException;

	public GetProvinces(OnEventListener callback) {
		mCallBack = callback;
	}

	@Override
	protected ProvincesResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getProvinces();
	}

	@Override
	protected ProvincesResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new ProvincesResponse();
	}

	@Override
	protected Class<ProvincesResponse> httpDoInBackgroundReturnType() {
		return ProvincesResponse.class;
	}

	@Override
	protected void onPostExecute(ProvincesResponse regionResponse) {
		super.onPostExecute(regionResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(regionResponse);
			}
		}
	}
}
