package za.co.woolworths.financial.services.android.models.rest.product;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductDetail;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class GetProductDetail extends HttpAsyncTask<String, String, ProductDetail> {

	private ProductRequest productRequest;
	private OnEventListener mCallBack;
	private String mException;

	public GetProductDetail(ProductRequest productRequest, OnEventListener callback) {
		this.productRequest = productRequest;
		this.mCallBack = callback;
	}

	@Override
	protected ProductDetail httpDoInBackground(String... params) {
		return WoolworthsApplication.getInstance().getApi().productDetail(productRequest.getProductId(), productRequest.getSkuId());
	}

	@Override
	protected ProductDetail httpError(String errorMessage, HttpErrorCode httpErrorCode) {
		mException = errorMessage;
		mCallBack.onFailure(errorMessage);
		return new ProductDetail();
	}

	@Override
	protected Class<ProductDetail> httpDoInBackgroundReturnType() {
		return ProductDetail.class;
	}

	@Override
	protected void onPostExecute(ProductDetail productDetail) {
		super.onPostExecute(productDetail);
		if (mCallBack != null) {
			if (TextUtils.isEmpty(mException)) {
				mCallBack.onSuccess(productDetail);
			}
		}
	}
}
