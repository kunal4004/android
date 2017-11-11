package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class CLIOfferDecision extends HttpAsyncTask<String, String, OfferActive> {

	private za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision createOfferDecision;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<OfferActive> mCallBack;
	private Context mContext;
	public String mException, mCliId;

	public CLIOfferDecision(Context context, za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision createOfferDecision, String cliId, OnEventListener callback) {
		this.mCallBack = callback;
		this.mContext = context;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		this.createOfferDecision = createOfferDecision;
		this.mCliId = cliId;
	}

	@Override
	protected OfferActive httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().createOfferDecision(createOfferDecision, mCliId);
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
