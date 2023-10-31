package za.co.woolworths.financial.services.android.recommendations.data.response.getresponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.ProductList

@Parcelize
data class Action(
    val actionEvents: List<String>?,
    val actionId: Int?,
    val componentName: String?,
    val experienceName: String?,
    val products: List<ProductList>?,
) : Parcelable