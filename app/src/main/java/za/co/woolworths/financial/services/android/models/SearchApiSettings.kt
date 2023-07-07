package za.co.woolworths.financial.services.android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchApiSettings(
    val pageSize: Int = 60
): Parcelable
