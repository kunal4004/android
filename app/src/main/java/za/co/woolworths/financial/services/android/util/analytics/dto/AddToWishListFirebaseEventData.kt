package za.co.woolworths.financial.services.android.util.analytics.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddToWishListFirebaseEventData(
    var shoppingListName: String? = null,
    val products: List<AnalyticProductItem>? = null
) : Parcelable