package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnableWriteReview(
    var foodItem: Boolean = false,
    var fashion: Boolean = false,
    var home: Boolean = false,
    var beauty: Boolean = false,
    var tncLink: String?
): Parcelable
