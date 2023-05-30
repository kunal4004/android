package za.co.woolworths.financial.services.android.enhancedSubstitution.utils.listener

import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item

interface ProductSubstitutionListListener {
    fun clickOnSubstituteProduct(item: Item?)
}