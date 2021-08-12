package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.util.Log;
import androidx.fragment.app.Fragment;

import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductListingExtensionFragment extends Fragment {

    private int mNumItemsInTotal;
    private boolean loadMoreData = false;
    private int pageOffset = 0;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private boolean productIsLoading = false;
    boolean isLoading = false;
    private ProductsRequestParams productsRequestParams;
    private Call<ProductView> retrieveProduct;

    private GridNavigator mNavigator;

    public void setNavigator(GridNavigator navigator) {
        this.mNavigator = navigator;
    }

    public GridNavigator getNavigator() {
        return mNavigator;
    }


    private void setIsLastPage(boolean mIsLastPage) {
        this.mIsLastPage = mIsLastPage;
    }

    public void setIsLoading(boolean mIsLoading) {
        this.mIsLoading = mIsLoading;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setProductRequestBody(ProductsRequestParams.SearchType searchType, String searchTerm, String navigationState, String sortOption) {
        this.productsRequestParams = new ProductsRequestParams(searchTerm, searchType, ProductsRequestParams.ResponseType.DETAIL, pageOffset);
        this.productsRequestParams.setRefinement(navigationState);
        this.productsRequestParams.setSortOption(sortOption);
    }

    public ProductsRequestParams getProductRequestBody() {
        return productsRequestParams;
    }

    public void executeLoadProduct(final Activity activity, ProductsRequestParams requestParams) {
        getNavigator().onLoadStart(getLoadMoreData());
        setProductIsLoading(true);
        retrieveProduct =  OneAppService.INSTANCE.getProducts(requestParams);
        retrieveProduct.enqueue(new CompletionHandler<>(new IResponseListener<ProductView>() {
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
                if (activity == null) return;
                activity.runOnUiThread(() -> {
                    getNavigator().failureResponseHandler(error.toString());
                    getNavigator().onLoadComplete(getLoadMoreData());
                    setProductIsLoading(false);
                });
                }
        },ProductView.class));
    }

    public Call<ProductView> getLoadProductRequest() {
        return retrieveProduct;
    }


    private void setLoadMoreData(boolean loadMoreData) {
        this.loadMoreData = loadMoreData;
    }

    private boolean getLoadMoreData() {
        return loadMoreData;
    }

    private void numItemsInTotal(ProductView productView) {
        PagingResponse pagingResponse = productView.pagingResponse;
        if (pagingResponse.numItemsInTotal != null && productView.pagingResponse.pageOffset != null) {
            mNumItemsInTotal = pagingResponse.numItemsInTotal;
            Log.d("paginationResponse", "pageOffset " + productView.pagingResponse.pageOffset + " mNumItemsInTotal " + mNumItemsInTotal);
            if (productView.pagingResponse.pageOffset > mNumItemsInTotal) {
                setIsLastPage(true);
            }
        }
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

    private void setProductIsLoading(boolean productIsLoading) {
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
