package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class User(
    val dyid: String? = null,
    val dyid_server: String? = null
) : Parcelable
