package za.co.woolworths.financial.services.android.util;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;

public class LocationItemTask extends HttpAsyncTask<String, String, LocationResponse> {

	private final WGlobalState mWGlobalState;
	private WoolworthsApplication mWoolworthApplication;
	private OnEventListener<LocationResponse> mCallBack;
	public String mException;
	public OtherSkus otherSkus;

	public LocationItemTask(OnEventListener callback) {
		mCallBack = callback;
		mWoolworthApplication = WoolworthsApplication.getInstance();
		mWGlobalState = mWoolworthApplication.getWGlobalState();
	}

	public LocationItemTask(OnEventListener callback, OtherSkus otherSkus) {
		mCallBack = callback;
		mWoolworthApplication = WoolworthsApplication.getInstance();
		mWGlobalState = mWoolworthApplication.getWGlobalState();
		this.otherSkus = otherSkus;
	}

	@Override
	protected LocationResponse httpDoInBackground(String... params) {
		if (otherSkus != null)
			return mWoolworthApplication.getApi().getLocationsItem(otherSkus.sku, String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
		else
			return mWoolworthApplication.getApi().getLocationsItem(mWGlobalState.getSelectedSKUId().sku, String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
	}

	@Override
	protected LocationResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new LocationResponse();
	}

	@Override
	protected Class<LocationResponse> httpDoInBackgroundReturnType() {
		return LocationResponse.class;
	}

	@Override
	protected void onPostExecute(LocationResponse locationResponse) {
		super.onPostExecute(locationResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(locationResponse);
			}
		}
	}
}
