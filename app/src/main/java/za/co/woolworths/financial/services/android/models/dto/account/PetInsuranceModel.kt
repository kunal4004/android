package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.Response


@Parcelize
data class PetInsuranceModel(
    var insuranceProducts: List<InsuranceProducts>,
    var httpCode: Int?
) : Parcelable


@Parcelize
data class InsuranceProducts(var type: String, var covered: Boolean) :
    Parcelable