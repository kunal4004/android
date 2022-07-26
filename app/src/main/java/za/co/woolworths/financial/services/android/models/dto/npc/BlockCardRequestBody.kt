package za.co.woolworths.financial.services.android.models.dto.npc

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockCardRequestBody(
    val visionAccountNumber: String,
    val cardNumber: String,
    val sequenceNumber: Int,
    val blockReason: Int?
) : Parcelable