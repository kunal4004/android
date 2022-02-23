package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeliveryStatus(val code: String?, var statusDescription: String?, var displayTitle: String, val displayCopy: String?, var displayColour: String?, val isCardNew: Boolean, val isEditable: Boolean, val isCancellable: Boolean, val receivedDate: String):Parcelable