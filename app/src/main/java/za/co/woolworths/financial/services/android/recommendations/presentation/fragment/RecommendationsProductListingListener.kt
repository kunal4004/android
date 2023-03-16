package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product

interface RecommendationsProductListingListener: IProductListing {
    fun openProductDetailView(productList: Product)
}