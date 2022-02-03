package za.co.woolworths.financial.services.android.chanel.views

import za.co.woolworths.financial.services.android.chanel.model.Navigation
import za.co.woolworths.financial.services.android.models.dto.ProductList

interface ChanelNavigationClickListener {
    fun openProductDetailsView(productList: ProductList?)
    fun openCategoryListView(navigation: Navigation?)
}