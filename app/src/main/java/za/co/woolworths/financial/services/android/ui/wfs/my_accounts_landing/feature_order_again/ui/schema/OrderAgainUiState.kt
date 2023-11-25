package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationViewState

@Parcelize
data class OrderAgainUiState(
    val isLoading: Boolean = false,
    val screenState: OrderAgainScreenState = OrderAgainScreenState.Loading,
    val deliveryState: DeliveryLocationViewState = DeliveryLocationViewState(),
    val orderList: List<ProductItem> = emptyList()
): Parcelable