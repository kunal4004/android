package za.co.woolworths.financial.services.android.ui.fragments.product.utils

import za.co.woolworths.financial.services.android.models.dto.ProductView

interface OnRefineProductsResult {
    fun onProductRefineSuccess(productView: ProductView, navigationState: String)
    fun onProductRefineFailure(message: String)
}