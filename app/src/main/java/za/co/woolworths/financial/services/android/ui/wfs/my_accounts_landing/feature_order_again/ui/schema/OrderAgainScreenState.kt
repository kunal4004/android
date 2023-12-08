package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class OrderAgainScreenState: Parcelable {

    object Loading: OrderAgainScreenState()
    object ShowOrderList: OrderAgainScreenState()
    object ShowEmptyScreen: OrderAgainScreenState()
    object ShowErrorScreen: OrderAgainScreenState()
}
