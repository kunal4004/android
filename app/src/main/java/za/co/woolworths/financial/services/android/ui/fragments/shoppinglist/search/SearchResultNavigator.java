package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;

public interface SearchResultNavigator {
	void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData);

	void unhandledResponseCode(Response response);

	void failureResponseHandler(String e);

	void cancelAPIRequest();

	void setTotalNumberOfItem();

	void bindRecyclerViewWithUI(List<ProductList> productList);

	void onGridItemSelected(ProductList productList);

	void onBottomReached();

	void startProductRequest();

	void loadMoreData(List<ProductList> productLists);

	void setProductBody();

	void onLoadStart(boolean isLoadMore);

	void onLoadComplete(boolean isLoadMore);
}
