package za.co.woolworths.financial.services.android.models.rest.cli;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class CLIGetOfferActive extends HttpAsyncTask<String, String, OfferActive> {

	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<OfferActive> mCallBack;
	private Context mContext;
	private String mException;
	private String productOfferingId;

	public CLIGetOfferActive(Context context, String productOfferingId, OnEventListener callback) {
		this.mContext = context;
		this.productOfferingId = productOfferingId;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected OfferActive httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getActiveOfferRequest(productOfferingId);
	}

	@Override
	protected OfferActive httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
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
