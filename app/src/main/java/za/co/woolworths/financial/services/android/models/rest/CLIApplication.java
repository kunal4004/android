package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class CLIApplication extends HttpAsyncTask<String, String, OfferActive> {

	private CreateOfferRequest mCreateOfferRequest;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<OfferActive> mCallBack;
	private Context mContext;
	public String mException;

	public CLIApplication(Context context, CreateOfferRequest createOfferRequest, OnEventListener callback) {
		this.mCallBack = callback;
		this.mContext = context;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		this.mCreateOfferRequest = createOfferRequest;
	}

	@Override
	protected OfferActive httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().cliApplication(mCreateOfferRequest);
	}

	@Override
	protected OfferActive httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new OfferActive();
	}

	@Override
	protected Class<OfferActive> httpDoInBackgroundReturnType() {
		return OfferActive.class;
	}

	@Override
	protected void onPostExecute(OfferActive offerActive) {
		super.onPostExecute(offerActive);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(offerActive);
			}
		}
	}
}
