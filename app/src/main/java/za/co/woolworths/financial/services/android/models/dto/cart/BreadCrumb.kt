package za.co.woolworths.financial.services.android.models.dto.cart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BreadCrumb(var label: String, var navigationURL: String): Parcelable