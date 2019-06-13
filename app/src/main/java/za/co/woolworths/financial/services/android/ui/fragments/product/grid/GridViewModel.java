package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
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
    Call<ProductView> retrieveProduct;

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

    public void executeLoadProduct(final Activity acivity, ProductsRequestParams requestParams) {
        getNavigator().onLoadStart(getLoadMoreData());
        setProductIsLoading(true);
        retrieveProduct =  OneAppService.INSTANCE.getProducts(requestParams);
        retrieveProduct.enqueue(new CompletionHandler<>(new RequestListener<ProductView>() {
            @Override
            public void onSuccess(ProductView productView) {
                if (productView.httpCode == 200) {
                    List<ProductList> productLists = productView.products;
                    if (productLists != null) {
                        numItemsInTotal(productView);
                        calculatePageOffset();
                        getNavigator().onLoadProductSuccess(productView, getLoadMoreData());
                        getNavigator().onLoadComplete(getLoadMoreData());
                        setLoadMoreData(true);
                    }
                } else {
                    if (productView.response != null) {
                        getNavigator().onLoadComplete(getLoadMoreData());
                        getNavigator().unhandledResponseCode(productView.response);
                    }
                }
                setProductIsLoading(false);
            }

            @Override
            public void onFailure(final Throwable error) {
                if (acivity == null) return;
                acivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getNavigator().failureResponseHandler(error.toString());
                            getNavigator().onLoadComplete(getLoadMoreData());
                            setProductIsLoading(false);
                        }
                    });
                }
        },ProductView.class));
    }

    public Call<ProductView> getLoadProductRequest() {
        return retrieveProduct;
    }


    public void setLoadMoreData(boolean loadMoreData) {
        this.loadMoreData = loadMoreData;
    }

    public boolean getLoadMoreData() {
        return loadMoreData;
    }

    private int numItemsInTotal(ProductView productView) {
        PagingResponse pagingResponse = productView.pagingResponse;
        if (pagingResponse.numItemsInTotal != null && productView.pagingResponse.pageOffset != null) {
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
