package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import java.io.Serializable

data class VirtualCardStaffMemberMessage (
    val title: String,
    val paragraphs: List<String>
) : Serializable
