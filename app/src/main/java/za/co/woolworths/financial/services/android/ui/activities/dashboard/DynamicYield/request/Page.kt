package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Page(
    val data: ArrayList<String>? = null,
    val location: String? = null,
    val type: String? = null,
    val dataProduct: ArrayList<DataProduct>? = null,
    val dataOther: ArrayList<DataOther>? = null
): Parcelable