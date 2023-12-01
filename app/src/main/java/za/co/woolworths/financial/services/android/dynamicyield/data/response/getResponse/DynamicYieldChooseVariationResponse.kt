package za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DynamicYieldChooseVariationResponse(
    val choices: List<String> = listOf(),
    val cookies: List<Cooky>,
    val httpCode: Int,
    val response: Response
): Parcelable