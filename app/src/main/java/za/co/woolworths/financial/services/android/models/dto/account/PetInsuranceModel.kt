package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PetInsuranceModel(
    var insuranceProducts: List<InsuranceProducts>,
    var httpCode: Int?
) : Parcelable

@Parcelize
data class InsuranceProducts(var type: String,
                             var status: String,
                             var policyNumber : String? = null,
                             var planType: String? = null) : Parcelable {
    fun statusType() = CoveredStatus.valueOf(status)
}
enum class CoveredStatus {
    NOT_COVERED,
    COVERED,
    PENDING
}