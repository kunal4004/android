package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RatingsAndReviews (val minimumSupportedAppBuildNumber: Int?,
                              var isEnabled: Boolean = false,
                              var enableWriteReview: EnableWriteReview ) :Parcelable