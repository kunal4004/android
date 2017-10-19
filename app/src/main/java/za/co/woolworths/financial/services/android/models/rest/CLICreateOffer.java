package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CLICreateOfferResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class CLICreateOffer extends HttpAsyncTask<String, String, CLICreateOfferResponse> {

	private CreateOfferRequest mCreateOfferRequest;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<CLICreateOfferResponse> mCallBack;
	private Context mContext;
	public String mException;

	public CLICreateOffer(Context context, CreateOfferRequest createOfferRequest, OnEventListener callback) {
		this.mCallBack = callback;
		this.mContext = context;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		this.mCreateOfferRequest = createOfferRequest;
	}

	@Override
	protected CLICreateOfferResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().createOfferRequest(mCreateOfferRequest);
	}

	@Override
	protected CLICreateOfferResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new CLICreateOfferResponse();
	}

	@Override
	protected Class<CLICreateOfferResponse> httpDoInBackgroundReturnType() {
		return CLICreateOfferResponse.class;
	}

	@Override
	protected void onPostExecute(CLICreateOfferResponse CLICreateOfferResponse) {
		super.onPostExecute(CLICreateOfferResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(CLICreateOfferResponse);
			}
		}
	}
}
