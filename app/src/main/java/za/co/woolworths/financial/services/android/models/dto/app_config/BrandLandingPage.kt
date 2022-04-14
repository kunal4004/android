package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BrandLandingPage(
    val minimumSupportedAppBuildNumber: Int,
    var isEnabled: Boolean,
    val brandCategories: ArrayList<BrandCategory>
) : Parcelable
