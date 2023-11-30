package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem

sealed class OrderAgainScreenEvents {
    // On Screen events
    object Idle : OrderAgainScreenEvents()
    data class ShowSnackBar(val count: Int = 0, val maxItemLimit: Int = 0) : OrderAgainScreenEvents()
    data class HideBottomBar(val hidden: Boolean = false) : OrderAgainScreenEvents()


    // User interaction events
    object DeliveryLocationClick : OrderAgainScreenEvents()
    object StartShoppingClicked : OrderAgainScreenEvents()
    object SelectAllClick : OrderAgainScreenEvents()
    object AddToCartClicked : OrderAgainScreenEvents()
    object CopyToListClicked : OrderAgainScreenEvents()
    data class ProductItemCheckedChange(
        val isChecked: Boolean = false,
        val productItem: ProductItem
    ) : OrderAgainScreenEvents()

    data class ListItemRevealed(val item: ProductItem): OrderAgainScreenEvents()
    data class ListItemCollapsed(val item: ProductItem): OrderAgainScreenEvents()
    data class OnSwipeAddAction(val item: ProductItem): OrderAgainScreenEvents()

    data class ChangeProductQuantityBy(
        val count: Int = 0,
        val item: ProductItem
    ) : OrderAgainScreenEvents()
}
