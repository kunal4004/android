package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema

import android.os.Parcelable
import androidx.annotation.StringRes
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap

@Parcelize
data class SnackbarDetails(
 val title: String = "",
 @StringRes val errorTitle: Int = R.string.empty,
 val desc: String = "",
 val listName: String = "",
 val listId: String = "",
 val count: Int = 0,
 val maxItem: Int = 0,
 val showDesc: Boolean = false,
 val productCountMap: ProductCountMap? = null
): Parcelable