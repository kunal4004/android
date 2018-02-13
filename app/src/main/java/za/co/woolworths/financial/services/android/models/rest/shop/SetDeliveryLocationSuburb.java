package za.co.woolworths.financial.services.android.models.rest.shop;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2017/11/04.
 */

public class SetDeliveryLocationSuburb extends HttpAsyncTask<String, String, SetDeliveryLocationSuburbResponse> {

	private String suburbId;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<SetDeliveryLocationSuburbResponse> mCallBack;
	private Context mContext;
	public String mException;

	public SetDeliveryLocationSuburb(Context context, String suburbId, OnEventListener callback)
	{
		this.mContext = context;
		this.suburbId = suburbId;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected SetDeliveryLocationSuburbResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().setSuburb(suburbId);
	}

	@Override
	protected SetDeliveryLocationSuburbResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new SetDeliveryLocationSuburbResponse();
	}

	@Override
	protected Class<SetDeliveryLocationSuburbResponse> httpDoInBackgroundReturnType() {
		return SetDeliveryLocationSuburbResponse.class;
	}

	@Override
	protected void onPostExecute(SetDeliveryLocationSuburbResponse response) {
		super.onPostExecute(response);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(response);
			}
		}
	}
}
