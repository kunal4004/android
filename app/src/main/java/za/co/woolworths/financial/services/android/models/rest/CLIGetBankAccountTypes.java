package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2017/10/05.
 */

public class CLIGetBankAccountTypes extends HttpAsyncTask<String, String, BankAccountTypes> {

	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<BankAccountTypes> mCallBack;
	private Context mContext;
	private String mException;

	public CLIGetBankAccountTypes(Context context, OnEventListener callback) {
		mCallBack = callback;
		mContext = context;
		mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected BankAccountTypes httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getBankAccountTypes();
	}

	@Override
	protected BankAccountTypes httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new BankAccountTypes();
	}

	@Override
	protected Class<BankAccountTypes> httpDoInBackgroundReturnType() {
		return BankAccountTypes.class;
	}

	@Override
	protected void onPostExecute(BankAccountTypes bankAccountTypes) {
		super.onPostExecute(bankAccountTypes);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(bankAccountTypes);
			}
		}
	}
}
