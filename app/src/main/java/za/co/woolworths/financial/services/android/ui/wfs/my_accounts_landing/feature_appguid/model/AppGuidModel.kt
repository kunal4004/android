package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppGUIDResponse(
    val appGuid: String,
    val httpCode: Int
) : Parcelable

@Parcelize
data class AppGUIDRequestModel(
    val scope: String,
    val product: String
) : Parcelable

sealed class AppGUIDRequestType {

    sealed class AppGUIDRequestSealed(val scope: String, val product: String) : AppGUIDRequestType() {
        object PETINSURANCE : AppGUIDRequestSealed(
            scope = "petInsuranceLanding",
            product = "Pet Insurance"
        )
    }

    fun toRequestModel(): AppGUIDRequestModel =
        when (this) {
            is AppGUIDRequestSealed -> AppGUIDRequestModel(scope, product)
        }
}
