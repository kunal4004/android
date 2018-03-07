package za.co.woolworths.financial.services.android.models.rest.product;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class ProductSubCategoryRequest extends HttpAsyncTask<String, String, SubCategories> {
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<SubCategories> mCallBack;
	private Context mContext;
	public String mException, category_id;

	public ProductSubCategoryRequest(Context context, String category_id, OnEventListener callback) {
		this.category_id = category_id;
		this.mContext = context;
		this.mCallBack = callback;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected SubCategories httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getSubCategory(category_id);
	}

	@Override
	protected SubCategories httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new SubCategories();
	}

	@Override
	protected Class<SubCategories> httpDoInBackgroundReturnType() {
		return SubCategories.class;
	}

	@Override
	protected void onPostExecute(SubCategories subCategories) {
		super.onPostExecute(subCategories);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(subCategories);
			}
		}
	}
}
