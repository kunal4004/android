package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.Response;

public interface GridNavigator {
	void onLoadProductSuccess(ProductView productView, boolean loadMoreData);

	void unhandledResponseCode(Response response);

	void failureResponseHandler(String e);

	void cancelAPIRequest();

	void bindRecyclerViewWithUI(List<ProductList> productList);

	void startProductRequest();

	void loadMoreData(List<ProductList> productLists);

	void setProductBody();

	void onLoadStart(boolean isLoadMore);

	void onLoadComplete(boolean isLoadMore);

	boolean isSearchByKeywordNavigation();
}
