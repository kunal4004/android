package za.co.woolworths.financial.services.android.models.rest.product;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LoadProduct;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class SearchProductRequest extends HttpAsyncTask<String, String, ProductView> {
	private LoadProduct mProductRequestModel;
	private WoolworthsApplication mWoolworthsApp;
	private OnEventListener<ProductView> mCallBack;
	private Context mContext;
	private String mException;

	public SearchProductRequest(Context context, LoadProduct loadProduct, OnEventListener callback) {
		this.mContext = context;
		this.mCallBack = callback;
		this.mProductRequestModel = loadProduct;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}

	@Override
	protected ProductView httpDoInBackground(String... params) {
		return mWoolworthsApp.getApi().getProductSearchList(mProductRequestModel);
//		.getProductSearchList(searchItem, false, pageNumber, Utils.PAGE_SIZE)
	}

	@Override
	protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		this.mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new ProductView();
	}

	@Override
	protected Class<ProductView> httpDoInBackgroundReturnType() {
		return ProductView.class;
	}

	@Override
	protected void onPostExecute(ProductView productView) {
		super.onPostExecute(productView);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(productView);
			}
		}
	}
}
