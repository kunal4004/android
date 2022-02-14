package za.co.woolworths.financial.services.android.chanel.views

import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.models.dto.ProductList

interface ChanelNavigationClickListener {
    fun openProductDetailsView(productList: ProductList?, bannerLabel: String?, bannerImage: String?)
    fun clickCategoryListViewCell(
        navigation: Navigation?,
        bannerImage: String?,
        bannerLabel: String?,
        isComingFromBlp: Boolean
    )
}