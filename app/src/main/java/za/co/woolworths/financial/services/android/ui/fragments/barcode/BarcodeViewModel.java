package za.co.woolworths.financial.services.android.ui.fragments.barcode;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.LoadProduct;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest;
import za.co.woolworths.financial.services.android.models.rest.product.SearchProductRequest;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class BarcodeViewModel extends BaseViewModel<BarcodeNavigator> {

	private boolean productIsLoading;
	private LoadProduct mLoadProduct;
	private ProductRequest mProductRequest;
	private GetProductDetail mGetProductDetail;
	private SearchProductRequest mGetBarcodeProduct;
	private boolean connectionHasFailed = false;

	public BarcodeViewModel() {
		super();
	}

	public BarcodeViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void executeGetBarcodeProduct(Context context) {
		mGetBarcodeProduct = getBarcodeProduct(context, getProductRequestBody());
		mGetBarcodeProduct.execute();
	}

	public SearchProductRequest getBarcodeProduct(final Context context, final LoadProduct loadProduct) {
		setProductIsLoading(true);
		getNavigator().onLoadStart();
		return new SearchProductRequest(context, loadProduct, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ProductView productView = (ProductView) object;
				switch (productView.httpCode) {
					case 200:
						List<ProductList> productLists = productView.products;
						if (productLists.size() == 0) {
							getNavigator().noItemFound();
							if (context != null) {
								errorScanCode((AppCompatActivity) context);
							}
							setProductIsLoading(false);
							setConnectionHasFailed(false);
							return;
						}

						if (productLists != null) {
							if (productLists.get(0) != null) {
								mProductRequest = new ProductRequest(productLists.get(0).productId, productLists.get(0).sku);
								executeProductDetail(mProductRequest);
							}
						}
						break;

					default:
						setProductIsLoading(false);
						setConnectionHasFailed(false);
						if (productView.response != null) {
							getNavigator().unhandledResponseCode(productView.response);
						}
						break;
				}
			}

			@Override
			public void onFailure(final String e) {
				if (context != null) {
					Activity activity = (Activity) context;
					if (activity != null) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setConnectionHasFailed(true);
								getNavigator().failureResponseHandler(e);
								setProductIsLoading(false);
							}
						});
					}
				}
			}
		});
	}

	public void setProductIsLoading(boolean productIsLoading) {
		this.productIsLoading = productIsLoading;
	}

	public boolean productIsLoading() {
		return productIsLoading;
	}

	/***
	 * ProductDetails detail calls
	 */

	public void executeProductDetail(ProductRequest productRequest) {
		mGetProductDetail = getProductDetail(productRequest);
		mGetProductDetail.execute();
	}

	public GetProductDetail getProductDetail(ProductRequest productRequest) {
		setProductIsLoading(true);
		return new GetProductDetail(productRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ProductDetailResponse productDetail = (ProductDetailResponse) object;
				String detailProduct = Utils.objectToJson(productDetail);
				switch (productDetail.httpCode) {
					case 200:
						//final WProduct wProduct = (WProduct) Utils.strToJson(detailProduct, WProduct.class);
						getNavigator().onLoadProductSuccess(productDetail, detailProduct);
						break;
					default:
						if (productDetail.response != null) {
							getNavigator().unhandledResponseCode(productDetail.response);
						}
						break;
				}
				setProductIsLoading(false);
				setConnectionHasFailed(false);
			}

			@Override
			public void onFailure(String e) {
				setConnectionHasFailed(true);
				getNavigator().failureResponseHandler(e);
				setProductIsLoading(false);
			}
		});
	}

	public void setProductRequestBody(boolean isBarcode, String productId) {
		this.mLoadProduct = new LoadProduct(isBarcode, productId);
	}

	private LoadProduct getProductRequestBody() {
		return mLoadProduct;
	}

	public void setConnectionHasFailed(boolean connectionHasFailed) {
		this.connectionHasFailed = connectionHasFailed;
	}

	public boolean connectionHasFailed() {
		return connectionHasFailed;
	}

	public void cancelRequest() {
		cancelRequest(mGetProductDetail);
		cancelRequest(mGetBarcodeProduct);
	}

	private void errorScanCode(Activity activity) {
		Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.BARCODE_ERROR, "");
	}
}
