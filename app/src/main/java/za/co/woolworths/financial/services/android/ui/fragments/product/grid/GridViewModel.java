package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.rest.product.GetProductsRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class GridViewModel extends BaseViewModel<GridNavigator> {

    private int mNumItemsInTotal;
    private boolean loadMoreData = false;
    private int pageOffset = 0;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private int mLoadStatus;
    private boolean productIsLoading = false;
    private ProductsRequestParams productsRequestParams;
    private GetProductsRequest mGetProductsRequest;

    public void setLoadStatus(int status) {
        this.mLoadStatus = status;
    }

    public int getLoadStatus() {
        return mLoadStatus;
    }

    public GridViewModel() {
        super();
    }

    public GridViewModel(SchedulerProvider schedulerProvider) {
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

    public void setProductRequestBody(ProductsRequestParams.SearchType searchType, String searchTerm) {
        this.productsRequestParams = new ProductsRequestParams(searchTerm, searchType, ProductsRequestParams.ResponseType.DETAIL, pageOffset);
    }


    public ProductsRequestParams getProductRequestBody() {
        return productsRequestParams;
    }

    public void executeLoadProduct(Context context, ProductsRequestParams lp) {
        this.mGetProductsRequest = loadProducts(context, lp);
        this.mGetProductsRequest.execute();
    }

    public GetProductsRequest getLoadProductRequest() {
        return mGetProductsRequest;
    }


    GetProductsRequest loadProducts(final Context context, final ProductsRequestParams requestParams) {
        getNavigator().onLoadStart(getLoadMoreData());
        setProductIsLoading(true);
        return new GetProductsRequest(context, requestParams, new OnEventListener<ProductView>() {
            @Override
            public void onSuccess(ProductView object) {
                ProductView productView = (ProductView) object;
                switch (productView.httpCode) {
                    case 200:
                        List<ProductList> productLists = productView.products;
                        if (productLists != null) {
                            numItemsInTotal(productView);
                            calculatePageOffset();
                            getNavigator().onLoadProductSuccess(productView, getLoadMoreData());
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
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getNavigator().failureResponseHandler(e);
                                getNavigator().onLoadComplete(getLoadMoreData());
                                setProductIsLoading(false);
                            }
                        });
                    }
                }
            }
        });
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
            Log.e("paginationResponse", "pageOffset " + productView.pagingResponse.pageOffset + " mNumItemsInTotal " + mNumItemsInTotal);
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

    public void setProductIsLoading(boolean productIsLoading) {
        this.productIsLoading = productIsLoading;
    }

    public boolean productIsLoading() {
        return productIsLoading;
    }

    public void updateProductRequestBodyForRefinement(String navigationState){
        this.pageOffset = 0;
        this.loadMoreData = false;
        this.mIsLoading = false;
        this.mIsLastPage = false;
        this.productIsLoading = false;
        getProductRequestBody().setPageOffset(pageOffset);
        getProductRequestBody().setRefinement(navigationState);

    }

    public void updateProductRequestBodyForSort(String sortOption){
        this.pageOffset = 0;
        this.loadMoreData = false;
        this.mIsLoading = false;
        this.mIsLastPage = false;
        this.productIsLoading = false;
        getProductRequestBody().setPageOffset(pageOffset);
        getProductRequestBody().setSortOption(sortOption);

    }
}
