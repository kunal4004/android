package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import org.jetbrains.annotations.NotNull;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;

/**
 * Created by W7099877 on 2018/03/08.
 */

public interface ShoppingListItemsNavigator {

    void onItemSelectionChange(Boolean isSelected);

    void onShoppingListItemDelete(ShoppingListItemsResponse shoppingListItemsResponse);

    void onItemDeleteClick(ShoppingListItem shoppingListItem);

    void onShoppingSearchClick();

    void openProductDetailFragment(String productName, ProductList productList);

    void openSetSuburbProcess(ShoppingListItem shoppingListItem);

    void onAddListItemCount(ShoppingListItem shoppingListItem);

    void onSubstractListItemCount(ShoppingListItem listItem);

    void showListBlackToolTip();

    void naviagteToMoreOptionDialog(@NotNull ShoppingListItem shoppingListItem);
}