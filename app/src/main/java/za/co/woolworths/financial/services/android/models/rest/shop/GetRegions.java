package za.co.woolworths.financial.services.android.models.rest.shop;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.RegionResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetRegions extends HttpAsyncTask<String, String, RegionResponse> {

	private OnEventListener mCallBack;
	private String mException;
	private String locationId;

	public GetRegions(String locationId, OnEventListener callback) {
		this.locationId = locationId;
		mCallBack = callback;
	}

	@Override
	protected RegionResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getRegions(locationId);
	}

	@Override
	protected RegionResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new RegionResponse();
	}

	@Override
	protected Class<RegionResponse> httpDoInBackgroundReturnType() {
		return RegionResponse.class;
	}

	@Override
	protected void onPostExecute(RegionResponse regionResponse) {
		super.onPostExecute(regionResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(regionResponse);
			}
		}
	}
}
