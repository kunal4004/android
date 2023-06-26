package za.co.woolworths.financial.services.android.util.analytics.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class is to be used only for the firebase analytic - screen view event for the PLP screen
 */
@Parcelize
data class ScreenViewEventData(
    var department: String? = null,
    var category: String? = null,
    var subCategory: String? = null,
    var subSubCategory: String? = null
) : Parcelable