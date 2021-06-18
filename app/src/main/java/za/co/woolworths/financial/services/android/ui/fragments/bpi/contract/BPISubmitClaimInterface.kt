package za.co.woolworths.financial.services.android.ui.fragments.bpi.contract

import za.co.woolworths.financial.services.android.models.dto.bpi.ClaimReason

interface BPISubmitClaimInterface {
    fun submitClaimList(): MutableList<ClaimReason>?
}