package za.co.woolworths.financial.services.android.models.rest.faq;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class FAQRequest extends HttpAsyncTask<String, String, FAQ> {

	private OnEventListener<FAQ> mCallBack;
	public String mException;

	public FAQRequest(OnEventListener callback) {
		this.mCallBack = callback;
	}

	@Override
	protected FAQ httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi()
				.getFAQ();
	}

	@Override
	protected FAQ httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new FAQ();
	}

	@Override
	protected Class<FAQ> httpDoInBackgroundReturnType() {
		return FAQ.class;
	}

	@Override
	protected void onPostExecute(FAQ faq) {
		super.onPostExecute(faq);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(faq);
			}
		}
	}
}
