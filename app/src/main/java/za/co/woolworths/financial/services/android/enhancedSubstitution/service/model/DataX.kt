package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

import za.co.woolworths.financial.services.android.models.dto.FormException

data class DataX(
        val substitutionInfo: List<Any>,
        val formexceptions:List<FormException>
)