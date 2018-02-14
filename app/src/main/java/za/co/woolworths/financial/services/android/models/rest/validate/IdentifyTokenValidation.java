package za.co.woolworths.financial.services.android.models.rest.validate;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.TokenValidationResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class IdentifyTokenValidation extends HttpAsyncTask<String, String, TokenValidationResponse> {

	private OnEventListener mCallBack;
	private String mException;

	public IdentifyTokenValidation(OnEventListener callback) {
		mCallBack = callback;
	}

	@Override
	protected TokenValidationResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().validateToken();
	}

	@Override
	protected TokenValidationResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new TokenValidationResponse();
	}

	@Override
	protected Class<TokenValidationResponse> httpDoInBackgroundReturnType() {
		return TokenValidationResponse.class;
	}

	@Override
	protected void onPostExecute(TokenValidationResponse tokenValidationResponse) {
		super.onPostExecute(tokenValidationResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(tokenValidationResponse);
			}
		}
	}
}
