package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import androidx.annotation.PluralsRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem

sealed class OrderAgainScreenEvents {
    // On Screen events
    object Idle : OrderAgainScreenEvents()

    object CopyToListError : OrderAgainScreenEvents()
    data class ShowSnackBar(val snackbarDetails: SnackbarDetails) : OrderAgainScreenEvents()
    data class ShowErrorSnackBar(val snackbarDetails: SnackbarDetails) : OrderAgainScreenEvents()
    data class HideBottomBar(val hidden: Boolean = false) : OrderAgainScreenEvents()
    data class ShowAddToCartError(val code: Int = 500, val errorMessage: String = "") : OrderAgainScreenEvents()
    data class CopyToListSuccess(val snackbarDetails: SnackbarDetails) : OrderAgainScreenEvents()
    data class ShowProgressView(
        @PluralsRes val titleRes: Int = R.plurals.copy_item,
        val descRes: Int = R.string.empty,
        val count: Int = 0
    ) : OrderAgainScreenEvents()


    // User interaction events
    object ChangeDeliveryClick : OrderAgainScreenEvents()
    object ChangeAddressClick : OrderAgainScreenEvents()
    object StartShoppingClicked : OrderAgainScreenEvents()
    object SelectAllClick : OrderAgainScreenEvents()
    object AddToCartClicked : OrderAgainScreenEvents()
    object CopyToListClicked : OrderAgainScreenEvents()
    data class CopyItemToListClicked(val item: ProductItem) : OrderAgainScreenEvents()
    object SnackbarViewClicked : OrderAgainScreenEvents()
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
