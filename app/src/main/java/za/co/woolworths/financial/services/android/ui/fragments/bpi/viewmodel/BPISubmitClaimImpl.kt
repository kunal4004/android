package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.bpi.ClaimReason
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPISubmitClaimInterface

class BPISubmitClaimImpl : BPISubmitClaimInterface {

    override fun submitClaimList(): MutableList<ClaimReason>? {
        return WoolworthsApplication.getInstance()?.balanceProtectionInsurance?.claimReason
    }
}