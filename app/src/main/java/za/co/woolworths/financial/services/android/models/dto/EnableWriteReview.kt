package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnableWriteReview(var foodItem: Boolean = false): Parcelable
