package za.co.woolworths.financial.services.android.presentation.common.delivery_location

import android.os.Parcelable
import androidx.annotation.StringRes
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeliveryLocationViewState(
    @StringRes val resDeliveryType: Int = R.string.standard_delivery,
    val textDeliveryLocation: String = "",
    @StringRes val textDeliveryLocationRes: Int = R.string.default_location
) : Parcelable