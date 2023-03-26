package za.co.woolworths.financial.services.android.enhancedSubstitution

import za.co.woolworths.financial.services.android.models.dto.ProductList

interface ProductListSelectionListener {
    fun clickOnProductSelection(productList: ProductList?)
}