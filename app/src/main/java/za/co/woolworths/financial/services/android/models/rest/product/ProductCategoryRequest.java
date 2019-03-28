package za.co.woolworths.financial.services.android.models.rest.product;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class ProductCategoryRequest extends HttpAsyncTask<String, String, RootCategories> {
	private OnEventListener<RootCategories> mCallBack;
	public String mException;

	public ProductCategoryRequest(OnEventListener callback) {
		this.mCallBack = callback;
	}

	@Override
	protected RootCategories httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().getRootCategory();
	}

	@Override
	protected RootCategories httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new RootCategories();
	}

	@Override
	protected Class<RootCategories> httpDoInBackgroundReturnType() {
		return RootCategories.class;
	}

	@Override
	protected void onPostExecute(RootCategories rootCategories) {
		super.onPostExecute(rootCategories);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(rootCategories);
			}
		}
	}
}
