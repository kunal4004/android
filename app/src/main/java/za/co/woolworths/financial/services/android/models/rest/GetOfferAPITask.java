package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetOfferAPITask extends HttpAsyncTask<String, String, CreateOfferResponse> {

	private final WGlobalState mWGlobalState;
	private CreateOfferRequest mCreateOfferRequest;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<CreateOfferResponse> mCallBack;
	private Context mContext;
	public String mException;

	public GetOfferAPITask(Context context, CreateOfferRequest createOfferRequest, OnEventListener callback) {
		mCallBack = callback;
		mContext = context;
		mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		mWGlobalState = mWoolworthsApp.getWGlobalState();
		this.mCreateOfferRequest = createOfferRequest;
	}

	@Override
	protected CreateOfferResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().createOfferRequest(mCreateOfferRequest);
	}

	@Override
	protected CreateOfferResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new CreateOfferResponse();
	}

	@Override
	protected Class<CreateOfferResponse> httpDoInBackgroundReturnType() {
		return CreateOfferResponse.class;
	}

	@Override
	protected void onPostExecute(CreateOfferResponse createOfferResponse) {
		super.onPostExecute(createOfferResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(createOfferResponse);
			}
		}
	}
}
