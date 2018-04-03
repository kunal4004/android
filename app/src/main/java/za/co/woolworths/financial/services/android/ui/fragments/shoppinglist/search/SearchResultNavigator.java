package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
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

	void onAddToListLoad(boolean isLoading);

	void onAddToListLoadComplete(List<ShoppingListItem> shoppingLists);

	void onCheckedItem(ProductList selectedProduct, boolean viewIsLoading);

	void toggleAddToListBtn(boolean enable);

	void onLoadStart();

	void responseFailureHandler(Response response);

	void onSuccessResponse(WProduct product);

	void onLoadComplete();

	void onLoadDetailFailure(String e);

	void onFoodTypeChecked(ProductList selectedProduct);

	void unknownErrorMessage(ShoppingListItemsResponse shoppingCartResponse);

	void accountExpired(ShoppingListItemsResponse shoppingCartResponse);
}
