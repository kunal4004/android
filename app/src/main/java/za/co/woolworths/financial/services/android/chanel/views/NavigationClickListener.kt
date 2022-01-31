package za.co.woolworths.financial.services.android.chanel.views

import za.co.woolworths.financial.services.android.models.dto.ProductList

interface NavigationClickListener {
    fun openProductDetailsView(productList: ProductList)
    fun openCategoryListView()
}