package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppGUIDModel(
    var appGuid: String,
    var httpCode: Int
) : Parcelable

data class AppGUIDRequestModel(var scope:String,var product:String)

sealed class AppGUIDRequestSealed {
    data class PetInsurance( var scope: String = "petInsuranceLanding", var product: String = "Pet Insurance") :
        AppGUIDRequestSealed()
}

fun getRequestBody(type: AppGUIDRequestType): AppGUIDRequestModel {
    when (type) {
        AppGUIDRequestType.PET_INSURANCE ->{
            val petModel =  AppGUIDRequestSealed.PetInsurance()
            return AppGUIDRequestModel(petModel.scope,petModel.product)
        }
    }
}

enum class AppGUIDRequestType{
    PET_INSURANCE
}