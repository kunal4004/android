package za.co.woolworths.financial.services.android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.util.Utils

@Parcelize
data class SearchApiSettings(
    val pageSize: Int = Utils.PAGE_SIZE
): Parcelable
