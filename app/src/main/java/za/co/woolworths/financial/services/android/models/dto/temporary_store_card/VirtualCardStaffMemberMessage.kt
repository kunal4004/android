package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VirtualCardStaffMemberMessage (
    val title: String,
    val paragraphs: List<String>
) : Parcelable
