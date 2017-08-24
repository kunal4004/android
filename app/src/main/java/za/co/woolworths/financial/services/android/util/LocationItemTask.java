package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;

public class LocationItemTask extends HttpAsyncTask<String, String, LocationResponse> {

	private final WGlobalState mWGlobalState;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<LocationResponse> mCallBack;
	private Context mContext;
	public String mException;

	public LocationItemTask(Context context, OnEventListener callback) {
		mCallBack = callback;
		mContext = context;
		mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		mWGlobalState = mWoolworthsApp.getWGlobalState();
	}

	@Override
	protected LocationResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getLocationsItem(mWGlobalState.getSelectedSKUId(), String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
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
