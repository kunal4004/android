package za.co.woolworths.financial.services.android.ui.activities.write_a_review.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Response(
    val code: String,
    val desc: String
): Parcelable