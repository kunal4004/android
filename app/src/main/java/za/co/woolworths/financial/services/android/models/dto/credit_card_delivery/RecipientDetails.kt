package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecipientDetails(var telWork: String? = null, var telCell: String? = null, var deliverTo: String? = null, var idNumber: String? = null, var isThirdPartyRecipient: Boolean? = false):Parcelable