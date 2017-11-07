package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2017/11/07.
 */

public class CLISendEmailRequest extends HttpAsyncTask<String,String,CLIEmailResponse>{
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<CLIEmailResponse> mCallBack;
	private Context mContext;
	public String mException;

	public CLISendEmailRequest(Context context,OnEventListener callback)
	{
		this.mContext = context;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected CLIEmailResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().cliEmailResponse();
	}

	@Override
	protected CLIEmailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new CLIEmailResponse();
	}

	@Override
	protected Class<CLIEmailResponse> httpDoInBackgroundReturnType() {
		return CLIEmailResponse.class;
	}

	@Override
	protected void onPostExecute(CLIEmailResponse cliEmailResponse) {
		super.onPostExecute(cliEmailResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(cliEmailResponse);
			}
		}
	}
}
