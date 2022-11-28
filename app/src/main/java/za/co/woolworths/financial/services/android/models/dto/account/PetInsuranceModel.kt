package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PetInsuranceModel(
    var insuranceProducts: List<InsuranceProducts>,
    var httpCode: Int?
) : Parcelable


@Parcelize
data class InsuranceProducts(var type: String, var status: String, var planType: String) :
    Parcelable

enum class CoveredStatus(s: String) {
    NotCovered("NOT_COVERED"),
    Covered("COVERED"),
    Pending("PENDING")
}