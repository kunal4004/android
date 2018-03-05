package za.co.woolworths.financial.services.android.models.rest.message;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetMessage extends HttpAsyncTask<String, String, MessageResponse> {

	private OnEventListener<MessageResponse> mCallBack;
	private String mException;

	public GetMessage(OnEventListener callback) {
		mCallBack = callback;
	}

	@Override
	protected MessageResponse httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getMessagesResponse(5, 1);
	}

	@Override
	protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new MessageResponse();
	}

	@Override
	protected Class<MessageResponse> httpDoInBackgroundReturnType() {
		return MessageResponse.class;
	}

	@Override
	protected void onPostExecute(MessageResponse messageResponse) {
		super.onPostExecute(messageResponse);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(messageResponse);
			}
		}
	}
}
