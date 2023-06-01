package za.co.woolworths.financial.services.android.ui.views.shop.dash

import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue

interface OnDataUpdateListener {
    fun onProductCatalogueUpdate(productCatalogues: ArrayList<ProductCatalogue>?)
}