package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class OrderAgainScreenState: Parcelable {

    object Loading: OrderAgainScreenState()
    object ShowOrderList: OrderAgainScreenState()
    data class ShowEmptyScreen(
        val icon: Int = R.drawable.empty_list_icon,
        val title: Int = R.string.order_again_empty_title,
        val subTitle: Int = R.string.order_again_empty_desc
    ): OrderAgainScreenState()
    data class ShowErrorScreen(
        val icon: Int = R.drawable.ic_skip,
        val title: Int = R.string.unfortunately_something_went_wrong,
        val subTitle: Int = R.string.order_again_error_desc
    ): OrderAgainScreenState()
}
