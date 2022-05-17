package za.co.woolworths.financial.services.android.util.eliteplan

import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan

interface EligibilityImpl {
    fun eligibilityResponse(eligibilityPlan: EligibilityPlan?)
    fun eligibilityFailed()
}

interface PMApiStatusImpl {
    fun pmaSuccess()
}