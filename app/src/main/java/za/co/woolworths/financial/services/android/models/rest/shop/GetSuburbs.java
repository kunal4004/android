package za.co.woolworths.financial.services.android.models.rest.shop;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetSuburbs extends HttpAsyncTask<String, String, SuburbsResponse> {

	private OnEventListener mCallBack;
	private String mException;
	private String locationId;

	public GetSuburbs(String locationId, OnEventListener callback) {
		this.locationId = locationId;
		mCallBack = callback;
	}

	@Override
	protected SuburbsResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getSuburbs(locationId);
	}

	@Override
	protected SuburbsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new SuburbsResponse();
	}

	@Override
	protected Class<SuburbsResponse> httpDoInBackgroundReturnType() {
		return SuburbsResponse.class;
	}

	@Override
	protected void onPostExecute(SuburbsResponse regionResponse) {
		super.onPostExecute(regionResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(regionResponse);
			}
		}
	}
}
