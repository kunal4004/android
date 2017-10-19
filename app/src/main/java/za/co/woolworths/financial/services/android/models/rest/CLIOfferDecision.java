package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.CLICreateOfferResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class CLIOfferDecision extends HttpAsyncTask<String, String, CLICreateOfferResponse> {

	private CreateOfferDecision createOfferDecision;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<CLICreateOfferResponse> mCallBack;
	private Context mContext;
	public String mException, mCliId;

	public CLIOfferDecision(Context context, CreateOfferDecision createOfferDecision, String cliId, OnEventListener callback) {
		this.mCallBack = callback;
		this.mContext = context;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
		this.createOfferDecision = createOfferDecision;
		this.mCliId = cliId;
	}

	@Override
	protected CLICreateOfferResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().createOfferDecision(createOfferDecision, mCliId);
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
