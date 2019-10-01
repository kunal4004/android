package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;

/**
 * Created by W7099877 on 2018/03/08.
 */

public interface ShoppingListItemsNavigator {

    void onItemSelectionChange(List<ShoppingListItem> items);

    void onShoppingListItemDelete(ShoppingListItemsResponse shoppingListItemsResponse);

    void onItemDeleteClick(String id, String productId, String catalogRefId, boolean shouldUpdateShoppingList);

    void onShoppingSearchClick();

    void openProductDetailFragment(String productName, ProductList productList);

    void requestDeliveryLocation(String requestMessage);

    void onQuantityChangeClick(int position, ShoppingListItem shoppingListItem);

    void onDeleteItemFailed();

    void openSetSuburbProcess(ShoppingListItem shoppingListItem);
}