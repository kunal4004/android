package za.co.woolworths.financial.services.android.ui.activities.write_a_review.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PrepareWriteAReviewFormRequestEvent(
    val usernickname: String? = null,
    val rating: Int? = null,
    val rating_Quality: Int? = null,
    val rating_Value: Int? = null,
    val isrecommended: Boolean? = null,
    val title: String? = null,
    val reviewtext: String? = null
): Parcelable