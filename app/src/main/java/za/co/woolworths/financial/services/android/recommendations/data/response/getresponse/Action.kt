package za.co.woolworths.financial.services.android.recommendations.data.response.getresponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Action(
    val actionEvents: List<String>?,
    val actionId: Int?,
    val componentName: String?,
    val experienceName: String?,
    val products: List<Product>?,
) : Parcelable