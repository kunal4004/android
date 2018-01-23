package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.content.Context;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.LoadProduct;
import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.rest.product.LoadProductRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class GridViewModel extends BaseViewModel<GridNavigator> {

	private LoadProduct mLoadProduct;
	private LoadProductRequest mProductRequest;
	private int mNumItemsInTotal;
	private boolean loadMoreData = false;
	private int pageOffset = 0;

	public GridViewModel() {
		super();
	}

	public GridViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void setProductRequestBody(boolean isBarcode, String productId) {
		this.mLoadProduct = new LoadProduct(isBarcode, productId);
	}

	public LoadProduct getProductRequestBody() {
		return mLoadProduct;
	}

	public void executeLoadProduct(Context context, LoadProduct lp) {
		this.mProductRequest = loadProduct(context, lp);
		this.mProductRequest.execute();
	}

	public LoadProductRequest getLoadProductRequest() {
		return mProductRequest;
	}

	public LoadProductRequest loadProduct(Context context, final LoadProduct loadProduct) {
		showProgress();
		getNavigator().onLoadStart();
		return new LoadProductRequest(context, loadProduct, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ProductView productView = (ProductView) object;
				switch (productView.httpCode) {
					case 200:
						List<ProductList> productLists = productView.products;
						if (productLists != null) {
							numItemsInTotal(productView);
							calculatePageOffset();
							getNavigator().onLoadProductSuccess(productLists, getLoadMoreData());
							setLoadMoreData(true);
						}
						break;

					default:
						if (productView.response != null) {
							getNavigator().unhandledResponseCode(productView.response);
						}
						break;
				}
				hideProgress();
			}

			@Override
			public void onFailure(String e) {
				getNavigator().failureResponseHandler(e);
				hideProgress();
			}
		});
	}

	private void showProgress() {
		if (getLoadMoreData()) {
			getNavigator().showProgressBarCentered();
		} else {
			getNavigator().showProgressBarAtBottom();
		}
	}

	public void hideProgress() {
		if (getLoadMoreData()) {
			getNavigator().dismissProgressBarCentered();
		} else {
			getNavigator().dismissProgressBarAtBottom();
		}
	}

	public void setLoadMoreData(boolean loadMoreData) {
		this.loadMoreData = loadMoreData;
	}

	public boolean getLoadMoreData() {
		return loadMoreData;
	}

	private int numItemsInTotal(ProductView productView) {
		PagingResponse pagingResponse = productView.pagingResponse;
			if (pagingResponse != null) {
				mNumItemsInTotal = pagingResponse.numItemsInTotal;
			}
		return mNumItemsInTotal;
	}

	public void canLoadMore(int totalItem, int sizeOfList) {
		if (sizeOfList >= totalItem) {
			setLoadMoreData(false);
		}
	}

	public int getNumItemsInTotal() {
		return mNumItemsInTotal;
	}

	private void calculatePageOffset() {
		pageOffset = pageOffset + Utils.PAGE_SIZE;
		getProductRequestBody().setPageOffset(pageOffset);
	}
}
