package za.co.woolworths.financial.services.android.chanel.listener

import za.co.woolworths.financial.services.android.models.dto.ProductList

interface ProductListSelectionListener {
    fun clickOnProductSelection(productList: ProductList?)
}