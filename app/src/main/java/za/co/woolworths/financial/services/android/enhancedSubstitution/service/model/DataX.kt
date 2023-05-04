package za.co.woolworths.financial.services.android.enhancedSubstitution.model

import za.co.woolworths.financial.services.android.models.dto.FormException

data class DataX(
        val substitutionInfo: List<Any>,
        val formExceptions:List<FormException>
)