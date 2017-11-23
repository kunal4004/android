package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class CLIGetDeaBank extends HttpAsyncTask<String, String, DeaBanks> {

	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<DeaBanks> mCallBack;
	private Context mContext;
	private String mException;

	public CLIGetDeaBank(Context context, OnEventListener callback) {
		mCallBack = callback;
		mContext = context;
		mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected DeaBanks httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getDeaBanks();
	}

	@Override
	protected DeaBanks httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new DeaBanks();
	}

	@Override
	protected Class<DeaBanks> httpDoInBackgroundReturnType() {
		return DeaBanks.class;
	}

	@Override
	protected void onPostExecute(DeaBanks deaBanks) {
		super.onPostExecute(deaBanks);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(deaBanks);
			}
		}
	}
}
