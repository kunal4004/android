package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem

sealed class OrderAgainScreenEvents {
    object DeliveryLocationClick : OrderAgainScreenEvents()
    object SelectAllClick : OrderAgainScreenEvents()
    data class ProductItemCheckedChange(
        val isChecked: Boolean = false,
        val productItem: ProductItem
    ) : OrderAgainScreenEvents()

    data class ChangeProductQuantityBy(
        val count: Int = 0,
        val item: ProductItem
    ) : OrderAgainScreenEvents()
}
