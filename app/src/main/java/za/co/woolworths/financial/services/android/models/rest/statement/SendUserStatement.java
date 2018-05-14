package za.co.woolworths.financial.services.android.models.rest.statement;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class SendUserStatement extends HttpAsyncTask<String, String, SendUserStatementResponse> {

	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<SendUserStatementResponse> mCallBack;
	private Context mContext;
	private String mException;
	private SendUserStatementRequest statement;

	public SendUserStatement(Context context, SendUserStatementRequest statement, OnEventListener callback) {
		this.mContext = context;
		this.statement = statement;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected SendUserStatementResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().sendStatementRequest(statement);
	}

	@Override
	protected SendUserStatementResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new SendUserStatementResponse();
	}

	@Override
	protected Class<SendUserStatementResponse> httpDoInBackgroundReturnType() {
		return SendUserStatementResponse.class;
	}

	@Override
	protected void onPostExecute(SendUserStatementResponse sendUserStatementResponse) {
		super.onPostExecute(sendUserStatementResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(sendUserStatementResponse);
			}
		}
	}
}
