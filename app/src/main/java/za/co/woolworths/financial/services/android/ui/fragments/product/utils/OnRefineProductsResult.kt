package za.co.woolworths.financial.services.android.ui.fragments.product.utils

import za.co.woolworths.financial.services.android.models.dto.ProductView

interface OnRefineProductsResult {
    fun onProductRefineSuccess(productView: ProductView)
    fun onProductRefineFailure(message: String)
    fun onProductRefineResetSuccess(productView: ProductView)
}