package za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener

import za.co.woolworths.financial.services.android.models.dto.ProductList

interface ProductListSelectionListener {
    fun clickOnProductSelection(productList: ProductList?)
}