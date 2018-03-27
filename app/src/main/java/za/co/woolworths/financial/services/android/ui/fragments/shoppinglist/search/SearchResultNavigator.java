package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WProduct;

public interface SearchResultNavigator {
	void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData);

	void unhandledResponseCode(Response response);

	void failureResponseHandler(String e);

	void cancelAPIRequest();

	void bindRecyclerViewWithUI(List<ProductList> productList);

	void onBottomReached();

	void startProductRequest();

	void loadMoreData(List<ProductList> productLists);

	void setProductBody();

	void onLoadStart(boolean isLoadMore);

	void onLoadComplete(boolean isLoadMore);

	void onFoodTypeSelect(ProductList productList);

	void onClothingTypeSelect(ProductList productList);

	void minOneItemSelected(List<ProductList> prodList);

	void onAddToListFailure(String e);

	void onAddToListLoad();

	void onAddToListLoadComplete();

	void onCheckedItem(ProductList selectedProduct);

	void onLoadStart();

	void responseFailureHandler(Response response);

	void onSuccessResponse(WProduct product);

	void onLoadComplete();
}
