package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ValidateStoreResponse(
    var validatePlace: ValidatePlace? = ValidatePlace(),
    var response: Response? = Response(),
    var httpCode: Int? = null,
) : Parcelable