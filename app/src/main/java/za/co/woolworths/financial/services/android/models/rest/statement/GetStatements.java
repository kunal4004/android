package za.co.woolworths.financial.services.android.models.rest.statement;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.statement.UserStatement;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetStatements extends HttpAsyncTask<String, String, StatementResponse> {

	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<StatementResponse> mCallBack;
	private Context mContext;
	private String mException;
	private UserStatement statement;

	public GetStatements(Context context, UserStatement statement, OnEventListener callback) {
		this.mContext = context;
		this.statement = statement;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected StatementResponse httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getStatementResponse(statement);
	}

	@Override
	protected StatementResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new StatementResponse();
	}

	@Override
	protected Class<StatementResponse> httpDoInBackgroundReturnType() {
		return StatementResponse.class;
	}

	@Override
	protected void onPostExecute(StatementResponse statementResponse) {
		super.onPostExecute(statementResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(statementResponse);
			}
		}
	}
}
