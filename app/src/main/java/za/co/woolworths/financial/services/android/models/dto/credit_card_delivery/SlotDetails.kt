package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SlotDetails(
    var appointmentDate: String? = null,
    var slot: String? = null,
    var formattedDate: String? = null
) : Parcelable