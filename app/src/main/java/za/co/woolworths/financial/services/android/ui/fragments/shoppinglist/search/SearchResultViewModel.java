package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import android.app.Activity;
import android.content.Context;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.LoadProduct;
import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.rest.product.LoadProductRequest;
import za.co.woolworths.financial.services.android.models.rest.product.SearchProductRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class SearchResultViewModel extends BaseViewModel<SearchResultNavigator> {

	private LoadProduct mLoadProduct;
	private LoadProductRequest mProductRequest;
	private SearchProductRequest mSearchProductRequest;
	private int mNumItemsInTotal;
	private boolean loadMoreData = false;
	private int pageOffset = 0;
	private boolean mIsLoading = false;
	private boolean mIsLastPage = false;
	private int mLoadStatus;

	public void setLoadStatus(int status) {
		this.mLoadStatus = status;
	}

	public int getLoadStatus() {
		return mLoadStatus;
	}

	public SearchResultViewModel() {
		super();
	}

	public SearchResultViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void setPageOffset(int pageOffset) {
		this.pageOffset = pageOffset;
	}

	public void setIsLastPage(boolean mIsLastPage) {
		this.mIsLastPage = mIsLastPage;
	}

	public void setIsLoading(boolean mIsLoading) {
		this.mIsLoading = mIsLoading;
	}

	public boolean isLastPage() {
		return mIsLastPage;
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public void setProductRequestBody(boolean isBarcode, String productId) {
		this.mLoadProduct = new LoadProduct(isBarcode, productId);
	}

	public void setProductRequestBody(String searchProduct, boolean isBarcode) {
		this.mLoadProduct = new LoadProduct(isBarcode, searchProduct);
	}


	public LoadProduct getProductRequestBody() {
		return mLoadProduct;
	}

	public void executeSearchProduct(Context context, LoadProduct lp) {
		this.mSearchProductRequest = searchProduct(context, lp);
		this.mSearchProductRequest.execute();
	}

	public SearchProductRequest getSearchProductRequest() {
		return mSearchProductRequest;
	}

	public void setLoadMoreData(boolean loadMoreData) {
		this.loadMoreData = loadMoreData;
	}

	public boolean getLoadMoreData() {
		return loadMoreData;
	}

	private int numItemsInTotal(ProductView productView) {
		PagingResponse pagingResponse = productView.pagingResponse;
		if (pagingResponse.numItemsInTotal != null) {
			mNumItemsInTotal = pagingResponse.numItemsInTotal;
			if (productView.pagingResponse.pageOffset > mNumItemsInTotal) {
				setIsLastPage(true);
			}
			return mNumItemsInTotal;
		}
		return 0;
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

	public SearchProductRequest searchProduct(final Context context, final LoadProduct loadProduct) {
		getNavigator().onLoadStart(getLoadMoreData());
		return new SearchProductRequest(context, loadProduct, new OnEventListener() {
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
							getNavigator().onLoadComplete(getLoadMoreData());
							setLoadMoreData(true);
						}
						break;

					default:
						if (productView.response != null) {
							getNavigator().onLoadComplete(getLoadMoreData());
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
								getNavigator().failureResponseHandler(e);
								getNavigator().onLoadComplete(getLoadMoreData());
							}
						});
					}
				}
			}
		});
	}
}
