package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewState
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationViewState

@Parcelize
data class OrderAgainUiState(
    val isLoading: Boolean = false,
    val headerState: @RawValue HeaderViewState.HeaderStateType1 =  HeaderViewState.HeaderStateType1(
        titleRes =  R.string.order_again,
        rightButtonRes = R.string.select_all
    ),
    val screenState: OrderAgainScreenState = OrderAgainScreenState.Loading,
    val deliveryState: DeliveryLocationViewState = DeliveryLocationViewState(),
    val revealedList: List<String> = emptyList(),
    val showAddToCart: Boolean = false,
    val itemsToBeAddedCount: Int = 0,
    val maxItemLimit: Int = 0,
    val resIdCopyToList: Int = R.string.copy_to_list
): Parcelable