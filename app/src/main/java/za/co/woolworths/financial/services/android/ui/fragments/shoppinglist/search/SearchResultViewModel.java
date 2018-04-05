package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.LoadProduct;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetail;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest;
import za.co.woolworths.financial.services.android.models.rest.product.SearchProductRequest;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class SearchResultViewModel extends BaseViewModel<SearchResultNavigator> {

	private LoadProduct mLoadProduct;
	private SearchProductRequest mSearchProductRequest;
	private int mNumItemsInTotal;
	private boolean loadMoreData = false;
	private int pageOffset = 0;
	private boolean mIsLoading = false;
	private boolean mIsLastPage = false;
	private int mLoadStatus;
	private ArrayList<OtherSkus> otherSkus;
	private boolean productIsLoading;

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
			if (productView.pagingResponse.numItemsOnPage > mNumItemsInTotal) {
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
		setProductIsLoading(true);
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
				setProductIsLoading(false);
			}

			@Override
			public void onFailure(final String e) {
				if (context != null) {
					Activity activity = (Activity) context;
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setProductIsLoading(false);
							getNavigator().failureResponseHandler(e);
							getNavigator().onLoadComplete(getLoadMoreData());
						}
					});
				}
			}
		});
	}

	public PostAddToList addToList(List<AddToListRequest> addToListRequest, String listId) {
		getNavigator().onAddToListLoad(true);
		return new PostAddToList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse shoppingCartResponse = (ShoppingListItemsResponse) object;
				switch (shoppingCartResponse.httpCode) {
					case 200:
						getNavigator().onAddToListLoadComplete(shoppingCartResponse.listItems);
						break;

					case 440:
						getNavigator().accountExpired(shoppingCartResponse);
						getNavigator().onAddToListLoad(false);
						break;

					default:
						getNavigator().unknownErrorMessage(shoppingCartResponse);
						break;
				}
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onAddToListFailure(e);
			}
		}, addToListRequest, listId);
	}

	public GetProductDetail getProductDetail(ProductRequest productRequest) {
		getNavigator().onLoadStart();
		//setProductLoadFail(false);
		return new GetProductDetail(productRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ProductDetail productDetail = (ProductDetail) object;
				String detailProduct = Utils.objectToJson(productDetail);
				switch (productDetail.httpCode) {
					case 200:
						if (productDetail.product != null) {
							setOtherSkus(productDetail.product.otherSkus);
						}
						WProduct product = (WProduct) Utils.strToJson(detailProduct, WProduct.class);
						getNavigator().onSuccessResponse(product);
						break;
					default:
						if (productDetail.response != null) {
							getNavigator().responseFailureHandler(productDetail.response);
						}
						break;
				}
				//	setProductLoadFail(false);
				getNavigator().onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onLoadDetailFailure(e);
			}
		});
	}


	public ArrayList<OtherSkus> getColorList() {
		Collections.sort(getOtherSkus(), new Comparator<OtherSkus>() {
			@Override
			public int compare(OtherSkus lhs, OtherSkus rhs) {
				return lhs.colour.compareToIgnoreCase(rhs.colour);
			}
		});

		ArrayList<OtherSkus> commonColorSku = new ArrayList<>();
		for (OtherSkus sku : getOtherSkus()) {
			if (!colourValueExist(commonColorSku, sku.colour)) {
				commonColorSku.add(sku);
			}
		}
		return commonColorSku;
	}

	public ArrayList<OtherSkus> getSizeList() {
		Collections.sort(getOtherSkus(), new Comparator<OtherSkus>() {
			@Override
			public int compare(OtherSkus lhs, OtherSkus rhs) {
				return lhs.size.compareToIgnoreCase(rhs.size);
			}
		});

		ArrayList<OtherSkus> commonColorSku = new ArrayList<>();
		for (OtherSkus sku : getOtherSkus()) {
			if (!colourValueExist(commonColorSku, sku.size)) {
				commonColorSku.add(sku);
			}
		}
		return commonColorSku;
	}

	public boolean colourValueExist(ArrayList<OtherSkus> list, String name) {
		for (OtherSkus item : list) {
			if (item.colour.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void setOtherSkus(ArrayList<OtherSkus> otherSkus) {
		this.otherSkus = otherSkus;
	}

	public ArrayList<OtherSkus> getOtherSkus() {
		return otherSkus;
	}

	public ArrayList<OtherSkus> commonSizeList(OtherSkus otherSku) throws NullPointerException {
		ArrayList<OtherSkus> commonSizeList = new ArrayList<>();
		// filter by colour
		ArrayList<OtherSkus> sizeList = new ArrayList<>();
		for (OtherSkus sku : getOtherSkus()) {
			if (sku.colour != null) {
				if (sku.colour.equalsIgnoreCase(otherSku.colour)) {
					sizeList.add(sku);
				}
			}
		}

		//remove duplicates
		for (OtherSkus os : sizeList) {
			if (!sizeValueExist(commonSizeList, os.size)) {
				commonSizeList.add(os);
			}
		}
		return commonSizeList;
	}

	private boolean sizeValueExist(ArrayList<OtherSkus> list, String name) {
		for (OtherSkus item : list) {
			if (item.size.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void setProductIsLoading(boolean productIsLoading) {
		this.productIsLoading = productIsLoading;
	}

	public boolean productIsLoading() {
		return productIsLoading;
	}
}
